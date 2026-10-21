package com.bsmi.attendancesystem.service;

/** Checks and clean-up shared by the services for text typed into a form. */
final class Fields {
    private Fields() {}

    /** @throws MissingFieldException if the value is null, empty or only spaces */
    static void requireFilled(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new MissingFieldException(fieldName);
        }
    }

    static String trimmedOrEmpty(String value) {
        return value == null ? "" : value.strip();
    }
}
