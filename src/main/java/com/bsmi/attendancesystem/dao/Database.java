package com.bsmi.attendancesystem.dao;

import com.bsmi.attendancesystem.DatabaseSettings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens connections to the database described by {@link DatabaseSettings}. */
public final class Database {
    private final DatabaseSettings settings;

    public Database(DatabaseSettings settings) {
        this.settings = settings;
    }

    public static Database fromCurrentSettings() {
        return new Database(DatabaseSettings.current());
    }

    /**
     * @return a new connection; the caller closes it (use try-with-resources)
     * @throws DataAccessException if the database can't be reached
     */
    public Connection open() {
        try {
            return DriverManager.getConnection(
                    settings.url(), settings.user(), settings.password());
        } catch (SQLException e) {
            throw new DataAccessException("open the database at " + settings.url(), e);
        }
    }
}
