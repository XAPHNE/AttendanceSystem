package com.bsmi.attendancesystem.service;

/** A card was scanned that no student has. */
public class UnknownCardException extends ValidationException {
    public UnknownCardException(String cardId) {
        super("No student has card " + cardId);
    }
}
