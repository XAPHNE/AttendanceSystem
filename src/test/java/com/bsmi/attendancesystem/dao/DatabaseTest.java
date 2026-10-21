package com.bsmi.attendancesystem.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bsmi.attendancesystem.DatabaseSettings;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DatabaseTest {
    @Test
    void openReturnsAWorkingConnection() throws SQLException {
        try (Connection connection = TestDatabases.migratedInMemory().open()) {
            assertThat(connection.isValid(1)).isTrue();
        }
    }

    @Test
    void unreachableDatabaseFailsWithItsUrlAndTheCause() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";IFEXISTS=TRUE";
        Database missing = new Database(new DatabaseSettings(url, "admin", "admin"));

        assertThatThrownBy(missing::open)
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Could not open the database at " + url)
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void errorMessageNeverShowsThePassword() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";IFEXISTS=TRUE";
        Database missing = new Database(new DatabaseSettings(url, "admin", "leaked-secret"));

        assertThatThrownBy(missing::open).message().doesNotContain("leaked-secret");
    }

    @Test
    void unknownDriverUrlFailsWithDataAccessException() {
        Database unknown = new Database(new DatabaseSettings("jdbc:nope:x", "a", "b"));

        assertThatThrownBy(unknown::open).isInstanceOf(DataAccessException.class);
    }
}
