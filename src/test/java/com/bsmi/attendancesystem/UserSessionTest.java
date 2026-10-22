package com.bsmi.attendancesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class UserSessionTest {
    private static final UserData ADMIN = new UserData(1, "admin", "Admin");
    private static final UserData EMPLOYEE = new UserData(2, "employee", "Employee");

    @AfterEach
    void logOut() {
        UserSession.end();
    }

    @Test
    void currentIsTheUserWhoLoggedIn() {
        UserSession.start(EMPLOYEE);

        assertThat(UserSession.current()).isSameAs(EMPLOYEE);
    }

    @Test
    void newLoginReplacesThePreviousUser() {
        UserSession.start(ADMIN);
        UserSession.start(EMPLOYEE);

        assertThat(UserSession.current()).isSameAs(EMPLOYEE);
    }

    @Test
    void currentWithoutLoginFails() {
        assertThatThrownBy(UserSession::current)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Nobody is logged in");
    }

    @Test
    void currentAfterLogoutFails() {
        UserSession.start(ADMIN);
        UserSession.end();

        assertThatThrownBy(UserSession::current).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyAnAdminIsAdmin() {
        UserSession.start(ADMIN);
        assertThat(UserSession.isAdmin()).isTrue();

        UserSession.start(EMPLOYEE);
        assertThat(UserSession.isAdmin()).isFalse();
    }

    @Test
    void nobodyLoggedInIsNotAdmin() {
        assertThat(UserSession.isAdmin()).isFalse();
    }
}
