package com.bsmi.attendancesystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DatabaseSettingsTest {
    private static final Path DATA_DIR = Path.of("/home/asha/.local/share/AttendanceSystem");

    @Test
    void defaultsToEmbeddedFileInDataDirectory() {
        DatabaseSettings settings = DatabaseSettings.fromEnvironment(Map.of(), DATA_DIR);

        assertThat(settings.url())
                .isEqualTo("jdbc:h2:file:/home/asha/.local/share/AttendanceSystem/attendance_system");
        assertThat(settings.user()).isEqualTo("admin");
        assertThat(settings.password()).isEqualTo("admin");
    }

    @Test
    void environmentVariablesOverrideEveryDefault() {
        Map<String, String> environment =
                Map.of(
                        "ATTENDANCE_DB_URL", "jdbc:h2:mem:other",
                        "ATTENDANCE_DB_USER", "teacher",
                        "ATTENDANCE_DB_PASSWORD", "s3cret");

        DatabaseSettings settings = DatabaseSettings.fromEnvironment(environment, DATA_DIR);

        assertThat(settings).isEqualTo(new DatabaseSettings("jdbc:h2:mem:other", "teacher", "s3cret"));
    }

    @Test
    void blankEnvironmentVariablesFallBackToDefaults() {
        Map<String, String> environment =
                Map.of("ATTENDANCE_DB_URL", "", "ATTENDANCE_DB_USER", " ", "ATTENDANCE_DB_PASSWORD", "");

        DatabaseSettings settings = DatabaseSettings.fromEnvironment(environment, DATA_DIR);

        assertThat(settings).isEqualTo(DatabaseSettings.fromEnvironment(Map.of(), DATA_DIR));
    }

    @Test
    void embeddedUrlUsesForwardSlashesOnly() {
        String url = DatabaseSettings.embeddedUrl(Path.of("C:\\Users\\asha\\AttendanceSystem"));

        assertThat(url).doesNotContain("\\").endsWith("C:/Users/asha/AttendanceSystem/attendance_system");
    }

    @Test
    void relativeDataDirectoryBecomesAbsolute() {
        String url = DatabaseSettings.embeddedUrl(Path.of("demo-home"));

        assertThat(Path.of(url.substring("jdbc:h2:file:".length()))).isAbsolute();
    }

    @Test
    void toStringNeverShowsThePassword() {
        DatabaseSettings settings = new DatabaseSettings("jdbc:h2:mem:x", "admin", "top-secret");

        assertThat(settings.toString()).contains("jdbc:h2:mem:x", "admin").doesNotContain("top-secret");
    }
}
