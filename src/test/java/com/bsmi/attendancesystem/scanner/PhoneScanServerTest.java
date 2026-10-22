package com.bsmi.attendancesystem.scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bsmi.attendancesystem.StudentData;
import com.bsmi.attendancesystem.service.StudentCards;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import javax.imageio.ImageIO;

/** Talks to the server the way the phone page does, over real HTTP on this computer. */
class PhoneScanServerTest {
    private static final String CHECKED_IN = "Asha Verma checked in 09:12";

    private final HttpClient http = HttpClient.newHttpClient();
    // Card IDs the server passed on to the app
    private final List<String> received = new CopyOnWriteArrayList<>();
    private PhoneScanServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.close();
        }
        http.close();
    }

    @Test
    void pageForThePhoneIsServedAtTheTokenAddress() throws Exception {
        start(checksEveryoneIn());

        HttpResponse<String> response = get(page());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type"))
                .contains("text/html; charset=utf-8");
        assertThat(response.body()).contains("<title>Card scanner", "Take photo", "fetch(\"scan\"");
    }

    @Test
    void answersAreNotCachedNorSniffedAndKeepTheTokenToThemselves() throws Exception {
        start(checksEveryoneIn());

        HttpResponse<String> response = get(page());

        assertThat(response.headers().firstValue("Cache-Control")).contains("no-store");
        assertThat(response.headers().firstValue("X-Content-Type-Options")).contains("nosniff");
        assertThat(response.headers().firstValue("Referrer-Policy")).contains("no-referrer");
        assertThat(response.headers().firstValue("Content-Security-Policy"))
                .hasValueSatisfying(policy -> assertThat(policy).contains("default-src 'none'"));
    }

    @Test
    void photoOfACardChecksTheStudentIn() throws Exception {
        start(checksEveryoneIn());

        HttpResponse<String> response = postPhoto(png(StudentCards.draw(asha())));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo(CHECKED_IN);
        assertThat(received).containsExactly("DEMO-1001");
    }

    @Test
    void bigPhonePhotoWithTheCardInACornerStillDecodes() throws Exception {
        start(checksEveryoneIn());
        BufferedImage photo = new BufferedImage(4000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = photo.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, photo.getWidth(), photo.getHeight());
        BufferedImage card = StudentCards.draw(asha());
        g.drawImage(card, 2600, 1500, card.getWidth() * 3, card.getHeight() * 3, null);
        g.dispose();

        HttpResponse<String> response = postPhoto(png(photo));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(received).containsExactly("DEMO-1001");
    }

    @Test
    void typedCardIdIsPassedOnWithoutSurroundingSpaces() throws Exception {
        start(checksEveryoneIn());

        HttpResponse<String> response = postTyped("  DEMO-1001 \n");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(received).containsExactly("DEMO-1001");
    }

    @Test
    void typedCardIdOfTheLongestAllowedLengthIsPassedOn() throws Exception {
        start(checksEveryoneIn());
        String longest = "C".repeat(PhoneScanServer.MAX_CARD_ID_LENGTH);

        assertThat(postTyped(longest).statusCode()).isEqualTo(200);
        assertThat(received).containsExactly(longest);
    }

    @Test
    void typedCardIdLongerThanAnyCardIsRefused() throws Exception {
        start(checksEveryoneIn());

        HttpResponse<String> response =
                postTyped("C".repeat(PhoneScanServer.MAX_CARD_ID_LENGTH + 1));

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).isEqualTo("A card ID is at most 64 characters");
        assertThat(received).isEmpty();
    }

    @Test
    void reasonTheAppRefusedTheCardIsShownOnThePhone() throws Exception {
        start(cardId -> ScanReply.failed("No student has card " + cardId));

        HttpResponse<String> response = postTyped("NOPE-1");

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.body()).isEqualTo("No student has card NOPE-1");
    }

    @Test
    void crashInTheAppGivesAServerErrorWithoutDetails() throws Exception {
        start(
                cardId -> {
                    throw new IllegalStateException("secret stack details");
                });

        HttpResponse<String> response = postTyped("DEMO-1001");

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.body())
                .isEqualTo("Something went wrong in the app. Try again")
                .doesNotContain("secret");
    }

    @Test
    void fileThatIsNotAPictureIsRefused() throws Exception {
        start(checksEveryoneIn());

        HttpResponse<String> response = postPhoto("this is not a picture".getBytes());

        assertThat(response.statusCode()).isEqualTo(415);
        assertThat(response.body()).startsWith("That file is not a photo");
        assertThat(received).isEmpty();
    }

    @Test
    void photoCutOffPartWayIsRefused() throws Exception {
        start(checksEveryoneIn());
        byte[] photo = png(StudentCards.draw(asha()));

        HttpResponse<String> response = postPhoto(Arrays.copyOf(photo, 40));

        assertThat(response.statusCode()).isEqualTo(415);
        assertThat(response.body()).startsWith("That file is not a photo");
        assertThat(received).isEmpty();
    }

    @Test
    void pictureWithoutAQrCodeAsksForAnotherTry() throws Exception {
        start(checksEveryoneIn());

        HttpResponse<String> response =
                postPhoto(png(new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB)));

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.body()).startsWith("No QR code found in the photo");
        assertThat(received).isEmpty();
    }

    @Test
    void photoOverTheSizeLimitIsRefused() throws Exception {
        start(checksEveryoneIn());

        HttpResponse<String> response = postPhoto(new byte[PhoneScanServer.MAX_PHOTO_BYTES + 1]);

        assertThat(response.statusCode()).isEqualTo(413);
        assertThat(response.body()).startsWith("The photo is over 15 MB");
        assertThat(received).isEmpty();
    }

    @Test
    void addressesWithoutTheRightTokenAreNotFound() throws Exception {
        start(checksEveryoneIn());
        String root = "http://127.0.0.1:" + server.port();
        String token = page().getPath().replace("/", "");
        String wrongToken = "0".repeat(token.length());

        for (String path :
                List.of(
                        "/",
                        "/scan",
                        "/" + wrongToken + "/",
                        "/" + wrongToken + "/scan",
                        "/" + token,
                        "/" + token + "/scan/more",
                        "/" + token.substring(1) + "/")) {
            HttpResponse<String> response = get(URI.create(root + path));
            assertThat(response.statusCode()).as(path).isEqualTo(404);
            assertThat(response.body()).as(path).isEqualTo("Not found");
        }
    }

    @Test
    void wrongMethodOnARealAddressIsRefused() throws Exception {
        start(checksEveryoneIn());

        HttpResponse<String> postToPage =
                send(HttpRequest.newBuilder(page()).POST(BodyPublishers.ofString("DEMO-1001")));
        HttpResponse<String> getScan = get(page().resolve("scan"));

        assertThat(postToPage.statusCode()).isEqualTo(405);
        assertThat(getScan.statusCode()).isEqualTo(405);
        assertThat(received).isEmpty();
    }

    @Test
    void everyStartMakesANewToken() throws Exception {
        start(checksEveryoneIn());
        URI first = page();
        server.close();

        start(checksEveryoneIn());

        assertThat(page().getPath()).isNotEqualTo(first.getPath()).matches("/[0-9a-f]{32}/");
    }

    @Test
    void pageAddressUsesTheGivenHostAndThePortTheServerGot() throws Exception {
        start(checksEveryoneIn());

        assertThat(server.port()).isPositive();
        assertThat(page().toString()).startsWith("http://127.0.0.1:" + server.port() + "/");
    }

    @Test
    void stoppedServerNoLongerAnswers() throws Exception {
        start(checksEveryoneIn());
        URI page = page();

        server.close();

        assertThatThrownBy(() -> get(page)).isInstanceOf(ConnectException.class);
    }

    @Test
    void portAlreadyInUseFailsToStart() throws Exception {
        start(checksEveryoneIn());
        InetSocketAddress taken =
                new InetSocketAddress(NetworkAddresses.thisComputerOnly(), server.port());

        assertThatThrownBy(() -> PhoneScanServer.start(taken, checksEveryoneIn()))
                .isInstanceOf(IOException.class);
    }

    @Test
    void smallPhotoIsDecodedAsItIs() {
        BufferedImage small =
                new BufferedImage(PhoneScanServer.MAX_DECODE_SIDE, 900, BufferedImage.TYPE_INT_RGB);

        assertThat(PhoneScanServer.shrinkForDecoding(small)).isSameAs(small);
    }

    @Test
    void bigPhotoIsShrunkKeepingItsShape() {
        BufferedImage landscape =
                PhoneScanServer.shrinkForDecoding(
                        new BufferedImage(4000, 3000, BufferedImage.TYPE_INT_RGB));
        BufferedImage portrait =
                PhoneScanServer.shrinkForDecoding(
                        new BufferedImage(3000, 4000, BufferedImage.TYPE_INT_RGB));

        assertThat(landscape.getWidth()).isEqualTo(1600);
        assertThat(landscape.getHeight()).isEqualTo(1200);
        assertThat(portrait.getWidth()).isEqualTo(1200);
        assertThat(portrait.getHeight()).isEqualTo(1600);
    }

    private void start(Function<String, ScanReply> onCardId) throws IOException {
        server =
                PhoneScanServer.start(
                        new InetSocketAddress(NetworkAddresses.thisComputerOnly(), 0), onCardId);
    }

    private Function<String, ScanReply> checksEveryoneIn() {
        return cardId -> {
            received.add(cardId);
            return ScanReply.ok(CHECKED_IN);
        };
    }

    private URI page() {
        return server.pageAddress(NetworkAddresses.thisComputerOnly());
    }

    private HttpResponse<String> get(URI address) throws IOException, InterruptedException {
        return send(HttpRequest.newBuilder(address).GET());
    }

    private HttpResponse<String> postPhoto(byte[] photo) throws IOException, InterruptedException {
        return post(BodyPublishers.ofByteArray(photo), "image/png");
    }

    private HttpResponse<String> postTyped(String cardId) throws IOException, InterruptedException {
        return post(BodyPublishers.ofString(cardId), "text/plain; charset=utf-8");
    }

    private HttpResponse<String> post(BodyPublisher body, String contentType)
            throws IOException, InterruptedException {
        return send(
                HttpRequest.newBuilder(page().resolve("scan"))
                        .header("Content-Type", contentType)
                        .POST(body));
    }

    private HttpResponse<String> send(HttpRequest.Builder request)
            throws IOException, InterruptedException {
        return http.send(request.build(), BodyHandlers.ofString());
    }

    private static byte[] png(BufferedImage image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    private static StudentData asha() {
        return new StudentData(
                1001,
                "1st Sem",
                "BSc",
                "Asha",
                "Verma",
                "Female",
                Date.valueOf("2005-03-14"),
                "Enrolled",
                "",
                "DEMO-1001");
    }
}
