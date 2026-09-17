<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Báo cáo đối soát #${report.reportId}</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/report-view.css"></head>
<body><main><nav><a href="${pageContext.request.contextPath}/dashboard?courseId=${reportCourseId}&amp;assignmentId=${report.assignmentId}">Quay lại bài tập</a><a href="${pageContext.request.contextPath}/logout">Đăng xuất</a></nav>
<section><h1>Báo cáo đối soát #${report.reportId}</h1><p>Độ tương đồng: <strong>${report.similarityScore}%</strong></p>
<p><c:out value="${report.studentAName}"/> ↔ <c:out value="${report.studentBName}"/></p>
<p>Thời điểm tạo: <c:out value="${report.createdAt}"/></p><p>Phân loại đã lưu: <c:out value="${report.riskLevel}"/></p>
<p>Độ tương đồng không tự kết luận đạo văn. Cần xem xét mã khung, yêu cầu bài tập và bối cảnh của từng bài nộp.</p></section>
<section><h2>Ghi chú đối soát đã lưu</h2><p class="preserve"><c:out value="${report.aiAnalysisSummary}" default="Chưa có ghi chú."/></p></section>
<section><h2>Đoạn mã khớp đã lưu</h2><c:if test="${empty matchingBlocks}"><p>Chưa có đoạn mã khớp trong báo cáo này.</p></c:if>
<c:forEach items="${matchingBlocks}" var="b"><article><h3><c:out value="${b.functionName}" default="Đoạn mã"/></h3>
<p>Bài A: dòng ${b.studentAStartLine}–${b.studentAEndLine} · Bài B: dòng ${b.studentBStartLine}–${b.studentBEndLine}</p>
<pre><code><c:out value="${b.matchedCodeSnippet}"/></code></pre><p class="preserve"><c:out value="${b.variableRenamingNotes}"/></p>
</article></c:forEach></section></main></body></html>
