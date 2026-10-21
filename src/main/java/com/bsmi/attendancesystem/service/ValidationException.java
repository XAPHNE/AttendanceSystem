package com.bsmi.attendancesystem.service;

/**
 * The user asked for something the rules don't allow. The message is written for the user, so the
 * UI can show it as it is.
 */
public abstract class ValidationException extends RuntimeException {
    protected ValidationException(String userMessage) {
        super(userMessage);
    }
}
