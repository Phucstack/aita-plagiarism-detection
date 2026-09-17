package com.aita.plagiarism.dao;

import com.aita.plagiarism.config.DBContext;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /** Called only after Google token verification. Existing roles are never taken from the client. */
    public User authenticateGoogle(String subject, String verifiedEmail, boolean authoritativeEmail) {
        if (subject == null || subject.isBlank() || subject.length() > 255 || verifiedEmail == null) return null;
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                User user = null;
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WITH (UPDLOCK,HOLDLOCK) WHERE google_subject = ?")) {
                    ps.setString(1, subject);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) user = mapUser(rs); }
                }
                if (user != null) { conn.commit(); return user; }
                if (!authoritativeEmail) { conn.rollback(); return null; }
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WITH (UPDLOCK,HOLDLOCK) WHERE LOWER(email) = LOWER(?)")) {
                    ps.setString(1, verifiedEmail);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getString("google_subject") != null) { conn.rollback(); return null; }
                        user = mapUser(rs);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement("UPDATE Users SET google_subject = ? WHERE user_id = ? AND google_subject IS NULL")) {
                    ps.setString(1, subject); ps.setInt(2, user.getUserId());
                    if (ps.executeUpdate() != 1) { conn.rollback(); return null; }
                }
                conn.commit();
                return user;
            } catch (Exception e) { conn.rollback(); throw e; }
        } catch (Exception e) { throw new DataAccessException(e); }
    }

    /**
     * Xác thực thông tin đăng nhập của người dùng qua JDBC
     * Database failures are propagated to the HTTP boundary.
     */
    public User authenticate(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || rawPassword == null) {
            return null;
        }

        String sql = "SELECT user_id, username, password_hash, full_name, email, role, avatar_url, created_at " +
                     "FROM Users WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usernameOrEmail.trim());
            ps.setString(2, usernameOrEmail.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(rawPassword, storedHash)) {
                        User user = mapUser(rs);
                        if (PasswordUtil.needsUpgrade(storedHash)) {
                            String replacement = PasswordUtil.hashPassword(rawPassword);
                            if (!replacePassword(user.getUserId(), storedHash, replacement)) {
                                // A concurrent login may have upgraded it, or a password change won.
                                try (PreparedStatement latest = conn.prepareStatement("SELECT password_hash FROM Users WHERE user_id = ?")) {
                                    latest.setInt(1, user.getUserId());
                                    try (ResultSet current = latest.executeQuery()) {
                                        if (!current.next() || !PasswordUtil.verifyPassword(rawPassword, current.getString(1))) return null;
                                    }
                                }
                            }
                        }
                        return user;
                    }
                    return null; // Tìm thấy tài khoản trong DB nhưng mật khẩu sai -> từ chối ngay
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = "SELECT user_id, username, full_name, email, role, avatar_url, created_at FROM Users WHERE user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return null;
    }

    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        String sql = "SELECT user_id, username, full_name, email, role, avatar_url, created_at FROM Users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return null;
    }

    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        String sql = "SELECT user_id, username, full_name, email, role, avatar_url, created_at FROM Users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return null;
    }

    /**
     * Tự động tra cứu hoặc khởi tạo tài khoản Google khi người dùng đăng nhập OAuth2
     */
    public User getOrCreateGoogleUser(String email, String fullName, String avatarUrl, String preferredRole) {
        throw new UnsupportedOperationException("Email-only Google provisioning is not supported");
    }

    private User mapUser(ResultSet rs) throws Exception {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }



    public boolean updateProfile(int userId, String fullName, String avatarUrl) {
        String sql = "UPDATE Users SET full_name = ?, avatar_url = ? WHERE user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, avatarUrl != null ? avatarUrl.trim() : "");
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 1024) return false;
        User u = getUserById(userId);
        if (u == null) return false;
        
        // Kiểm tra mật khẩu cũ
        String checkSql = "SELECT password_hash FROM Users WHERE user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String currentHash = rs.getString("password_hash");
                    if (!PasswordUtil.verifyPassword(oldPassword, currentHash)) {
                        return false;
                    }
                    return replacePassword(userId, currentHash, PasswordUtil.hashPassword(newPassword));
                }
            }
            return false;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    private boolean replacePassword(int userId, String expected, String replacement) {
        String sql = "UPDATE Users SET password_hash = ? WHERE user_id = ? AND password_hash COLLATE Latin1_General_100_BIN2 = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, replacement);
            ps.setInt(2, userId);
            ps.setString(3, expected);
            return ps.executeUpdate() == 1;
        } catch (Exception e) { throw new DataAccessException(e); }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, username, full_name, email, role, avatar_url, created_at FROM Users ORDER BY user_id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return list;
    }

}
