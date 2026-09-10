package com.aita.plagiarism.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp quản lý kết nối Cơ sở dữ liệu SQL Server cho AITA Ecosystem
 * Tương thích tiêu chuẩn PRJ301 (JDBC Driver)
 * Tự động hỗ trợ cả Named Instance (localhost\\SQL2019) và Default Port 1433
 */
public class DBContext {
    private static final String SERVER_NAME = System.getProperty("DB_SERVER", 
            System.getenv("DB_SERVER") != null ? System.getenv("DB_SERVER") : "localhost\\SQL2019");
    private static final String PORT = System.getProperty("DB_PORT", 
            System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "1433");
    private static final String DB_NAME = System.getProperty("DB_NAME", 
            System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "AITA_PlagiarismDB");
    private static final String USERNAME = System.getProperty("DB_USER", 
            System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "sa");
    private static final String PASSWORD = System.getProperty("DB_PASSWORD", 
            System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "123456");

    /**
     * Mở kết nối tới SQL Server
     */
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        String url;
        if (SERVER_NAME.contains("\\")) {
            url = String.format("jdbc:sqlserver://%s;databaseName=%s;encrypt=true;trustServerCertificate=true;loginTimeout=5;",
                    SERVER_NAME, DB_NAME);
        } else {
            url = String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true;loginTimeout=5;",
                    SERVER_NAME, PORT, DB_NAME);
        }

        try {
            return DriverManager.getConnection(url, USERNAME, PASSWORD);
        } catch (SQLException ex) {
            // Fallback sang localhost:1433 nếu named instance thất bại (khi chạy trên môi trường khác)
            if (SERVER_NAME.contains("SQL2019")) {
                try {
                    String fallbackUrl = String.format("jdbc:sqlserver://localhost:1433;databaseName=%s;encrypt=true;trustServerCertificate=true;loginTimeout=5;", DB_NAME);
                    return DriverManager.getConnection(fallbackUrl, USERNAME, PASSWORD);
                } catch (SQLException ignored) {}
            }
            throw ex;
        }
    }
}
