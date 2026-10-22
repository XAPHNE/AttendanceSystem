package com.bsmi.attendancesystem.service;

/** A required field was empty or only spaces. */
public class MissingFieldException extends ValidationException {
    private final String fieldName;

    public MissingFieldException(String fieldName) {
        super("Please fill in the " + fieldName + " field");
        this.fieldName = fieldName;
    }

    public String fieldName() {
        return fieldName;
    }
}
