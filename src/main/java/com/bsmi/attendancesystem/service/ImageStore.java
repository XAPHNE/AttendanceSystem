package com.bsmi.attendancesystem.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Keeps student photos in the app's own folder. A chosen photo is copied in under a new random
 * name and only that name is stored with the student, so the photo still shows after the original
 * file is moved or the data folder is copied to another computer.
 */
public final class ImageStore {
    private static final Set<String> PHOTO_EXTENSIONS = Set.of("png", "jpg", "jpeg");

    private final Path folder;

    public ImageStore(Path folder) {
        this.folder = folder.toAbsolutePath().normalize();
    }

    /**
     * Copies the photo into the store.
     *
     * @return the name to store with the student
     * @throws InvalidValueException if the file isn't a PNG or JPEG
     * @throws UncheckedIOException if the photo can't be read or the folder can't be written
     */
    public String save(Path photo) {
        String extension = extensionOf(photo);
        if (!PHOTO_EXTENSIONS.contains(extension)) {
            throw new InvalidValueException("Photo", "a PNG or JPEG file");
        }
        String storedName = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(folder);
            Files.copy(photo, folder.resolve(storedName));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the photo " + photo, e);
        }
        return storedName;
    }

    /**
     * @return the stored photo, or empty if the name is blank, the file is gone, or the name points
     *     outside the store
     */
    public Optional<Path> find(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return Optional.empty();
        }
        try {
            Path photo = folder.resolve(storedName).normalize();
            // Rejects absolute paths saved by older versions and names like "../secret.png"
            boolean insideStore = folder.equals(photo.getParent());
            return insideStore && Files.isRegularFile(photo) ? Optional.of(photo) : Optional.empty();
        } catch (InvalidPathException notAFileName) {
            return Optional.empty();
        }
    }

    private static String extensionOf(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
