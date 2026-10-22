package com.bsmi.attendancesystem.service;

/** A field was filled in, but with a value the rules don't accept. */
public class InvalidValueException extends ValidationException {
    private final String fieldName;

    /**
     * @param expectation what a valid value looks like, e.g. "a positive whole number"
     */
    public InvalidValueException(String fieldName, String expectation) {
        super(fieldName + " must be " + expectation);
        this.fieldName = fieldName;
    }

    public String fieldName() {
        return fieldName;
    }
}
