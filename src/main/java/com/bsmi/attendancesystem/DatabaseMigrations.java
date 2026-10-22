package com.bsmi.attendancesystem;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

/**
 * Creates or upgrades the database schema by running the SQL files in {@code db/migration} that
 * have not run yet. Flyway records each applied file in the {@code flyway_schema_history} table,
 * so running this on every start is cheap and never applies a file twice.
 */
public final class DatabaseMigrations {
    static final String MIGRATIONS_LOCATION = "classpath:db/migration";

    private DatabaseMigrations() {}

    /**
     * @return how many migration files were applied (0 when the database was already current)
     * @throws DatabaseSetupException if the database can't be opened or a migration fails
     */
    public static int applyPending(DatabaseSettings settings) {
        try {
            return Flyway.configure()
                    .dataSource(settings.url(), settings.user(), settings.password())
                    .locations(MIGRATIONS_LOCATION)
                    .load()
                    .migrate()
                    .migrationsExecuted;
        } catch (FlywayException e) {
            throw new DatabaseSetupException("Could not set up the database at " + settings.url(), e);
        }
    }

    /** The database could not be created or brought up to date, so the app can't start. */
    public static class DatabaseSetupException extends RuntimeException {
        public DatabaseSetupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
