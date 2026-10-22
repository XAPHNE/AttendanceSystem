package com.bsmi.attendancesystem;

import java.nio.file.Path;
import java.util.Map;

/**
 * Where the database is and how to log in to it. Defaults to an embedded H2 file in the user's
 * data folder; the {@code ATTENDANCE_DB_*} environment variables point the app elsewhere.
 */
public record DatabaseSettings(String url, String user, String password) {
    public static final String URL_VARIABLE = "ATTENDANCE_DB_URL";
    public static final String USER_VARIABLE = "ATTENDANCE_DB_USER";
    public static final String PASSWORD_VARIABLE = "ATTENDANCE_DB_PASSWORD";

    static final String DATABASE_FILE_NAME = "attendance_system";
    static final String DEFAULT_USER = "admin";
    static final String DEFAULT_PASSWORD = "admin";

    public static DatabaseSettings current() {
        return fromEnvironment(System.getenv(), AppPaths.dataDirectory());
    }

    static DatabaseSettings fromEnvironment(Map<String, String> environment, Path dataDirectory) {
        return new DatabaseSettings(
                valueOrDefault(environment.get(URL_VARIABLE), embeddedUrl(dataDirectory)),
                valueOrDefault(environment.get(USER_VARIABLE), DEFAULT_USER),
                valueOrDefault(environment.get(PASSWORD_VARIABLE), DEFAULT_PASSWORD));
    }

    static String embeddedUrl(Path dataDirectory) {
        // H2 accepts forward slashes on every OS; backslashes would need escaping in some tools
        String databaseFile =
                dataDirectory.resolve(DATABASE_FILE_NAME).toAbsolutePath().toString().replace('\\', '/');
        return "jdbc:h2:file:" + databaseFile;
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /** Keeps the password out of logs and error messages. */
    @Override
    public String toString() {
        return "DatabaseSettings[url=" + url + ", user=" + user + "]";
    }
}
