<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Không xử lý được yêu cầu</title>
    <style>
        body{font-family:system-ui,-apple-system,Segoe UI,sans-serif;background:#080911;color:#e2e8f0;margin:0;padding:24px;display:flex;align-items:center;justify-content:center;min-height:100vh}
        main{max-width:520px;border:1px solid #1e293b;border-radius:16px;padding:32px;background:#0e101d}
        h1{font-size:20px;margin:0 0 12px;color:#f8fafc}
        p{line-height:1.6;color:#94a3b8;margin:0 0 16px}
        code{background:#111827;padding:2px 8px;border-radius:6px;color:#22d3ee;font-size:13px}
        a{color:#22d3ee;text-decoration:none}
        a:hover{text-decoration:underline}
    </style>
</head>
<body>
<main>
    <h1>Không xử lý được yêu cầu</h1>
    <p>Mã trạng thái: <code>${pageContext.errorData.statusCode}</code></p>
    <p>
        Nếu bạn cho rằng mình có quyền truy cập nội dung này, hãy đăng nhập lại bằng tài khoản
        có vai trò phù hợp. Chi tiết kỹ thuật của lỗi không được hiển thị để tránh rò rỉ thông tin hệ thống.
    </p>
    <p><a href="${pageContext.request.contextPath}/login">Về trang đăng nhập</a></p>
</main>
</body>
</html>
