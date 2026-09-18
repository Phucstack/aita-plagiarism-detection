<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AITA Student Portal - Tra Cứu Bài Nộp & Liêm Chính</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: { deepBg: '#090a15', cardBg: '#121422', neonCyan: '#06b6d4', neonViolet: '#8b5cf6', neonAmber: '#f59e0b', neonRose: '#f43f5e' }
                }
            }
        }
    </script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/liquid-glass-2026.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/aita-copilot.css?v=2.0">
    <!-- Chế độ Tắt Toàn Bộ Hiệu Ứng (Mặc định: TẮT sau khi đăng nhập) -->
    <script>
        (function() {
            var fx = localStorage.getItem('aita_effects_enabled');
            if (fx !== 'true') {
                document.documentElement.classList.add('effects-disabled');
            } else {
                document.documentElement.classList.add('effects-enabled');
            }
        })();
    </script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/data-views.css">
</head>
<body class="data-view cinema-grain bg-[#090a15] text-slate-200 min-h-screen flex flex-col font-sans relative">
    <canvas id="cyber-canvas"></canvas>
    <div class="cyber-aurora-bg">
        <div class="aurora-blob-1"></div>
        <div class="aurora-blob-2"></div>
    </div>

    <!-- Header Navigation -->
    <header class="h-16 border-b border-white/10 bg-[#0c0e1d]/90 backdrop-blur-md px-5 flex items-center justify-between sticky top-0 z-50">
        <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-xl bg-gradient-to-tr from-cyan-500 to-violet-600 flex items-center justify-center shadow-[0_0_20px_rgba(6,182,212,0.35)]">
                <i data-lucide="shield-check" class="w-5 h-5 text-white"></i>
            </div>
            <div>
                <span class="text-base font-extrabold tracking-wider text-white">AITA</span>
                <span class="text-xs font-bold text-cyan-400 font-mono">StudentPortal</span>
                <span class="text-[10px] font-mono px-1.5 py-0.5 rounded bg-cyan-950/60 text-cyan-400 border border-cyan-500/30 ml-2">PRJ301</span>
            </div>
        </div>

        <div class="flex items-center gap-4">
            <!-- Visual Effects Toggle Button (Mặc định: TẮT sau khi đăng nhập) -->
            <button onclick="CyberEffects.toggle()" class="effects-hud-toggle px-2.5 py-1.5 rounded-xl border border-white/10 bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white transition-all flex items-center gap-1.5 text-xs font-mono shrink-0" title="Bật/Tắt Toàn Bộ Hiệu Ứng (Mặc định: TẮT)">
                <i data-lucide="zap-off" class="w-3.5 h-3.5 text-amber-400"></i>
                <span class="hidden sm:inline text-[11px]">Hiệu ứng: <span class="effects-status-val font-bold text-amber-400">TẮT</span></span>
            </button>

            <!-- Cyber Audio HUD Toggle Button -->
            <button onclick="CyberAudio.toggle()" class="audio-hud-toggle p-2 rounded-xl border border-white/10 bg-white/5 hover:bg-cyan-500/10 hover:border-cyan-500/40 text-slate-400 hover:text-cyan-300 transition-all flex items-center justify-center shrink-0" title="Bật/Tắt Âm Thanh Tương Tác HUD">
                <i data-lucide="volume-x" class="w-4 h-4"></i>
            </button>

            <!-- Command Palette Trigger Button (Ctrl+K) -->
            <button onclick="window.commandPalette && window.commandPalette.open()" class="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-xl border border-white/10 bg-white/5 hover:bg-cyan-500/10 hover:border-cyan-500/40 text-slate-400 hover:text-cyan-300 transition-all text-xs font-mono group" title="Tìm kiếm & Điều hướng nhanh (Ctrl+K)">
                <i data-lucide="search" class="w-3.5 h-3.5 text-cyan-400 group-hover:scale-110 transition-transform"></i>
                <span class="hidden md:inline">Tìm kiếm...</span>
                <span class="px-1.5 py-0.5 rounded bg-black/40 border border-white/10 text-[10px] text-slate-400">Ctrl K</span>
            </button>

            <div class="flex items-center gap-2.5 pl-3 border-l border-white/10">
                <img src="${fn:escapeXml(sessionScope.currentUser.avatarUrl != null ? sessionScope.currentUser.avatarUrl : 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80')}" alt="Avatar" class="w-8 h-8 rounded-full border border-cyan-400/60 object-cover cursor-pointer" onclick="openProfileModal()">
                <div class="text-left leading-tight hidden sm:block text-xs cursor-pointer" onclick="openProfileModal()">
                    <div class="font-semibold text-white hover:text-cyan-300 transition-colors">${fn:escapeXml(sessionScope.currentUser.fullName != null ? sessionScope.currentUser.fullName : 'Trần Văn Long (SE1701)')}</div>
                    <div class="text-[10px] text-cyan-400 font-mono">${fn:escapeXml(sessionScope.currentUser.email != null ? sessionScope.currentUser.email : 'longtvse1701@fpt.edu.vn')}</div>
                </div>
                <button type="button" onclick="openProfileModal()" class="ml-1 text-slate-400 hover:text-cyan-300 transition-colors p-1.5 rounded-lg bg-white/5" title="Cài đặt tài khoản &amp; Đổi mật khẩu">
                    <i data-lucide="user-cog" class="w-4 h-4"></i>
                </button>
                <a href="${pageContext.request.contextPath}/logout" class="ml-1 p-1.5 rounded-lg bg-white/5 hover:bg-rose-500/20 text-slate-400 hover:text-rose-400 border border-white/5 transition-all" title="Đăng xuất">
                    <i data-lucide="log-out" class="w-4 h-4"></i>
                </a>
            </div>
        </div>
    </header>

    <!-- Main Content -->
    <main class="flex-1 max-w-6xl w-full mx-auto px-4 py-6 space-y-6 relative z-10">
        <!-- Top Banner -->
        <div class="p-5 rounded-2xl liquid-glass-card border border-cyan-500/20 shadow-lg flex flex-col md:flex-row items-start md:items-center justify-between gap-4 specular-rim-border hud-corner-box">
            <div>
                <div class="inline-flex items-center gap-2 px-2.5 py-0.5 rounded-full bg-cyan-500/10 border border-cyan-500/30 text-cyan-300 text-[11px] font-mono mb-2">
                    <span class="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-ping"></span>
                    <span>Bài nộp và kết quả của tôi</span>
                </div>
                <h1 class="text-xl font-bold text-white">Cổng Tra Cứu Liêm Chính & Bài Nộp PRJ301</h1>
                <p class="text-xs text-slate-400 mt-1">Theo dõi bài nộp và kết quả đối soát được giảng viên thực hiện.</p>
            </div>

        </div>

        <section class="p-4 rounded-xl border border-white/10"><h2 class="text-sm text-slate-400">Số bài nộp của tôi</h2><strong id="my-submission-count" class="text-2xl">${submissionCount}</strong></section>
        <!-- Ingestion / Submission Section (Full CRUD for Student) -->
        <div class="rounded-2xl bg-[#0f1122]/90 border border-cyan-500/30 p-5 shadow-xl liquid-glass-card specular-rim-border">
            <div class="flex items-center justify-between pb-3 mb-4 border-b border-white/10">
                <div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-xl bg-cyan-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-400">
                        <i data-lucide="upload-cloud" class="w-5 h-5"></i>
                    </div>
                    <div>
                        <h2 class="text-sm font-bold text-white font-mono">Nộp Mã Nguồn Bài Tập</h2>
                        <p class="text-[11px] text-slate-400">Tự động tính mã băm SHA-256 byte stream và phân tích cây cú pháp AST</p>
                    </div>
                </div>
                <span class="text-[10px] font-mono px-2.5 py-1 rounded bg-cyan-950/60 border border-cyan-500/30 text-cyan-300">
                    Định dạng hỗ trợ: .java, .txt, .docx, .zip (Max 25MB)
                </span>
            </div>

            <form action="${pageContext.request.contextPath}/submit" method="POST" enctype="multipart/form-data" class="grid grid-cols-1 md:grid-cols-12 gap-4 items-end text-xs font-mono">
                <div class="md:col-span-4">
                    <label class="block text-slate-300 mb-1.5 font-semibold">Chọn bài tập nộp*</label>
                    <select name="assignmentId" required class="w-full px-3 py-2 rounded-xl bg-[#141628] border border-white/10 text-white focus:border-cyan-400 outline-none cursor-pointer">
                        <c:forEach var="a" items="${assignments}">
                            <option value="${a.assignmentId}"><c:out value="${a.title}"/></option>
                        </c:forEach>
                        <c:if test="${empty assignments}">
                            <option value="">Chưa có bài tập</option>
                        </c:if>
                    </select>
                </div>
                <div class="md:col-span-5">
                    <label class="block text-slate-300 mb-1.5 font-semibold">Chọn tệp mã nguồn*</label>
                    <input aria-label="Tệp bài nộp" type="file" name="file" accept=".java,.txt,.docx,.zip" required class="w-full px-3 py-1.5 rounded-xl bg-[#141628] border border-white/10 text-slate-300 file:mr-3 file:py-1 file:px-3 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-cyan-500/20 file:text-cyan-300 hover:file:bg-cyan-500/30 cursor-pointer focus:border-cyan-400 outline-none">
                </div>
                <div class="md:col-span-3">
                    <button type="submit" class="w-full btn-shimmer py-2 px-4 rounded-xl bg-gradient-to-r from-cyan-500 to-violet-600 hover:from-cyan-400 hover:to-violet-500 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-[0_0_15px_rgba(6,182,212,0.3)] transition-all">
                        <i data-lucide="send" class="w-4 h-4"></i>
                        <span>Nộp Bài &amp; Băm SHA</span>
                    </button>
                </div>
            </form>
        </div>

        <!-- Submissions Table -->
        <div class="rounded-2xl bg-[#0f1122]/90 border border-white/10 shadow-xl overflow-hidden">
            <div class="px-5 py-4 border-b border-white/10 flex items-center justify-between">
                <h3 class="text-sm font-bold text-white flex items-center gap-2">
                    <i data-lucide="layers" class="w-4 h-4 text-cyan-400"></i>
                    Lịch Sử Bài Nộp &amp; Kết Quả Phân Tích
                </h3>
                <span class="text-[11px] font-mono text-slate-400">Bài nộp của tài khoản hiện tại</span>
            </div>

            <div class="overflow-x-auto">
                <table class="w-full text-left text-xs">
                    <thead class="bg-white/5 text-slate-400 uppercase font-mono text-[10px]">
                        <tr>
                            <th class="py-3 px-4">Môn / Bài Nộp</th>
                            <th class="py-3 px-4">Tập Tin Nộp</th>
                            <th class="py-3 px-4">Mã Băm SHA-256</th>
                            <th class="py-3 px-4">Thời Gian Nộp</th>
                            <th class="py-3 px-4 text-center">Trạng thái lưu trữ</th>
                            <th class="py-3 px-4 text-right">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-white/5 font-sans">
                        <c:choose>
                            <c:when test="${not empty mySubmissions}">
                                <c:forEach items="${mySubmissions}" var="sub">
                                    <tr class="hover:bg-white/[0.02] transition-colors">
                                        <td class="py-3.5 px-4 font-medium text-white">
                                            PRJ301
                                            <span class="block text-[10px] text-slate-400">Mã bài: #${sub.submissionId}</span>
                                        </td>
                                        <td class="py-3.5 px-4 font-mono text-cyan-300 font-medium"><c:out value="${sub.fileName}"/></td>
                                        <td class="py-3.5 px-4 font-mono text-[11px] text-slate-400" title="${sub.sha256Hash}">
                                            <c:choose>
                                                <c:when test="${not empty sub.sha256Hash && sub.sha256Hash.length() > 16}">
                                                    ${sub.sha256Hash.substring(0, 7)}...${sub.sha256Hash.substring(sub.sha256Hash.length() - 8)}
                                                </c:when>
                                                <c:otherwise>
                                                    ${sub.sha256Hash}
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="py-3.5 px-4 text-slate-400 font-mono">${sub.submittedAt}</td>
                                        <td class="py-3.5 px-4 text-center">
                                            <c:choose>
                                                <c:when test="${sub.status == 'FLAGGED'}">
                                                    <span class="px-2.5 py-1 rounded-full bg-rose-500/10 border border-rose-500/30 text-rose-300 text-[11px] font-bold inline-flex items-center gap-1.5">
                                                        <span class="w-1.5 h-1.5 rounded-full bg-rose-400 animate-pulse"></span>
                                                        FLAGGED
                                                    </span>
                                                </c:when>
                                                <c:when test="${sub.status == 'PENDING'}">
                                                    <span class="px-2.5 py-1 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-300 text-[11px] font-bold inline-flex items-center gap-1.5">
                                                        <span class="w-1.5 h-1.5 rounded-full bg-amber-400"></span>
                                                        CHỜ ĐỐI SOÁT
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="px-2.5 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-[11px] font-bold inline-flex items-center gap-1.5">
                                                        <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
                                                        HỢP LỆ (SAFE)
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="py-3.5 px-4 text-right">
                                            <div class="flex items-center justify-end gap-2">

                                                <form action="${pageContext.request.contextPath}/submission-action" method="POST" class="inline m-0 p-0" onsubmit="return confirm('Bạn có chắc chắn muốn xóa bài nộp này? File lưu trữ sẽ bị gỡ bỏ khỏi hệ thống!');">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="submissionId" value="${sub.submissionId}">
                                                    <button type="submit" class="p-1 rounded-lg hover:bg-rose-500/20 text-slate-400 hover:text-rose-400 transition-colors" title="Xóa bài nộp">
                                                        <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise><tr><td colspan="6" class="p-5 text-slate-400" id="submissions-empty">Bạn chưa có bài nộp.</td></tr></c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <section class="p-5 rounded-xl border border-white/10"><h2 class="text-sm font-bold mb-3">Kết quả đối soát của tôi</h2>
            <c:if test="${empty myResults}"><p class="text-slate-400">Chưa có kết quả đối soát.</p></c:if>
            <ul><c:forEach items="${myResults}" var="r"><li class="py-2"><a class="text-cyan-300 underline" href="${pageContext.request.contextPath}/diff-inspector?reportId=${r.reportId}">Báo cáo #${r.reportId} · ${r.similarityScore}% · <c:out value="${r.createdAt}"/></a></li></c:forEach></ul>
            <p class="text-xs text-slate-400 mt-4">Gửi giải trình trực tuyến chưa được triển khai. Liên hệ giảng viên nếu cần xem xét kết quả.</p>
        </section>
    </main>

    <!-- Student Code Inspection Modal (Đối soát chi tiết vi phạm cho Sinh viên) -->
    <div id="modal-user-profile" class="ast-modal-backdrop" onclick="if(event.target === this) closeProfileModal()">
        <div class="ast-modal-card p-6 border border-cyan-500/40 relative max-w-lg w-full liquid-glass-card specular-rim-border">
            <button type="button" onclick="closeProfileModal()" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
            <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-xl bg-cyan-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-400">
                    <i data-lucide="user-check" class="w-5 h-5"></i>
                </div>
                <div>
                    <h3 class="text-base font-bold text-white font-mono">Hồ Sơ &amp; Bảo Mật</h3>
                    <p class="text-xs text-slate-400">Cập nhật thông tin sinh viên &amp; bảo mật tài khoản</p>
                </div>
            </div>

            <!-- Tab 1: Profile Form -->
            <form action="${pageContext.request.contextPath}/profile-action" method="POST" class="space-y-3 text-xs font-mono pb-4 mb-4 border-b border-white/10">
                <input type="hidden" name="action" value="update_profile">
                <div class="font-bold text-cyan-400 text-xs uppercase tracking-wider mb-2">Thông Tin Cá Nhân</div>
                <div>
                    <label class="block text-slate-300 mb-1">Họ và tên</label>
                    <input aria-label="Họ và tên" type="text" name="fullName" value="${fn:escapeXml(sessionScope.currentUser.fullName)}" required class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-cyan-400 outline-none">
                </div>
                <div>
                    <label class="block text-slate-300 mb-1">URL Ảnh Đại Diện</label>
                    <input aria-label="URL ảnh đại diện" type="text" name="avatarUrl" value="${fn:escapeXml(sessionScope.currentUser.avatarUrl)}" placeholder="https://..." class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-cyan-400 outline-none">
                </div>
                <div class="flex justify-end">
                    <button type="submit" class="px-3.5 py-1.5 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-black font-bold text-xs">Cập Nhật Hồ Sơ</button>
                </div>
            </form>

            <!-- Tab 2: Password Form -->
            <form action="${pageContext.request.contextPath}/profile-action" method="POST" class="space-y-3 text-xs font-mono">
                <input type="hidden" name="action" value="change_password">
                <div class="font-bold text-rose-400 text-xs uppercase tracking-wider mb-2">Đổi Mật Khẩu</div>
                <div>
                    <label class="block text-slate-300 mb-1">Mật khẩu hiện tại</label>
                    <input aria-label="Mật khẩu hiện tại" type="password" name="oldPassword" required class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-rose-400 outline-none">
                </div>
                <div>
                    <label class="block text-slate-300 mb-1">Mật khẩu mới (tối thiểu 6 ký tự)</label>
                    <input aria-label="Mật khẩu mới" type="password" name="newPassword" minlength="8" required class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-rose-400 outline-none">
                </div>
                <div class="flex justify-end">
                    <button type="submit" class="px-3.5 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs">Đổi Mật Khẩu</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        window.AITA_USER_ROLE = 'STUDENT';
        function openProfileModal() {
            const m = document.getElementById('modal-user-profile');
            if (m) m.classList.add('active');
            if (window.CyberAudio) CyberAudio.playTick();
        }
        function closeProfileModal() {
            const m = document.getElementById('modal-user-profile');
            if (m) m.classList.remove('active');
        }
        function openStudentInspectModal() {
            const m = document.getElementById('student-inspect-modal');
            if (m) m.classList.add('active');
        }
        function closeStudentInspectModal() {
            const m = document.getElementById('student-inspect-modal');
            if (m) m.classList.remove('active');
        }
        function proceedToAppealFromInspect() {
            closeStudentInspectModal();
            openAppealModal();
        }
        function openAppealModal() {
            const m = document.getElementById('appeal-modal');
            if (m) m.classList.add('active');
        }
        function closeAppealModal() {
            const m = document.getElementById('appeal-modal');
            if (m) m.classList.remove('active');
        }
        function handleAppealSubmit(e) {
            e.preventDefault();
            closeAppealModal();
            if (window.showToast) {
                window.showToast("Đơn giải trình #DP-2026 đã được gửi tới hội đồng khảo thí!", "success");
            } else {
                alert("Đơn giải trình #DP-2026 đã được gửi thành công!");
            }
        }

        // Toast notifications for Student Portal
        window.addEventListener('DOMContentLoaded', () => {
            const params = new URLSearchParams(window.location.search);
            if (window.showToast) {
                if (params.get('submitSuccess') === 'true') {
                    showToast('Nộp bài thành công! Đã băm mã SHA-256 an toàn.', 'success');
                }
                if (params.get('subMsg') === 'deleted') {
                    showToast('Đã xóa bài nộp thành công khỏi hệ thống!', 'info');
                }
                if (params.get('submitError')) {
                    showToast('Lỗi khi nộp bài: File không hợp lệ hoặc quá dung lượng (25MB)!', 'error');
                }
                if (params.get('subError')) {
                    showToast('Không thể xóa bài nộp hoặc bạn không có quyền!', 'error');
                }
                if (params.get('appealSuccess') === 'true') {
                    showToast('Đơn giải trình đã được chuyển tiếp tới Hội đồng Khảo thí.', 'success');
                }
                if (params.get('profileMsg') === 'profile_updated') {
                    showToast('Cập nhật thông tin cá nhân thành công!', 'success');
                }
                if (params.get('profileMsg') === 'password_changed') {
                    showToast('Đổi mật khẩu thành công!', 'success');
                }
                if (params.get('profileMsg') === 'invalid_password_format') showToast('Mật khẩu mới cần từ 8 đến 1024 ký tự.', 'error');
                if (params.get('profileMsg') === 'wrong_old_password') {
                    showToast('Mật khẩu hiện tại không chính xác!', 'error');
                }
            }
        });

        lucide.createIcons();
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-effects.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-audio.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-particles.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/tilt-motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/system-modals.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion-system-2026.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/command-palette.js"></script>
    <!-- AITA Realtime AI Copilot (ZeroTTS Voice Engine) -->
</body>
</html>
