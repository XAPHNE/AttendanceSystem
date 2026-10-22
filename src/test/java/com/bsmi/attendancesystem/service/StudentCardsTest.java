package com.bsmi.attendancesystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bsmi.attendancesystem.StudentData;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class StudentCardsTest {
    @TempDir Path tempDir;

    @Test
    void drawnCardDecodesBackToTheCardId() {
        BufferedImage card = StudentCards.draw(asha("DEMO-1001"));

        assertThat(card.getWidth()).isEqualTo(StudentCards.WIDTH);
        assertThat(card.getHeight()).isEqualTo(StudentCards.HEIGHT);
        assertThat(StudentCards.decode(card)).contains("DEMO-1001");
    }

    @Test
    void longestCardIdStillFitsInTheQrCode() {
        String longest = "C".repeat(StudentService.CARD_ID_MAX_LENGTH);

        assertThat(StudentCards.decode(StudentCards.draw(asha(longest)))).contains(longest);
    }

    @Test
    void cardStillDecodesWhenSmallAndOffCentreLikeInAPhoto() {
        BufferedImage card = StudentCards.draw(asha("DEMO-1001"));
        BufferedImage photo = new BufferedImage(900, 700, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = photo.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, photo.getWidth(), photo.getHeight());
        g.drawImage(card, 500, 300, card.getWidth() / 2, card.getHeight() / 2, null);
        g.dispose();

        assertThat(StudentCards.decode(photo)).contains("DEMO-1001");
    }

    @Test
    void qrCodeHasTheAskedSizeAndDecodesBackToTheText() {
        String pageAddress = "http://192.168.1.20:52114/" + "f".repeat(32) + "/";

        BufferedImage qr = StudentCards.qrCode(pageAddress, 110);

        assertThat(qr.getWidth()).isEqualTo(110);
        assertThat(qr.getHeight()).isEqualTo(110);
        assertThat(StudentCards.decode(withQuietZone(qr))).contains(pageAddress);
    }

    @Test
    void qrCodeTooSmallForItsTextIsStillDrawnAtTheSmallestSizeThatFits() {
        BufferedImage qr = StudentCards.qrCode("DEMO-1001", 1);

        // The smallest QR code is 21 by 21 squares, here one pixel each
        assertThat(qr.getWidth()).isEqualTo(21);
        assertThat(StudentCards.decode(withQuietZone(enlarged(qr, 10)))).contains("DEMO-1001");
    }

    @Test
    void textLongerThanAnyQrCodeHoldsFails() {
        assertThatThrownBy(() -> StudentCards.qrCode("x".repeat(5000), 110))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Could not make a QR code of xxx");
    }

    @Test
    void savedPngDecodesBackToTheCardId() throws IOException {
        Path file = tempDir.resolve("card-1001.png");

        StudentCards.savePng(asha("DEMO-1001"), file);

        assertThat(StudentCards.decode(ImageIO.read(file.toFile()))).contains("DEMO-1001");
    }

    @Test
    void savingIntoAMissingFolderFailsWithUncheckedIo() {
        Path file = tempDir.resolve("no-such-folder").resolve("card.png");

        assertThatThrownBy(() -> StudentCards.savePng(asha("DEMO-1001"), file))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("Could not save the card");
        assertThat(Files.exists(file)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void studentWithoutCardIdHasNoCardToDraw(String noCard) {
        assertThatThrownBy(() -> StudentCards.draw(asha(noCard)))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the Card ID field");
    }

    @Test
    void blankImageHasNoCardId() {
        BufferedImage blank = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);

        assertThat(StudentCards.decode(blank)).isEmpty();
    }

    @Test
    void noiseHasNoCardId() {
        Random seeded = new Random(42);
        BufferedImage noise = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < noise.getHeight(); y++) {
            for (int x = 0; x < noise.getWidth(); x++) {
                noise.setRGB(x, y, seeded.nextBoolean() ? 0xFFFFFF : 0x000000);
            }
        }

        assertThat(StudentCards.decode(noise)).isEmpty();
    }

    @Test
    void newCardIdsLookLikeStuAndEightEasyToReadLetters() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(StudentCards.newCardId());
        }

        assertThat(ids).hasSize(1000).allMatch(id -> id.matches("STU-[A-HJKMNP-Z2-9]{8}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"card", "card.jpg", "card.png.txt"})
    void fileNameWithoutPngGetsIt(String name) {
        assertThat(StudentCards.withPngExtension(tempDir.resolve(name)))
                .isEqualTo(tempDir.resolve(name + ".png"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"card.png", "CARD.PNG"})
    void fileNameEndingInPngIsKept(String name) {
        assertThat(StudentCards.withPngExtension(tempDir.resolve(name)))
                .isEqualTo(tempDir.resolve(name));
    }

    private static BufferedImage enlarged(BufferedImage image, int times) {
        BufferedImage big = new BufferedImage(
                image.getWidth() * times, image.getHeight() * times, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = big.createGraphics();
        g.drawImage(image, 0, 0, big.getWidth(), big.getHeight(), null);
        g.dispose();
        return big;
    }

    // A screen shows the code on a white panel; readers need that white border around it
    private static BufferedImage withQuietZone(BufferedImage qr) {
        int border = 20;
        BufferedImage framed = new BufferedImage(
                qr.getWidth() + 2 * border, qr.getHeight() + 2 * border, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = framed.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, framed.getWidth(), framed.getHeight());
        g.drawImage(qr, border, border, null);
        g.dispose();
        return framed;
    }

    private static StudentData asha(String cardId) {
        return new StudentData(
                1001, "1st Sem", "BSc", "Asha", "Verma", "Female", Date.valueOf("2005-03-14"),
                "Enrolled", "", cardId);
    }
}
