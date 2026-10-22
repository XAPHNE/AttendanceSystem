package com.bsmi.attendancesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import javafx.scene.text.Font;

/**
 * Loads the Inter font that ships inside the app, so the text looks the same on every OS instead
 * of falling back to whatever system font each one has. Once loaded, the stylesheets can use it by
 * its family name.
 */
public final class AppFonts {
    static final List<String> BUNDLED_FILES =
            List.of("Inter-Regular.ttf", "Inter-SemiBold.ttf", "Inter-Bold.ttf");

    private AppFonts() {}

    public static void loadBundled() {
        load(BUNDLED_FILES);
    }

    static List<Font> load(List<String> fileNames) {
        return fileNames.stream().map(AppFonts::load).toList();
    }

    private static Font load(String fileName) {
        String resource = "fonts/" + fileName;
        try (InputStream file = AppFonts.class.getResourceAsStream(resource)) {
            if (file == null) {
                throw new IllegalStateException("Font missing from the app: " + resource);
            }
            // The size only matters for the returned Font; CSS picks its own sizes
            Font font = Font.loadFont(file, Font.getDefault().getSize());
            if (font == null) {
                throw new IllegalStateException("Not a usable font file: " + resource);
            }
            return font;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the font " + resource, e);
        }
    }
}
