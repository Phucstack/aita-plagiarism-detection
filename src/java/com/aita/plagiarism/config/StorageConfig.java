package com.aita.plagiarism.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Vị trí lưu trữ artifact bài nộp, nằm NGOÀI web root.
 *
 * Thứ tự ưu tiên:
 * <ol>
 *   <li>System property {@code AITA_UPLOAD_DIR}</li>
 *   <li>Biến môi trường {@code AITA_UPLOAD_DIR}</li>
 *   <li>{@code ${catalina.base}/aita-uploads} (khi chạy trong Tomcat)</li>
 *   <li>{@code ${java.io.tmpdir}/aita-uploads} (chạy rời, ví dụ khi test)</li>
 * </ol>
 *
 * CSDL lưu đường dẫn TƯƠNG ĐỐI (tên tệp an toàn đã được đặt lại).
 * Các bản ghi cũ lưu đường dẫn tuyệt đối hoặc đường dẫn kiểu {@code /uploads/...}
 * vẫn được đọc được nhờ {@link #resolve(String)} — không cần di chuyển dữ liệu.
 */
public final class StorageConfig {

    private StorageConfig() {}

    public static Path baseDir() {
        String configured = System.getProperty("AITA_UPLOAD_DIR");
        if (isBlank(configured)) configured = System.getenv("AITA_UPLOAD_DIR");
        if (!isBlank(configured)) return Paths.get(configured.trim()).toAbsolutePath().normalize();

        String catalinaBase = System.getProperty("catalina.base");
        if (!isBlank(catalinaBase)) {
            return Paths.get(catalinaBase.trim()).resolve("aita-uploads").toAbsolutePath().normalize();
        }
        return Paths.get(System.getProperty("java.io.tmpdir")).resolve("aita-uploads")
                .toAbsolutePath().normalize();
    }

    /**
     * Tạo (và đảm bảo tồn tại) tệp đích cho một tên tệp an toàn.
     *
     * @param safeName tên tệp đã được đặt lại, không chứa thành phần đường dẫn
     */
    public static File newTarget(String safeName) {
        if (isBlank(safeName)) throw new IllegalArgumentException("safeName must not be blank");
        Path base = baseDir();
        try {
            Files.createDirectories(base);
        } catch (Exception e) {
            throw new IllegalStateException("Không tạo được thư mục lưu trữ bài nộp: " + base, e);
        }
        return base.resolve(safeName).normalize().toFile();
    }

    /**
     * Ánh xạ giá trị {@code Submissions.file_path} thành tệp trên đĩa.
     *
     * @return tệp tương ứng, hoặc {@code null} nếu bản ghi không có đường dẫn.
     *         Tệp chưa chắc đã tồn tại — người gọi phải kiểm tra.
     */
    public static File resolve(String storedPath) {
        if (isBlank(storedPath)) return null;

        Path raw = Paths.get(storedPath);
        if (raw.isAbsolute() && Files.exists(raw)) return raw.normalize().toFile();

        String unix = storedPath.replace('\\', '/');
        String relative = unix.replaceFirst("^/+", "");
        Path inBase = baseDir().resolve(relative).normalize();
        if (Files.exists(inBase)) return inBase.toFile();

        // Bản ghi cũ kiểu /uploads/sub_01/OrderManager.java: thử tìm theo tên tệp.
        int slash = unix.lastIndexOf('/');
        if (slash >= 0) {
            String name = unix.substring(slash + 1);
            if (!name.isEmpty()) {
                Path byName = baseDir().resolve(name).normalize();
                if (Files.exists(byName)) return byName.toFile();
            }
        }
        return inBase.toFile();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
