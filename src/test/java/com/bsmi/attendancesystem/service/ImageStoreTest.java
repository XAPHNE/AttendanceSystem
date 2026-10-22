package com.bsmi.attendancesystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImageStoreTest {
    private static final byte[] PHOTO_BYTES = {1, 2, 3, 4};

    @TempDir Path tempDir;

    private Path storeFolder;
    private ImageStore store;

    @BeforeEach
    void createStore() {
        storeFolder = tempDir.resolve("images");
        store = new ImageStore(storeFolder);
    }

    @Test
    void savedPhotoIsCopiedIntoTheStore() throws IOException {
        Path original = photo("asha.png");

        String storedName = store.save(original);

        assertThat(storedName).endsWith(".png").doesNotContain("asha");
        assertThat(storeFolder.resolve(storedName)).hasBinaryContent(PHOTO_BYTES);
    }

    @Test
    void storedPhotoStillShowsAfterTheOriginalIsDeleted() throws IOException {
        Path original = photo("asha.jpg");
        String storedName = store.save(original);

        Files.delete(original);

        assertThat(store.find(storedName)).contains(storeFolder.resolve(storedName));
    }

    @Test
    void savingTheSamePhotoTwiceKeepsTwoCopies() throws IOException {
        Path original = photo("asha.png");

        assertThat(store.save(original)).isNotEqualTo(store.save(original));
    }

    @Test
    void extensionIsMatchedCaseInsensitivelyAndStoredLowercase() throws IOException {
        assertThat(store.save(photo("ROHAN.JPEG"))).endsWith(".jpeg");
    }

    @Test
    void unsupportedFileTypeIsRejected() throws IOException {
        Path notAPhoto = photo("notes.txt");

        assertThatThrownBy(() -> store.save(notAPhoto))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("Photo must be a PNG or JPEG file");
        assertThat(storeFolder).doesNotExist();
    }

    @Test
    void fileWithoutExtensionIsRejected() throws IOException {
        Path noExtension = photo("photo");

        assertThatThrownBy(() -> store.save(noExtension)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void missingOriginalFailsWithItsPath() {
        Path missing = tempDir.resolve("gone.png");

        assertThatThrownBy(() -> store.save(missing))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("gone.png");
    }

    @Test
    void storeFolderThatIsAFileFails() throws IOException {
        Files.writeString(storeFolder, "in the way");
        Path original = photo("asha.png");

        assertThatThrownBy(() -> store.save(original)).isInstanceOf(UncheckedIOException.class);
    }

    @Test
    void noPhotoFindsNothing() {
        assertThat(store.find("")).isEmpty();
        assertThat(store.find("   ")).isEmpty();
        assertThat(store.find(null)).isEmpty();
    }

    @Test
    void deletedStoredPhotoFindsNothing() throws IOException {
        String storedName = store.save(photo("asha.png"));

        Files.delete(storeFolder.resolve(storedName));

        assertThat(store.find(storedName)).isEmpty();
    }

    @Test
    void absolutePathFromAnOlderVersionFindsNothing() throws IOException {
        Path elsewhere = photo("asha.png");

        assertThat(store.find(elsewhere.toString())).isEmpty();
    }

    @Test
    void nameLeadingOutOfTheStoreFindsNothing() throws IOException {
        Files.createDirectories(storeFolder);
        photo("secret.png");

        assertThat(store.find("../secret.png")).isEmpty();
        assertThat(store.find("..")).isEmpty();
    }

    @Test
    void nameThatIsNoValidPathFindsNothing() {
        assertThat(store.find("bad\0name.png")).isEmpty();
    }

    @Test
    void folderInsideTheStoreIsNoPhoto() throws IOException {
        Files.createDirectories(storeFolder.resolve("album"));

        assertThat(store.find("album")).isEmpty();
    }

    private Path photo(String fileName) throws IOException {
        return Files.write(tempDir.resolve(fileName), PHOTO_BYTES);
    }
}
