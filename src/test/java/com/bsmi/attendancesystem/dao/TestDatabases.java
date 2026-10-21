package com.bsmi.attendancesystem.dao;

import com.bsmi.attendancesystem.DatabaseMigrations;
import com.bsmi.attendancesystem.DatabaseSettings;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/** Throwaway databases for tests. Each call gets its own, so tests can't affect each other. */
public final class TestDatabases {
    private TestDatabases() {}

    /** @return a new in-memory database with the schema and demo data */
    public static Database migratedInMemory() {
        // DB_CLOSE_DELAY keeps the database alive between the DAO's short-lived connections
        DatabaseSettings settings =
                new DatabaseSettings(
                        "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "admin", "admin");
        DatabaseMigrations.applyPending(settings);
        return new Database(settings);
    }

    /** @return a database whose every connection attempt fails */
    public static Database unreachable() {
        // IFEXISTS=TRUE refuses to create the database, so opening a missing one fails
        return new Database(
                new DatabaseSettings(
                        "jdbc:h2:mem:" + UUID.randomUUID() + ";IFEXISTS=TRUE", "admin", "admin"));
    }

    public static void execute(Database database, String sql) throws SQLException {
        try (Connection connection = database.open();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
