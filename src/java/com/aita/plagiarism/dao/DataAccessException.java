package com.aita.plagiarism.dao;

/** Persistence failures must never be replaced with demo data. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(Throwable cause) {
        super("Database operation failed", cause);
    }
    public boolean isConflict() {
        return getCause() instanceof java.sql.SQLException
                && (((java.sql.SQLException) getCause()).getErrorCode() == 2601
                    || ((java.sql.SQLException) getCause()).getErrorCode() == 2627
                    || ((java.sql.SQLException) getCause()).getErrorCode() == 547);
    }
}
