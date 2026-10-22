package com.bsmi.attendancesystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AppPathsTest {
    private static final Path HOME = Path.of("/home/asha");

    @Test
    void overrideWinsOverOsDefault() {
        Path dir = AppPaths.resolveDataDirectory("/tmp/demo", "Linux", Map.of(), HOME);

        assertThat(dir).isEqualTo(Path.of("/tmp/demo"));
    }

    @Test
    void blankOverrideIsIgnored() {
        Path dir = AppPaths.resolveDataDirectory("  ", "Linux", Map.of(), HOME);

        assertThat(dir).isEqualTo(HOME.resolve(".local/share/AttendanceSystem"));
    }

    @Test
    void windowsUsesAppData() {
        Path dir =
                AppPaths.resolveDataDirectory(
                        null, "Windows 11", Map.of("APPDATA", "D:/Roaming"), HOME);

        assertThat(dir).isEqualTo(Path.of("D:/Roaming", "AttendanceSystem"));
    }

    @Test
    void windowsWithoutAppDataFallsBackToRoamingInHome() {
        Path dir = AppPaths.resolveDataDirectory(null, "Windows 10", Map.of(), HOME);

        assertThat(dir).isEqualTo(HOME.resolve("AppData/Roaming/AttendanceSystem"));
    }

    @Test
    void macUsesApplicationSupport() {
        Path dir = AppPaths.resolveDataDirectory(null, "Mac OS X", Map.of(), HOME);

        assertThat(dir).isEqualTo(HOME.resolve("Library/Application Support/AttendanceSystem"));
    }

    @Test
    void linuxUsesAbsoluteXdgDataHome() {
        // "/data/asha" has no drive letter, so it is not absolute when the tests run on Windows
        Path xdgDataHome = Path.of("xdg-data").toAbsolutePath();
        Path dir =
                AppPaths.resolveDataDirectory(
                        null, "Linux", Map.of("XDG_DATA_HOME", xdgDataHome.toString()), HOME);

        assertThat(dir).isEqualTo(xdgDataHome.resolve("AttendanceSystem"));
    }

    @Test
    void linuxIgnoresRelativeXdgDataHome() {
        Path dir =
                AppPaths.resolveDataDirectory(
                        null, "Linux", Map.of("XDG_DATA_HOME", "relative/dir"), HOME);

        assertThat(dir).isEqualTo(HOME.resolve(".local/share/AttendanceSystem"));
    }

    @Test
    void unknownOsUsesTheLinuxConvention() {
        Path dir = AppPaths.resolveDataDirectory(null, "FreeBSD", Map.of(), HOME);

        assertThat(dir).isEqualTo(HOME.resolve(".local/share/AttendanceSystem"));
    }

    @Test
    void dataDirectoryReadsTheOverrideProperty() {
        String previous = System.getProperty(AppPaths.HOME_OVERRIDE_PROPERTY);
        System.setProperty(AppPaths.HOME_OVERRIDE_PROPERTY, "/srv/attendance");
        try {
            assertThat(AppPaths.dataDirectory()).isEqualTo(Path.of("/srv/attendance"));
        } finally {
            if (previous == null) {
                System.clearProperty(AppPaths.HOME_OVERRIDE_PROPERTY);
            } else {
                System.setProperty(AppPaths.HOME_OVERRIDE_PROPERTY, previous);
            }
        }
    }
}
