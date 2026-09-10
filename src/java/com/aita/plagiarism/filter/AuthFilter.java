package com.aita.plagiarism.filter;

import com.aita.plagiarism.controller.LoginServlet;
import com.aita.plagiarism.dao.UserDAO;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.util.JWTUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebFilter(urlPatterns = {"/dashboard", "/batch-scanner", "/diff-inspector", "/student-portal"})
public class AuthFilter implements Filter {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        // Nếu chưa có trong session, thử khôi phục từ JWT Token trong Cookie
        if (currentUser == null) {
            String token = extractAuthToken(req);
            if (token != null && JWTUtil.validateToken(token)) {
                Map<String, String> claims = JWTUtil.extractClaims(token);
                if (claims.containsKey("userId")) {
                    int userId = Integer.parseInt(claims.get("userId"));
                    currentUser = userDAO.getUserById(userId);
                    if (currentUser != null) {
                        session.setAttribute("currentUser", currentUser);
                        session.setAttribute("jwtToken", token);
                    }
                }
            }
        }

        // Chưa đăng nhập hoặc Token không hợp lệ -> Chuyển về trang đăng nhập
        if (currentUser == null) {
            res.sendRedirect(req.getContextPath() + "/login?error=unauthorized");
            return;
        }

        String path = req.getServletPath();

        // Phân quyền Role-based Access Control (RBAC)
        if ("STUDENT".equalsIgnoreCase(currentUser.getRole())) {
            // Sinh viên không được phép vào trang quét mã toàn lớp (Batch Scanner)
            if ("/batch-scanner".equals(path)) {
                res.sendRedirect(req.getContextPath() + "/student-portal?error=forbidden");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String extractAuthToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (LoginServlet.AUTH_COOKIE_NAME.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public void destroy() {}
}
