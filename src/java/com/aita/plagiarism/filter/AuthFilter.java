package com.aita.plagiarism.filter;

import com.aita.plagiarism.controller.LoginServlet;
import com.aita.plagiarism.dao.DataAccessException;
import com.aita.plagiarism.dao.UserDAO;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.util.JWTUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@WebFilter("/*")
public class AuthFilter implements Filter {
    private final UserDAO userDAO = new UserDAO();
    private static final Set<String> PUBLIC = Set.of("", "/", "/index.jsp", "/login", "/login.jsp",
            "/login-google", "/logout", "/favicon.ico");
    private static final Set<String> STAFF = Set.of("/dashboard", "/dashboard.jsp",
            "/batch-scanner", "/batch-scanner.jsp",
            "/course-action", "/assignment-action");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader("Cache-Control", "no-store");
        try {
            String path = req.getServletPath();
            if (PUBLIC.contains(path) || path.startsWith("/assets/") || path.startsWith("/preview/")) {
                chain.doFilter(request, response);
                return;
            }
            String token = null;
            if (req.getCookies() != null) for (Cookie cookie : req.getCookies()) {
                if (LoginServlet.AUTH_COOKIE_NAME.equals(cookie.getName())) token = cookie.getValue();
            }
            Map<String, String> claims = JWTUtil.extractClaims(token);
            User user = claims.isEmpty() ? null : userDAO.getUserById(Integer.parseInt(claims.get("userId")));
            if (user != null && tokenRevoked(claims, user)) {
                // Mật khẩu đã đổi sau khi token được phát hành -> thu hồi phiên như token hết hạn.
                user = null;
            }
            if (user == null) {
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();
                res.sendRedirect(req.getContextPath() + "/login?error=unauthorized");
                return;
            }
            if (STAFF.contains(path) && !Set.of("ADMIN", "INSTRUCTOR").contains(user.getRole())) {
                res.sendError(403);
                return;
            }
            if (!"GET".equals(req.getMethod()) && !"HEAD".equals(req.getMethod())) {
                String site = req.getHeader("Sec-Fetch-Site");
                String origin = req.getHeader("Origin");
                String expected = req.getScheme() + "://" + req.getServerName()
                        + ((req.getServerPort() == 80 && "http".equals(req.getScheme()))
                        || (req.getServerPort() == 443 && "https".equals(req.getScheme())) ? "" : ":" + req.getServerPort());

                // Chỉ chấp nhận khi CÓ BẰNG CHỨNG rõ ràng về cùng nguồn.
                // Nếu cả Origin lẫn Sec-Fetch-Site đều vắng mặt (trình khách cũ hoặc
                // không phải trình duyệt), không thể chứng minh cùng nguồn -> từ chối.
                boolean provenSameOrigin = "same-origin".equals(site) || "none".equals(site);
                boolean originMatches = origin != null && expected.equals(origin);

                if ("cross-site".equals(site) || (!provenSameOrigin && !originMatches)) {
                    res.sendError(403);
                    return;
                }
            }
            if (path.startsWith("/uploads/")) { res.sendError(403); return; }
            if (Set.of("/dashboard.jsp", "/batch-scanner.jsp", "/diff-inspector.jsp", "/student-portal.jsp").contains(path)) {
                String query = req.getQueryString();
                res.sendRedirect(req.getContextPath() + path.substring(0, path.length() - 4)
                        + (query == null ? "" : "?" + query));
                return;
            }
            HttpSession session = req.getSession(true);
            session.setMaxInactiveInterval(1800);
            session.setAttribute("currentUser", user);
            session.setAttribute("jwtToken", token);
            chain.doFilter(request, response);
        } catch (DataAccessException e) {
            res.setStatus(e.isConflict() ? 409 : 503);
            res.setContentType("text/plain;charset=UTF-8");
            res.getWriter().write(e.isConflict()
                    ? "Dữ liệu bị trùng hoặc đang được tham chiếu. Thao tác chưa được lưu."
                    : "Không thể kết nối hoặc thực hiện thao tác với cơ sở dữ liệu. Vui lòng thử lại.");
        } catch (IllegalStateException e) {
            res.setStatus(503);
            res.setContentType("text/plain;charset=UTF-8");
            res.getWriter().write("Cấu hình xác thực chưa sẵn sàng.");
        }
    }

    /**
     * Token bị thu hồi nếu JWT iat (epoch giây) nhỏ hơn thời điểm đổi mật khẩu gần nhất.
     * Không có dấu thời gian đổi mật khẩu (NULL) hoặc claim iat không đọc được -> không thu hồi
     * (token vẫn bị ràng buộc bởi exp như trước).
     *
     * <p>Public để {@code LoginServlet} dùng chung: trang login phải bỏ qua token đã thu hồi
     * thay vì chuyển hướng theo nó (tránh vòng lặp redirect vô hạn).</p>
     */
    public static boolean tokenRevoked(Map<String, String> claims, User user) {
        java.sql.Timestamp changedAt = user.getPasswordChangedAt();
        if (changedAt == null) return false;
        String iatRaw = claims.get("iat");
        if (iatRaw == null) return false;
        try {
            long iatSeconds = Long.parseLong(iatRaw.trim());
            return iatSeconds * 1000L < changedAt.getTime();
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
