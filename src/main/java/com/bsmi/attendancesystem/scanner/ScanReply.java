package com.bsmi.attendancesystem.scanner;

/** What the app made of one scan, shown on the phone: "Asha Verma checked in 09:12" or why not. */
public record ScanReply(boolean ok, String message) {
    public static ScanReply ok(String message) {
        return new ScanReply(true, message);
    }

    public static ScanReply failed(String message) {
        return new ScanReply(false, message);
    }
}
