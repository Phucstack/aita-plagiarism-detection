package com.aita.plagiarism.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử ánh xạ đường dẫn lưu trữ bài nộp. Không cần CSDL.
 */
class StorageConfigTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("AITA_UPLOAD_DIR được ưu tiên làm thư mục gốc")
    void baseDirUsesConfiguredValue() {
        System.setProperty("AITA_UPLOAD_DIR", tempDir.toString());
        try {
            assertEquals(tempDir.toAbsolutePath().normalize(), StorageConfig.baseDir());
        } finally {
            System.clearProperty("AITA_UPLOAD_DIR");
        }
    }

    @Test
    @DisplayName("Đường dẫn tương đối được giải trong thư mục gốc cấu hình")
    void resolvesRelativePathInsideBaseDir() {
        System.setProperty("AITA_UPLOAD_DIR", tempDir.toString());
        try {
            File target = StorageConfig.newTarget("sub_1_123_Order.java");
            assertEquals(tempDir.resolve("sub_1_123_Order.java").toFile(), target);
            assertTrue(target.getParentFile().isDirectory());

            File resolved = StorageConfig.resolve("sub_1_123_Order.java");
            assertNotNull(resolved);
            assertEquals(target, resolved);
        } finally {
            System.clearProperty("AITA_UPLOAD_DIR");
        }
    }

    @Test
    @DisplayName("Bản ghi cũ kiểu /uploads/... được tìm theo tên tệp")
    void resolvesLegacyUploadsPathByName() throws Exception {
        System.setProperty("AITA_UPLOAD_DIR", tempDir.toString());
        try {
            File legacy = tempDir.resolve("OrderManager_PhucTV.java").toFile();
            assertTrue(legacy.createNewFile());

            File resolved = StorageConfig.resolve("/uploads/sub_01/OrderManager_PhucTV.java");
            assertNotNull(resolved);
            assertEquals(legacy, resolved);
        } finally {
            System.clearProperty("AITA_UPLOAD_DIR");
        }
    }

    @Test
    @DisplayName("Đường dẫn trống trả về null thay vì ném lỗi")
    void blankPathReturnsNull() {
        assertNull(StorageConfig.resolve(null));
        assertNull(StorageConfig.resolve("   "));
    }
}
