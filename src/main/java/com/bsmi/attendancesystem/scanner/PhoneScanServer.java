package com.bsmi.attendancesystem.scanner;

import com.bsmi.attendancesystem.service.StudentCards;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import javax.imageio.ImageIO;

/**
 * A small web page that turns a phone into a card scanner: the phone photographs a student's QR
 * card (or the ID is typed in) and the card ID goes to {@code onCardId}, like a reader typing it.
 *
 * <p>Every address holds a random token, so only a phone that scanned the pairing code can use it;
 * anything else gets 404. Each start makes a new token.
 */
public final class PhoneScanServer implements AutoCloseable {
    static final int MAX_PHOTO_BYTES = 15 * 1024 * 1024;
    static final int MAX_CARD_ID_LENGTH = 64;
    // Phone photos are 12 megapixels or more; this is plenty for a card and decodes fast
    static final int MAX_DECODE_SIDE = 1600;
    private static final int TOKEN_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PAGE_RESOURCE = "phone-page.html";

    private final HttpServer server;
    private final ExecutorService worker;
    private final String token;
    private final byte[] page;
    private final Function<String, ScanReply> onCardId;

    private PhoneScanServer(HttpServer server, Function<String, ScanReply> onCardId) {
        this.server = server;
        this.onCardId = onCardId;
        this.token = HexFormat.of().formatHex(randomBytes(TOKEN_BYTES));
        this.page = readPage();
        this.worker =
                Executors.newSingleThreadExecutor(
                        Thread.ofPlatform().name("phone-scanner").daemon().factory());
    }

    /**
     * Starts answering on {@code address}. {@code onCardId} is called on a background thread.
     *
     * @throws IOException if the port is already taken
     */
    public static PhoneScanServer start(
            InetSocketAddress address, Function<String, ScanReply> onCardId) throws IOException {
        HttpServer server = HttpServer.create(address, 0);
        PhoneScanServer phoneScanner = new PhoneScanServer(server, onCardId);
        server.setExecutor(phoneScanner.worker);
        server.createContext("/", phoneScanner::handle);
        server.start();
        return phoneScanner;
    }

    public int port() {
        return server.getAddress().getPort();
    }

    /**
     * @return the page for the phone, for example {@code http://192.168.1.20:52114/3f9c…/}
     */
    public URI pageAddress(Inet4Address host) {
        return URI.create("http://" + host.getHostAddress() + ":" + port() + "/" + token + "/");
    }

    @Override
    public void close() {
        server.stop(0);
        worker.shutdownNow();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getRawPath();
            String method = exchange.getRequestMethod();
            if (isTokenPath(path, "/")) {
                if ("GET".equals(method)) {
                    send(exchange, 200, "text/html; charset=utf-8", page);
                } else {
                    sendText(exchange, 405, "Only GET works here");
                }
            } else if (isTokenPath(path, "/scan")) {
                if ("POST".equals(method)) {
                    Answer answer = scan(exchange);
                    sendText(exchange, answer.status(), answer.text());
                } else {
                    sendText(exchange, 405, "Only POST works here");
                }
            } else {
                sendText(exchange, 404, "Not found");
            }
        }
    }

    // Compares in constant time, so the response time gives no hint about the token
    private boolean isTokenPath(String path, String rest) {
        byte[] expected = ("/" + token + rest).getBytes(StandardCharsets.UTF_8);
        return path != null
                && MessageDigest.isEqual(path.getBytes(StandardCharsets.UTF_8), expected);
    }

    private Answer scan(HttpExchange exchange) throws IOException {
        byte[] body = readAtMost(exchange.getRequestBody(), MAX_PHOTO_BYTES);
        if (body == null) {
            return new Answer(413, "The photo is over 15 MB. Take it again at a lower resolution");
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("text/plain")) {
            String typed = new String(body, StandardCharsets.UTF_8).strip();
            if (typed.length() > MAX_CARD_ID_LENGTH) {
                return new Answer(
                        400, "A card ID is at most " + MAX_CARD_ID_LENGTH + " characters");
            }
            return askApp(typed);
        }
        Optional<BufferedImage> photo = readImage(body);
        if (photo.isEmpty()) {
            return new Answer(415, "That file is not a photo the app can read. Use JPEG or PNG");
        }
        Optional<String> cardId = StudentCards.decode(shrinkForDecoding(photo.get()));
        if (cardId.isEmpty()) {
            return new Answer(
                    422,
                    "No QR code found in the photo. Hold the card flat, fill the frame and try"
                        + " again");
        }
        return askApp(cardId.get());
    }

    private Answer askApp(String cardId) {
        try {
            ScanReply reply = onCardId.apply(cardId);
            return new Answer(reply.ok() ? 200 : 422, reply.message());
        } catch (RuntimeException e) {
            return new Answer(500, "Something went wrong in the app. Try again");
        }
    }

    /**
     * @return the body, or null if it is longer than {@code limit} (the rest is read and dropped)
     */
    private static byte[] readAtMost(InputStream in, int limit) throws IOException {
        byte[] body = in.readNBytes(limit + 1);
        if (body.length <= limit) {
            return body;
        }
        // Reading the rest lets the phone finish sending, so it sees the answer instead of an error
        in.transferTo(OutputStream.nullOutputStream());
        return null;
    }

    private static Optional<BufferedImage> readImage(byte[] bytes) {
        try {
            return Optional.ofNullable(ImageIO.read(new ByteArrayInputStream(bytes)));
        } catch (IOException brokenImage) {
            return Optional.empty();
        }
    }

    static BufferedImage shrinkForDecoding(BufferedImage photo) {
        int longest = Math.max(photo.getWidth(), photo.getHeight());
        if (longest <= MAX_DECODE_SIDE) {
            return photo;
        }
        double scale = (double) MAX_DECODE_SIDE / longest;
        int width = Math.max(1, (int) Math.round(photo.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(photo.getHeight() * scale));
        BufferedImage smaller = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = smaller.createGraphics();
        try {
            g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(photo, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return smaller;
    }

    private static void sendText(HttpExchange exchange, int status, String text)
            throws IOException {
        send(exchange, status, "text/plain; charset=utf-8", text.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] body)
            throws IOException {
        var headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Cache-Control", "no-store");
        // The token is in the address, so it must not leak to other sites
        headers.set("Referrer-Policy", "no-referrer");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set(
                "Content-Security-Policy",
                "default-src 'none'; style-src 'unsafe-inline'; script-src 'unsafe-inline';"
                        + " connect-src 'self'; img-src blob:");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
    }

    private static byte[] randomBytes(int count) {
        byte[] bytes = new byte[count];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static byte[] readPage() {
        try (InputStream in = PhoneScanServer.class.getResourceAsStream(PAGE_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(PAGE_RESOURCE + " is missing from the app");
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + PAGE_RESOURCE, e);
        }
    }

    private record Answer(int status, String text) {}
}
