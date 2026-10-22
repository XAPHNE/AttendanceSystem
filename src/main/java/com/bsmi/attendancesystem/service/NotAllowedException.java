package com.bsmi.attendancesystem.service;

/** The request is valid, but the rules don't allow it, e.g. it would lock people out of the app. */
public class NotAllowedException extends ValidationException {
    public NotAllowedException(String reason) {
        super(reason);
    }
}
