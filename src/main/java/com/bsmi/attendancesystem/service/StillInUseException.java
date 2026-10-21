package com.bsmi.attendancesystem.service;

/** Deleting this would leave other records pointing at nothing. */
public class StillInUseException extends ValidationException {
    public StillInUseException(String what, String usedBy) {
        super(what + " can't be deleted while " + usedBy + " use it");
    }
}
