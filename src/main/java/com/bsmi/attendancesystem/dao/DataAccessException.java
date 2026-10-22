package com.bsmi.attendancesystem.dao;

import java.sql.SQLException;

/**
 * A database call failed. The message says what the app was trying to do; the {@link SQLException}
 * with the database's own reason is kept as the cause.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String failedOperation, SQLException cause) {
        super("Could not " + failedOperation, cause);
    }
}
