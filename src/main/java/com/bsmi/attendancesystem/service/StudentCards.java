package com.bsmi.attendancesystem.service;

import com.bsmi.attendancesystem.StudentData;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.ReaderException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;

/**
 * Printable student cards: a QR code of the card ID with the student's name and number under it.
 * Any QR scanner in keyboard mode, or the phone scanner, reads the card ID back from it.
 */
public final class StudentCards {
    static final int WIDTH = 360;
    static final int HEIGHT = 440;
    private static final int QR_SIZE = 320;
    private static final int MARGIN = (WIDTH - QR_SIZE) / 2;
    private static final String NEW_CARD_PREFIX = "STU-";
    // No 0/O, 1/I/L, so an ID read off the card can be typed without mix-ups
    private static final String NEW_CARD_LETTERS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int NEW_CARD_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private StudentCards() {}

    /** A random ID like {@code STU-K7Q2MX9A} for a student who has no card yet. */
    public static String newCardId() {
        StringBuilder id = new StringBuilder(NEW_CARD_PREFIX);
        for (int i = 0; i < NEW_CARD_LENGTH; i++) {
            id.append(NEW_CARD_LETTERS.charAt(RANDOM.nextInt(NEW_CARD_LETTERS.length())));
        }
        return id.toString();
    }

    /** @throws MissingFieldException if the student has no card ID */
    public static BufferedImage draw(StudentData student) {
        String cardId = student.getCardId();
        if (cardId == null || cardId.isBlank()) {
            throw new MissingFieldException("Card ID");
        }
        BufferedImage card = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = card.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setColor(Color.BLACK);
            drawQrCode(g, cardId);
            g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawCentered(g, student.getFirstName() + " " + student.getLastName(),
                    new Font(Font.SANS_SERIF, Font.BOLD, 22), MARGIN + QR_SIZE + 40);
            drawCentered(g, "Student #" + student.getStudentNum() + "   " + cardId,
                    new Font(Font.SANS_SERIF, Font.PLAIN, 16), MARGIN + QR_SIZE + 72);
        } finally {
            g.dispose();
        }
        return card;
    }

    /**
     * Saves the student's card as a PNG.
     *
     * @throws MissingFieldException if the student has no card ID
     * @throws UncheckedIOException if the file can't be written
     */
    public static void savePng(StudentData student, Path file) {
        BufferedImage card = draw(student);
        try {
            ImageIO.write(card, "png", file.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the card to " + file, e);
        }
    }

    /** The chosen file name with {@code .png} added, as some save dialogs leave it off. */
    public static Path withPngExtension(Path file) {
        String name = file.getFileName().toString();
        return name.toLowerCase(Locale.ROOT).endsWith(".png")
                ? file
                : file.resolveSibling(name + ".png");
    }

    /** @return the text of the QR code in the image, or empty if there is no readable one */
    public static Optional<String> decode(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        BinaryBitmap bitmap =
                new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixels)));
        try {
            return Optional.of(
                    new QRCodeReader()
                            .decode(bitmap, Map.of(DecodeHintType.TRY_HARDER, Boolean.TRUE))
                            .getText());
        } catch (ReaderException noReadableQrCode) {
            return Optional.empty();
        }
    }

    private static void drawQrCode(Graphics2D g, String cardId) {
        BitMatrix code;
        try {
            code = new QRCodeWriter()
                    .encode(cardId, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE,
                            Map.of(EncodeHintType.MARGIN, 0));
        } catch (WriterException e) {
            // Only happens for text far longer than a card ID can be
            throw new IllegalStateException("Could not make a QR code of " + cardId, e);
        }
        for (int y = 0; y < code.getHeight(); y++) {
            for (int x = 0; x < code.getWidth(); x++) {
                if (code.get(x, y)) {
                    g.fillRect(MARGIN + x, MARGIN + y, 1, 1);
                }
            }
        }
    }

    private static void drawCentered(Graphics2D g, String text, Font font, int baseline) {
        g.setFont(font);
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (WIDTH - textWidth) / 2, baseline);
    }
}
