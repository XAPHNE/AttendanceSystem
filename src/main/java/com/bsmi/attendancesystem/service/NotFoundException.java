package com.bsmi.attendancesystem.service;

/** The thing to change or delete is not stored (anymore). */
public class NotFoundException extends ValidationException {
    public NotFoundException(String what) {
        super(what + " does not exist");
    }
}
