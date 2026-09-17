package com.aita.plagiarism.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBContext {
    private DBContext() {}
    private static String config(String name, String fallback) {
        return System.getProperty(name, System.getenv().getOrDefault(name, fallback));
    }
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String url = config("DB_URL", "");
        if (url.isBlank()) {
            String server = config("DB_SERVER", "localhost");
            String endpoint = server.contains("\\") ? server : server + ":" + config("DB_PORT", "1433");
            url = "jdbc:sqlserver://" + endpoint + ";databaseName=" + config("DB_NAME", "AITA_PlagiarismDB")
                    + ";encrypt=true;trustServerCertificate=" + config("DB_TRUST_SERVER_CERTIFICATE", "false")
                    + ";loginTimeout=5;";
        }
        String user = config("DB_USER", "");
        String password = config("DB_PASSWORD", "");
        if (user.isBlank() || password.isBlank()) throw new SQLException("DB_USER and DB_PASSWORD are required");
        return DriverManager.getConnection(url, user, password);
    }
}
