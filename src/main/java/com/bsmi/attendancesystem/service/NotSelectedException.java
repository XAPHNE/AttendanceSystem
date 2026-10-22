package com.bsmi.attendancesystem.service;

/** The action works on a row of a table, but no row was chosen. */
public class NotSelectedException extends ValidationException {
    public NotSelectedException(String what) {
        super("Please choose a " + what + " in the table first");
    }
}
