package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.UserDAO;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.util.JWTUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    public static final String AUTH_COOKIE_NAME = "AUTH_TOKEN";
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Kiểm tra nếu người dùng đã có token hợp lệ thì chuyển hướng ngay
        String existingToken = extractAuthToken(request);
        if (existingToken != null && JWTUtil.validateToken(existingToken)) {
            HttpSession session = request.getSession();
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser != null && "STUDENT".equalsIgnoreCase(currentUser.getRole())) {
                response.sendRedirect(request.getContextPath() + "/student-portal");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String usernameOrEmail = request.getParameter("email");
        String password = request.getParameter("password");

        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() 
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập đầy đủ tên đăng nhập/email và mật khẩu.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.authenticate(usernameOrEmail.trim(), password);

        if (user == null) {
            request.setAttribute("errorMessage", "Email hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại!");
            request.setAttribute("lastEmail", usernameOrEmail.trim());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        // Tạo JWT Token theo chuẩn RFC 7519 HMAC-SHA256 (Section 4.1.3)
        String jwtToken = JWTUtil.generateToken(user);

        // Lưu Token vào HttpOnly Cookie
        Cookie authCookie = new Cookie(AUTH_COOKIE_NAME, jwtToken);
        authCookie.setHttpOnly(true);
        authCookie.setPath("/");
        authCookie.setMaxAge(24 * 60 * 60); // 1 ngày
        response.addCookie(authCookie);

        // Đồng thời lưu thông tin User vào Session cho JSP EL truy xuất nhanh
        HttpSession session = request.getSession();
        session.setAttribute("currentUser", user);
        session.setAttribute("jwtToken", jwtToken);

        // Điều hướng theo Role
        if ("STUDENT".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/student-portal");
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    private String extractAuthToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (AUTH_COOKIE_NAME.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
