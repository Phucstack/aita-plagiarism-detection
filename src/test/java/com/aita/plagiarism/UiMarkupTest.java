package com.aita.plagiarism;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tĩnh cấu trúc giao diện: liên kết, biểu mẫu, nút, nhãn, thuộc tính alt
 * và khả năng truy cập cơ bản. Không cần trình duyệt hay CSDL.
 */
@DisplayName("Kiểm thử tĩnh giao diện (UI markup)")
class UiMarkupTest {

    /** Tất cả endpoint do servlet cung cấp (đọc từ annotation @WebServlet). */
    private static final Set<String> SERVLET_PATHS = Set.of(
            "login", "logout", "login-google", "dashboard", "course-action", "assignment-action",
            "submit", "submission-action", "batch-scanner", "diff-inspector", "export-report",
            "student-portal", "profile-action");

    private static final Pattern HREF = Pattern.compile("<a\\b[^>]*\\shref=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACTION = Pattern.compile("<form\\b[^>]*\\saction=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG = Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern INPUT = Pattern.compile("<input\\b[^>]*>", Pattern.CASE_INSENSITIVE);

    private static List<Path> viewFiles() throws Exception {
        try (var stream = Files.walk(Paths.get("web"))) {
            return stream.filter(p -> {
                String n = p.toString();
                return (n.endsWith(".jsp") || n.endsWith(".jspf")) && !n.contains("preview");
            }).toList();
        }
    }

    private static String read(Path p) throws Exception {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Mọi liên kết nội bộ phải trỏ tới endpoint hoặc tệp có thật")
    void internalLinksResolve() throws Exception {
        List<String> broken = new ArrayList<>();
        for (Path p : viewFiles()) {
            String html = read(p);
            Matcher m = HREF.matcher(html);
            while (m.find()) {
                String href = m.group(1);
                String target = href.replace("${pageContext.request.contextPath}", "");
                if (target.startsWith("http") || target.startsWith("#") || target.startsWith("mailto:")
                        || target.contains("${") || target.startsWith("javascript:") || target.isEmpty()) {
                    continue;
                }
                target = target.substring(1); // bỏ dấu /
                // Bỏ query string và phần neo (fragment) trước khi kiểm tra đích.
                int cut = target.length();
                for (int i = 0; i < target.length(); i++) {
                    char c = target.charAt(i);
                    if (c == '?' || c == '#') { cut = i; break; }
                }
                String root = target.substring(0, cut);
                if (root.isEmpty()) continue;
                if (SERVLET_PATHS.contains(root)) continue;
                if (root.startsWith("assets/") || root.startsWith("preview/")) {
                    if (!Files.exists(Paths.get("web", root))) broken.add(p + " -> " + href);
                    continue;
                }
                if (root.endsWith(".jsp") || root.endsWith(".html")) {
                    if (!Files.exists(Paths.get("web", root))) broken.add(p + " -> " + href);
                    continue;
                }
                broken.add(p + " -> " + href);
            }
        }
        assertTrue(broken.isEmpty(), "Liên kết gãy: " + broken);
    }

    @Test
    @DisplayName("Mọi form phải có action hợp lệ và một nút gửi")
    void formsHaveValidActionAndSubmit() throws Exception {
        List<String> problems = new ArrayList<>();
        for (Path p : viewFiles()) {
            String html = read(p);
            Matcher m = ACTION.matcher(html);
            while (m.find()) {
                String action = m.group(1).replace("${pageContext.request.contextPath}", "");
                String root = action.startsWith("/") ? action.substring(1) : action;
                root = root.contains("?") ? root.substring(0, root.indexOf('?')) : root;
                if (!SERVLET_PATHS.contains(root) && !root.endsWith(".jsp")) {
                    problems.add(p + " action không hợp lệ: " + action);
                }
            }
            // Nếu có form thì phải có nút submit hoặc button
            if (html.contains("<form") && !html.contains("type=\"submit\"") && !html.contains("<button")) {
                problems.add(p + " có form nhưng không có nút gửi");
            }
        }
        assertTrue(problems.isEmpty(), "Vấn đề form: " + problems);
    }

    @Test
    @DisplayName("Mọi thẻ <img> phải có thuộc tính alt (khả năng truy cập cơ bản)")
    void imagesHaveAltAttribute() throws Exception {
        List<String> missing = new ArrayList<>();
        for (Path p : viewFiles()) {
            String html = read(p);
            Matcher m = IMG.matcher(html);
            while (m.find()) {
                String tag = m.group();
                if (!tag.toLowerCase().contains("alt=")) missing.add(p + " :: " + tag);
            }
        }
        assertTrue(missing.isEmpty(), "Thiếu alt: " + missing);
    }

    @Test
    @DisplayName("Mọi trường input đều có name để servlet có thể đọc được")
    void inputsHaveName() throws Exception {
        List<String> missing = new ArrayList<>();
        for (Path p : viewFiles()) {
            String html = read(p);
            Matcher m = INPUT.matcher(html);
            while (m.find()) {
                String tag = m.group();
                if (tag.toLowerCase().contains("type=\"hidden\"")) continue;
                if (!tag.toLowerCase().contains("name=")) missing.add(p + " :: " + tag);
            }
        }
        assertTrue(missing.isEmpty(), "Input thiếu name: " + missing);
    }

    @Test
    @DisplayName("Trang đăng nhập có đầy đủ trường và liên kết cần thiết")
    void loginPageHasRequiredElements() throws Exception {
        String login = read(Paths.get("web/login.jsp"));
        assertTrue(login.contains("name=\"email\""), "Phải có trường email/username");
        assertTrue(login.contains("name=\"password\""), "Phải có trường mật khẩu");
        assertTrue(login.contains("method=\"POST\""), "Form đăng nhập phải dùng POST");
        assertTrue(login.contains("/login"), "Form phải gửi tới /login");
    }

    @Test
    @DisplayName("Mỗi trang JSP khai báo contentType và pageEncoding UTF-8")
    void pagesDeclareUtf8() throws Exception {
        List<String> problems = new ArrayList<>();
        for (Path p : viewFiles()) {
            String head = read(p);
            if (!head.contains("pageEncoding=\"UTF-8\"")) {
                problems.add(p + " thiếu pageEncoding UTF-8");
            }
        }
        assertTrue(problems.isEmpty(), "Mã hóa không đúng: " + problems);
    }

    @Test
    @DisplayName("Không có tài nguyên cục bộ bị tham chiếu mà thiếu trên đĩa (css/js thường dùng)")
    void referencedStaticAssetsExist() throws Exception {
        Set<String> refs = new LinkedHashSet<>();
        for (Path p : viewFiles()) {
            Matcher m = Pattern.compile("(?:href|src)=\"[^\"]*?/(assets/(?:css|js|models)/[^\"?]+)\"").matcher(read(p));
            while (m.find()) refs.add(m.group(1));
        }
        List<String> missing = new ArrayList<>();
        for (String r : refs) {
            if (!Files.exists(Paths.get("web", r))) missing.add(r);
        }
        assertTrue(missing.isEmpty(), "Tài nguyên thiếu: " + missing);
    }
}
