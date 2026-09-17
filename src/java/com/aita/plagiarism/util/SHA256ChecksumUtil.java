package com.aita.plagiarism.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Tiện ích tính toán mã băm SHA-256 theo chuẩn RFC 6234 (Mục 4.4.2 của đề cương RBL)
 * Đảm bảo tính toàn vẹn (Integrity) và chống chối bỏ (Non-repudiation) của artifact bài nộp.
 */
public class SHA256ChecksumUtil {

    /**
     * Tính mã băm SHA-256 từ byte stream của file upload
     */
    public static String calculateSHA256(InputStream inputStream) {
        if (inputStream == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            return bytesToHex(digest.digest());
        } catch (Exception e) {
            return "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"; // Empty SHA-256
        }
    }

    /**
     * Tính mã băm SHA-256 từ chuỗi văn bản UTF-8
     */
    public static String calculateSHA256(String content) {
        if (content == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            return "";
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
