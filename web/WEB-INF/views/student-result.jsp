<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Kết quả đối soát</title>
<style>body{font-family:system-ui;background:#090b16;color:#e2e8f0;margin:0;padding:24px}main{max-width:640px;margin:40px auto;padding:24px;border:1px solid #334155;border-radius:16px}a{color:#22d3ee}p{line-height:1.6}</style></head>
<body><main><h1>Kết quả đối soát bài nộp</h1><p>Báo cáo #<c:out value="${reportId}"/></p>
<p>Độ tương đồng: <strong><c:out value="${score}"/>%</strong></p><p>Thời điểm tạo: <c:out value="${createdAt}"/></p>
<p>Điểm tương đồng là tín hiệu để giảng viên xem xét, không tự kết luận vi phạm. Thông tin và mã nguồn của sinh viên khác được giữ riêng tư.</p>
<a href="${pageContext.request.contextPath}/student-portal">Quay lại bài nộp của tôi</a></main></body></html>
