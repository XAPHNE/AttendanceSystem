package com.bsmi.attendancesystem.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordsTest {
    private final Passwords passwords = new Passwords(4);

    @Test
    void hashMatchesTheOriginalPassword() {
        String hash = passwords.hash("Secret123");

        assertThat(passwords.matches("Secret123", hash)).isTrue();
    }

    @Test
    void hashDoesNotMatchAnotherPassword() {
        String hash = passwords.hash("Secret123");

        assertThat(passwords.matches("secret123", hash)).isFalse();
    }

    @Test
    void hashIsNotThePasswordItself() {
        assertThat(passwords.hash("Secret123")).doesNotContain("Secret123").startsWith("$2a$04$");
    }

    @Test
    void samePasswordHashesDifferentlyEachTime() {
        assertThat(passwords.hash("Secret123")).isNotEqualTo(passwords.hash("Secret123"));
    }

    @Test
    void defaultUsesTwelveRounds() {
        assertThat(new Passwords().hash("x")).startsWith("$2a$12$");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"plain-text", "$2a$12$tooShort", "$9z$12$abcdefghijklmnopqrstuv"})
    void storedValueThatIsNotABcryptHashNeverMatches(String corruptHash) {
        assertThat(passwords.matches("anything", corruptHash)).isFalse();
    }
}
