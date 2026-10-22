package com.bsmi.attendancesystem.service;

import org.mindrot.jbcrypt.BCrypt;

/** Hashes passwords with bcrypt and checks typed passwords against stored hashes. */
final class Passwords {
    /** Each extra round doubles the work, which slows down guessing a stolen hash. */
    static final int DEFAULT_ROUNDS = 12;

    private final int rounds;

    Passwords() {
        this(DEFAULT_ROUNDS);
    }

    /** @param rounds bcrypt's cost factor; tests use a low one to stay fast */
    Passwords(int rounds) {
        this.rounds = rounds;
    }

    /** @return a hash that includes its own random salt, so equal passwords hash differently */
    String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(rounds));
    }

    /** @return false also when the stored value is missing or not a bcrypt hash */
    boolean matches(String password, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException | IndexOutOfBoundsException notABcryptHash) {
            return false;
        }
    }
}
