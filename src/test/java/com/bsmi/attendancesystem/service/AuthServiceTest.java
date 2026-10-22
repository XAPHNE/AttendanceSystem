package com.bsmi.attendancesystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bsmi.attendancesystem.UserData;
import com.bsmi.attendancesystem.dao.DataAccessException;
import com.bsmi.attendancesystem.dao.Database;
import com.bsmi.attendancesystem.dao.TestDatabases;
import com.bsmi.attendancesystem.dao.UserDao;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AuthServiceTest {
    private Database database;
    private AuthService auth;

    @BeforeEach
    void useFreshDatabase() {
        database = TestDatabases.migratedInMemory();
        auth = new AuthService(new UserDao(database), new Passwords(4));
    }

    @Test
    void demoAdminLogsInWithTheirRole() {
        assertThat(auth.login("admin", "admin123"))
                .get()
                .extracting(UserData::getUserName, UserData::getUserRole)
                .containsExactly("admin", "Admin");
    }

    @Test
    void demoEmployeeLogsInWithTheirRole() {
        assertThat(auth.login("employee", "employee123"))
                .get()
                .extracting(UserData::getUserRole)
                .isEqualTo("Employee");
    }

    @Test
    void usernameIsTrimmedButPasswordIsNot() {
        assertThat(auth.login("  admin ", "admin123")).isPresent();
        assertThat(auth.login("admin", " admin123")).isEmpty();
    }

    @Test
    void wrongPasswordIsRejected() {
        assertThat(auth.login("admin", "Admin123")).isEmpty();
    }

    @Test
    void unknownUserIsRejected() {
        assertThat(auth.login("nobody", "admin123")).isEmpty();
    }

    @Test
    void injectionInUsernameIsRejected() {
        assertThat(auth.login("admin' OR '1'='1", "anything")).isEmpty();
    }

    @Test
    void corruptStoredHashIsRejectedInsteadOfCrashing() throws SQLException {
        TestDatabases.execute(database, "UPDATE admin SET password_hash = 'not-a-hash'");

        assertThat(auth.login("admin", "admin123")).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankUsernameNamesTheUsernameField(String blank) {
        assertThatThrownBy(() -> auth.login(blank, "admin123"))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the Username field");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankPasswordNamesThePasswordField(String blank) {
        assertThatThrownBy(() -> auth.login("admin", blank))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the Password field");
    }

    @Test
    void unreachableDatabaseIsReported() {
        AuthService broken = new AuthService(new UserDao(TestDatabases.unreachable()));

        assertThatThrownBy(() -> broken.login("admin", "admin123"))
                .isInstanceOf(DataAccessException.class);
    }
}
