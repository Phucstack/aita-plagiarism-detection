package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.UserDAO;
import com.aita.plagiarism.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller xử lý cập nhật hồ sơ cá nhân và đổi mật khẩu người dùng
 * URL Pattern: /profile-action
 */
@WebServlet("/profile-action")
public class UserProfileActionServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "update_profile": {
                String fullName = request.getParameter("fullName");
                String avatarUrl = request.getParameter("avatarUrl");

                if (avatarUrl != null && !avatarUrl.isBlank()) {
                    try {
                        java.net.URI uri = java.net.URI.create(avatarUrl.trim());
                        if (avatarUrl.length() > 255 || !"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                            response.sendError(400); return;
                        }
                    } catch (IllegalArgumentException e) { response.sendError(400); return; }
                }
                if (fullName != null && !fullName.trim().isEmpty() && fullName.trim().length() <= 100) {
                    boolean ok = userDAO.updateProfile(currentUser.getUserId(), fullName.trim(), avatarUrl);
                    if (ok) {
                        currentUser.setFullName(fullName.trim());
                        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                            currentUser.setAvatarUrl(avatarUrl.trim());
                        }
                        session.setAttribute("currentUser", currentUser);
                    }
                    redirectBack(request, response, currentUser, ok ? "profile_updated" : "profile_failed");
                } else {
                    redirectBack(request, response, currentUser, "missing_name");
                }
                break;
            }

            case "change_password": {
                String oldPass = request.getParameter("oldPassword");
                String newPass = request.getParameter("newPassword");

                if (oldPass != null && newPass != null && newPass.length() >= 8 && newPass.length() <= 1024) {
                    boolean ok = userDAO.changePassword(currentUser.getUserId(), oldPass, newPass);
                    if (ok) {
                        redirectBack(request, response, currentUser, "password_changed");
                    } else {
                        redirectBack(request, response, currentUser, "wrong_old_password");
                    }
                } else {
                    redirectBack(request, response, currentUser, "invalid_password_format");
                }
                break;
            }

            default:
                redirectBack(request, response, currentUser, null);
                break;
        }
    }

    private void redirectBack(HttpServletRequest request, HttpServletResponse response, User user, String msg) 
            throws IOException {
        String target = "STUDENT".equalsIgnoreCase(user.getRole()) ? "/student-portal" : "/dashboard";
        if (msg != null) {
            target += "?profileMsg=" + msg;
        }
        response.sendRedirect(request.getContextPath() + target);
    }
}
