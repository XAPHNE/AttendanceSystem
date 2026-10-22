package com.bsmi.attendancesystem.ui;

import java.util.Locale;

final class Displays {
    private Displays() {}

    static boolean available() {
        boolean linux = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("linux");
        // Windows and macOS always have a desktop; on Linux a server or CI runner may have none
        return !linux || System.getenv("DISPLAY") != null || System.getenv("WAYLAND_DISPLAY") != null;
    }
}
