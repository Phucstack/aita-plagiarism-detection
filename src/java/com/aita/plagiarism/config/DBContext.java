package com.aita.plagiarism.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Nguồn kết nối CSDL dùng connection pool (HikariCP) — đáp ứng NFR-PERF-03.
 *
 * Mỗi DAO vẫn tự mở và đóng {@link Connection}, nhưng giờ đây thao tác đó chỉ là
 * mượn/trả một kết nối đã có sẵn trong pool thay vì thiết lập kết nối TCP mới.
 * Đo thực tế: quét 50 bài nộp giảm từ ~10,9s xuống mức ghi nhận trong TEST_REPORT.md.
 */
public final class DBContext {

    private DBContext() {}

    private static volatile HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    private static String config(String name, String fallback) {
        return System.getProperty(name, System.getenv().getOrDefault(name, fallback));
    }

    private static HikariDataSource dataSource() {
        if (dataSource == null) {
            synchronized (LOCK) {
                if (dataSource == null) {
                    dataSource = new HikariDataSource(buildConfig());
                }
            }
        }
        return dataSource;
    }

    private static HikariConfig buildConfig() {
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
        if (user.isBlank() || password.isBlank()) {
            throw new IllegalStateException("DB_USER and DB_PASSWORD are required");
        }

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(url);
        hikari.setUsername(user);
        hikari.setPassword(password);
        hikari.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        hikari.setPoolName("aita-pool");
        hikari.setMaximumPoolSize(intConfig("DB_POOL_MAX", 10));
        hikari.setMinimumIdle(intConfig("DB_POOL_MIN_IDLE", 2));
        // Các lượt quét trên cùng một bài tập bị nối tiếp hoá bởi khoá UPDLOCK, nên khi
        // nhiều request đến cùng lúc chúng sẽ xếp hàng chờ kết nối. Ngưỡng 10 giây từng
        // làm request nhận HTTP 503 khi có 16 lượt quét đồng thời; nâng lên 30 giây để
        // hàng đợi được xử lý hết thay vì bị từ chối.
        hikari.setConnectionTimeout(longConfig("DB_POOL_CONN_TIMEOUT_MS", 30_000L));
        hikari.setIdleTimeout(longConfig("DB_POOL_IDLE_TIMEOUT_MS", 600_000L));
        hikari.setMaxLifetime(longConfig("DB_POOL_MAX_LIFETIME_MS", 1_800_000L));
        return hikari;
    }

    private static int intConfig(String name, int fallback) {
        String raw = System.getProperty(name, System.getenv(name));
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long longConfig(String name, long fallback) {
        String raw = System.getProperty(name, System.getenv(name));
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** Mượn một kết nối từ pool. Người gọi có trách nhiệm đóng (tức là trả về pool). */
    public static Connection getConnection() throws SQLException {
        return dataSource().getConnection();
    }

    /** Đóng pool — dùng khi tắt ứng dụng hoặc trong kiểm thử. */
    public static void close() {
        synchronized (LOCK) {
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
        }
    }
}
