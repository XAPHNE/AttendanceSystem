package com.bsmi.attendancesystem;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Finds the per-user folder where the app keeps its data, following each OS's convention so the
 * app works from any install location without write access to it.
 */
public final class AppPaths {
    /** {@code -Dattendance.home=<folder>} replaces the OS default, e.g. for demos and tests. */
    public static final String HOME_OVERRIDE_PROPERTY = "attendance.home";

    static final String APP_FOLDER_NAME = "AttendanceSystem";

    private AppPaths() {}

    public static Path dataDirectory() {
        return resolveDataDirectory(
                System.getProperty(HOME_OVERRIDE_PROPERTY),
                System.getProperty("os.name"),
                System.getenv(),
                Path.of(System.getProperty("user.home")));
    }

    static Path resolveDataDirectory(
            String homeOverride, String osName, Map<String, String> environment, Path userHome) {
        if (isSet(homeOverride)) {
            return Path.of(homeOverride);
        }
        String os = osName.toLowerCase(Locale.ROOT);
        if (os.startsWith("windows")) {
            String appData = environment.get("APPDATA");
            Path roaming =
                    isSet(appData) ? Path.of(appData) : userHome.resolve("AppData").resolve("Roaming");
            return roaming.resolve(APP_FOLDER_NAME);
        }
        if (os.startsWith("mac")) {
            return userHome.resolve("Library").resolve("Application Support").resolve(APP_FOLDER_NAME);
        }
        // The XDG spec says a relative XDG_DATA_HOME is invalid and must be ignored
        String xdgDataHome = environment.get("XDG_DATA_HOME");
        Path dataHome =
                isSet(xdgDataHome) && Path.of(xdgDataHome).isAbsolute()
                        ? Path.of(xdgDataHome)
                        : userHome.resolve(".local").resolve("share");
        return dataHome.resolve(APP_FOLDER_NAME);
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
