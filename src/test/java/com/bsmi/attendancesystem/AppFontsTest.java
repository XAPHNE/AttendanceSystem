package com.bsmi.attendancesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import javafx.scene.text.Font;
import org.junit.jupiter.api.Test;

class AppFontsTest {
    @Test
    void bundledFontsLoadUnderTheNamesTheStylesheetsUse() {
        List<Font> fonts = AppFonts.load(AppFonts.BUNDLED_FILES);

        assertThat(fonts)
                .extracting(Font::getName)
                .containsExactly("Inter Regular", "Inter SemiBold", "Inter Bold");
        assertThat(fonts)
                .extracting(Font::getFamily)
                .containsExactly("Inter", "Inter SemiBold", "Inter");
    }

    @Test
    void missingFontFileIsReportedByName() {
        assertThatThrownBy(() -> AppFonts.load(List.of("Missing.ttf")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Font missing from the app: fonts/Missing.ttf");
    }

    @Test
    void fileThatIsNotAFontIsReportedByName() {
        assertThatThrownBy(() -> AppFonts.load(List.of("Inter-LICENSE.txt")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not a usable font file: fonts/Inter-LICENSE.txt");
    }
}
