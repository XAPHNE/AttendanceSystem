package com.bsmi.attendancesystem.service;

/** The request is valid, but carrying it out would lock people out of the app. */
public class NotAllowedException extends ValidationException {
    public NotAllowedException(String reason) {
        super(reason);
    }
}
