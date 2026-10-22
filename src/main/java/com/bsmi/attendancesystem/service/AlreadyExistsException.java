package com.bsmi.attendancesystem.service;

/** Something with the same unique name or number is already stored. */
public class AlreadyExistsException extends ValidationException {
    public AlreadyExistsException(String what) {
        super(what + " already exists");
    }
}
