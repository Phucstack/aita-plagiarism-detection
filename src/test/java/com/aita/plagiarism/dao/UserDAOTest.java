package com.aita.plagiarism.dao;

import com.aita.plagiarism.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Dữ liệu Người dùng (UserDAO)")
public class UserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
    }

    @Test
    @DisplayName("Xác thực đăng nhập Giảng viên bằng Username thành công")
    void testAuthenticateInstructorByUsername() {
        User user = userDAO.authenticate("teacher_ha", "123456");
        assertNotNull(user, "Giảng viên phải đăng nhập thành công với username");
        assertEquals("INSTRUCTOR", user.getRole());
        assertTrue(user.getUsername().equalsIgnoreCase("teacher_ha"));
    }

    @Test
    @DisplayName("Xác thực đăng nhập Giảng viên bằng Email giáo dục FPT thành công")
    void testAuthenticateInstructorByEmail() {
        User user = userDAO.authenticate("ha.nh@fpt.edu.vn", "123456");
        assertNotNull(user, "Giảng viên phải đăng nhập thành công với email @fpt.edu.vn");
        assertEquals("INSTRUCTOR", user.getRole());
    }

    @Test
    @DisplayName("Xác thực đăng nhập Sinh viên thành công")
    void testAuthenticateStudent() {
        User user = userDAO.authenticate("phuctv", "123456");
        if (user == null) {
            user = userDAO.authenticate("student_102", "123456");
        }
        assertNotNull(user, "Sinh viên phải đăng nhập thành công");
        assertEquals("STUDENT", user.getRole());
    }

    @Test
    @DisplayName("Từ chối đăng nhập khi sai mật khẩu")
    void testAuthenticateWrongPassword() {
        User user = userDAO.authenticate("teacher_ha", "WrongPassword@2026");
        assertNull(user, "Mật khẩu sai phải trả về null");
    }

    @Test
    @DisplayName("Từ chối đăng nhập khi tài khoản không tồn tại")
    void testAuthenticateNonExistentUser() {
        User user = userDAO.authenticate("ghost_non_existent_user_999", "123456");
        assertNull(user, "Tài khoản không tồn tại phải trả về null");
    }

    @Test
    @DisplayName("Xử lý an toàn khi thông tin đăng nhập null hoặc rỗng")
    void testAuthenticateNullInputs() {
        assertNull(userDAO.authenticate(null, "123456"));
        assertNull(userDAO.authenticate("teacher_ha", null));
        assertNull(userDAO.authenticate(null, null));
    }

    @Test
    @DisplayName("Tìm người dùng theo ID")
    void testGetUserById() {
        User user = userDAO.getUserById(1);
        assertNotNull(user, "Người dùng ID 1 phải tồn tại");
        assertNotNull(user.getUsername());
    }

    @Test
    @DisplayName("Tìm người dùng theo Username không phân biệt hoa thường")
    void testGetUserByUsername() {
        User user = userDAO.getUserByUsername("TEACHER_HA");
        assertNotNull(user);
        assertTrue(user.getUsername().equalsIgnoreCase("teacher_ha"));
    }

    @Test
    @DisplayName("Tìm người dùng theo Email")
    void testGetUserByEmail() {
        User user = userDAO.getUserByEmail("ha.nh@fpt.edu.vn");
        assertNotNull(user);
        assertEquals("ha.nh@fpt.edu.vn", user.getEmail());

        assertNull(userDAO.getUserByEmail(null));
        assertNull(userDAO.getUserByEmail(""));
    }

    @Test
    @DisplayName("Lấy danh sách tất cả người dùng hệ thống")
    void testGetAllUsers() {
        List<User> users = userDAO.getAllUsers();
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Danh sách người dùng không được rỗng");
        assertTrue(users.stream().anyMatch(u -> "INSTRUCTOR".equals(u.getRole())), "Phải có ít nhất 1 giảng viên");
        assertTrue(users.stream().anyMatch(u -> "STUDENT".equals(u.getRole())), "Phải có ít nhất 1 sinh viên");
    }

    @Test
    @DisplayName("Google OAuth: Tự động trả về người dùng sẵn có nếu email đã tồn tại")
    void testGetOrCreateGoogleUserExisting() {
        User existing = userDAO.getOrCreateGoogleUser("ha.nh@fpt.edu.vn", "TS. Nguyễn Hoàng Hà", null, "INSTRUCTOR");
        assertNotNull(existing);
        assertEquals("INSTRUCTOR", existing.getRole());
    }

    @Test
    @DisplayName("Google OAuth: Tự động tạo người dùng mới với role Sinh viên")
    void testGetOrCreateGoogleUserNewStudent() {
        String testEmail = "sv_new_test_unit@fpt.edu.vn";
        User user = userDAO.getOrCreateGoogleUser(testEmail, "Sinh Viên Mới Unit Test", "https://img.jpg", "STUDENT");
        assertNotNull(user);
        assertEquals("STUDENT", user.getRole());
        assertEquals(testEmail, user.getEmail());
    }

    @Test
    @DisplayName("Google OAuth: An toàn khi email đầu vào null hoặc rỗng")
    void testGetOrCreateGoogleUserNullEmail() {
        assertNull(userDAO.getOrCreateGoogleUser(null, "Test", null, "STUDENT"));
        assertNull(userDAO.getOrCreateGoogleUser("  ", "Test", null, "STUDENT"));
    }
}
