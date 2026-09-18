package com.aita.plagiarism.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích tính toán mã băm SHA-256 theo chuẩn RFC 6234 (Mục 4.4.2 của đề cương RBL)
 * Đảm bảo tính toàn vẹn (Integrity) và chống chối bỏ (Non-repudiation) của artifact bài nộp.
 *
 * Nguyên tắc: không bao giờ trả về một mã băm "thay thế" khi quá trình băm lỗi.
 * Lỗi băm được ném ra để thao tác nộp bài bị từ chối, thay vì lưu một checksum
 * hợp lệ về mặt định dạng nhưng sai về mặt ý nghĩa (như mã băm của chuỗi rỗng).
 */
public class SHA256ChecksumUtil {

    private static final int BUFFER_SIZE = 8192;

    /**
     * Tính mã băm SHA-256 từ byte stream của file upload.
     *
     * @throws IOException nếu không lấy được thuật toán băm hoặc lỗi đọc luồng
     */
    public static String calculateSHA256(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream must not be null");
        }
        MessageDigest digest = newDigest();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        return bytesToHex(digest.digest());
    }

    /**
     * Tính mã băm SHA-256 từ chuỗi văn bản UTF-8.
     *
     * @throws IOException nếu không lấy được thuật toán băm
     */
    public static String calculateSHA256(String content) throws IOException {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }
        MessageDigest digest = newDigest();
        return bytesToHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Vừa băm vừa ghi xuống đích trong một lượt đọc duy nhất (single pass).
     * Dùng cho tiếp nhận bài nộp: không đọc file hai lần, không cần tệp tạm.
     *
     * @return mã băm SHA-256 (64 ký tự hex) của toàn bộ luồng đã ghi
     * @throws IOException nếu không lấy được thuật toán băm hoặc lỗi I/O;
     *                     khi ném ra, tệp đích có thể đang ghi dở và người gọi
     *                     có trách nhiệm dọn dẹp
     */
    public static String digestAndWrite(InputStream in, OutputStream out) throws IOException {
        if (in == null || out == null) {
            throw new IllegalArgumentException("in and out must not be null");
        }
        MessageDigest digest = newDigest();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
            out.write(buffer, 0, bytesRead);
        }
        return bytesToHex(digest.digest());
    }

    private static MessageDigest newDigest() throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Thuật toán băm SHA-256 không khả dụng", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
