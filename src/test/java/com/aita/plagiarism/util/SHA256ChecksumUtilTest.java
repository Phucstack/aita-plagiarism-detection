package com.aita.plagiarism.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tiện ích băm SHA-256. Không cần CSDL.
 */
class SHA256ChecksumUtilTest {

    /** SHA-256 của chuỗi rỗng — giá trị từng bị dùng làm "mã băm thay thế". */
    private static final String EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Test
    @DisplayName("Mã băm đúng 64 ký tự hex và khớp giá trị chuẩn")
    void hashesKnownValue() throws IOException {
        String hash = SHA256ChecksumUtil.calculateSHA256("abc");
        assertEquals(64, hash.length());
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hash);
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    @DisplayName("digestAndWrite băm và ghi trong một lượt, cho cùng kết quả với băm riêng")
    void digestAndWriteMatchesSeparateHash() throws IOException {
        byte[] payload = "public class A { int x = 1; }".repeat(500).getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String writtenHash = SHA256ChecksumUtil.digestAndWrite(new ByteArrayInputStream(payload), out);

        assertArrayEquals(payload, out.toByteArray());
        assertEquals(SHA256ChecksumUtil.calculateSHA256(new ByteArrayInputStream(payload)), writtenHash);
    }

    @Test
    @DisplayName("Lỗi đọc luồng phải ném ra, không được trả mã băm của chuỗi rỗng")
    void ioFailureThrowsInsteadOfReturningEmptyHash() {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("disk failure");
            }
        };
        assertThrows(IOException.class, () -> SHA256ChecksumUtil.calculateSHA256(broken));
        assertThrows(IOException.class,
                () -> SHA256ChecksumUtil.digestAndWrite(broken, new ByteArrayOutputStream()));
    }

    @Test
    @DisplayName("Mã băm của chuỗi rỗng không bao giờ được sinh ra như một giá trị hợp lệ thay thế")
    void emptyHashIsNotUsedAsFallback() throws IOException {
        // Băm chuỗi rỗng vẫn là kết quả hợp lệ khi đầu vào THỰC SỰ rỗng...
        assertEquals(EMPTY_SHA256, SHA256ChecksumUtil.calculateSHA256(""));
        // ...nhưng không phải là giá trị nhận được khi có lỗi (xem test ioFailureThrows...).
        assertNotEquals(EMPTY_SHA256, SHA256ChecksumUtil.calculateSHA256("anything"));
    }

    @Test
    @DisplayName("Đầu vào null bị từ chối thay vì trả về chuỗi rỗng")
    void nullInputRejected() {
        assertThrows(IllegalArgumentException.class, () -> SHA256ChecksumUtil.calculateSHA256((String) null));
        assertThrows(IllegalArgumentException.class, () -> SHA256ChecksumUtil.calculateSHA256((InputStream) null));
    }
}
