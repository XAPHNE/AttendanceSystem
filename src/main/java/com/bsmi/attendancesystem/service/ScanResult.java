package com.bsmi.attendancesystem.service;

import com.bsmi.attendancesystem.StudentData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** What a card scan did, to show next to the scan field. */
public record ScanResult(StudentData student, Outcome outcome, LocalDateTime at) {
    private static final DateTimeFormatter HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm");

    public enum Outcome {
        CHECKED_IN("checked in"),
        CHECKED_OUT("checked out"),
        /** The same card again within a few seconds; nothing was changed. */
        REPEATED("was just scanned");

        private final String words;

        Outcome(String words) {
            this.words = words;
        }
    }

    /** @return for example "Asha Verma checked in 09:12" */
    public String message() {
        String name = student.getFirstName() + " " + student.getLastName();
        return outcome == Outcome.REPEATED
                ? name + " " + outcome.words
                : name + " " + outcome.words + " " + at.format(HOUR_MINUTE);
    }
}
