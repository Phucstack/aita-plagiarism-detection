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
</head>
<body class="cinema-grain bg-[#090a15] text-slate-200 min-h-screen flex flex-col font-sans relative">
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
                <img src="${sessionScope.currentUser.avatarUrl != null ? sessionScope.currentUser.avatarUrl : 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80'}" alt="Avatar" class="w-8 h-8 rounded-full border border-cyan-400/60 object-cover cursor-pointer" onclick="openProfileModal()">
                <div class="text-left leading-tight hidden sm:block text-xs cursor-pointer" onclick="openProfileModal()">
                    <div class="font-semibold text-white hover:text-cyan-300 transition-colors">${sessionScope.currentUser.fullName != null ? sessionScope.currentUser.fullName : 'Trần Văn Long (SE1701)'}</div>
                    <div class="text-[10px] text-cyan-400 font-mono">${sessionScope.currentUser.email != null ? sessionScope.currentUser.email : 'longtvse1701@fpt.edu.vn'}</div>
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
                    <span>École 42 Peer-Review Ecosystem</span>
                </div>
                <h1 class="text-xl font-bold text-white">Cổng Tra Cứu Liêm Chính & Bài Nộp PRJ301</h1>
                <p class="text-xs text-slate-400 mt-1">Mọi bài nộp đều được băm SHA-256 và đối soát cây cú pháp AST tự động.</p>
            </div>
            <div class="flex items-center gap-3">
                <div class="px-4 py-2.5 rounded-xl bg-violet-950/40 border border-violet-500/30 text-center shadow-lg">
                    <div class="text-[11px] text-slate-400 font-mono">Điểm Sửa Lỗi (Correction Points)</div>
                    <div class="text-xl font-bold font-mono text-violet-300">5 Pts</div>
                </div>
            </div>
        </div>

        <!-- Metric Grid -->
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div class="p-4 rounded-xl liquid-glass-card border border-white/10 shadow specular-rim-border">
                <div class="flex items-center justify-between text-xs text-slate-400 mb-1 font-mono">
                    <span>Số bài tập đã nộp</span>
                    <i data-lucide="file-check" class="w-4 h-4 text-cyan-400"></i>
                </div>
                <div class="text-2xl font-extrabold font-mono text-white">01</div>
                <span class="text-[10px] text-emerald-400 font-mono">100% đúng hạn</span>
            </div>
            <div class="p-4 rounded-xl liquid-glass-card border border-white/10 shadow specular-rim-border">
                <div class="flex items-center justify-between text-xs text-slate-400 mb-1 font-mono">
                    <span>Xác thực SHA-256</span>
                    <i data-lucide="fingerprint" class="w-4 h-4 text-emerald-400"></i>
                </div>
                <div class="text-2xl font-extrabold font-mono text-emerald-400">VERIFIED</div>
                <span class="text-[10px] text-slate-400 font-mono">Toàn vẹn mã nguồn 4.4.2</span>
            </div>
            <div id="metric-card-ast" class="p-4 rounded-xl liquid-glass-card border border-rose-500/30 shadow specular-rim-border transition-all">
                <div class="flex items-center justify-between text-xs text-slate-400 mb-1 font-mono">
                    <span>Cảnh báo AST Gemini</span>
                    <i id="metric-ast-icon" data-lucide="alert-triangle" class="w-4 h-4 text-rose-400"></i>
                </div>
                <div id="metric-ast-status" class="text-2xl font-extrabold font-mono text-rose-400">FLAGGED</div>
                <span id="metric-ast-sub" class="text-[10px] text-rose-300 font-mono">88.5% tương đồng logic</span>
            </div>
        </div>

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
                            <option value="${a.assignmentId}">${a.title}</option>
                        </c:forEach>
                        <c:if test="${empty assignments}">
                            <option value="2">Assignment 2 - E-Commerce Cart &amp; Payment</option>
                        </c:if>
                    </select>
                </div>
                <div class="md:col-span-5">
                    <label class="block text-slate-300 mb-1.5 font-semibold">Chọn tệp mã nguồn*</label>
                    <input type="file" name="file" accept=".java,.txt,.docx,.zip" required class="w-full px-3 py-1.5 rounded-xl bg-[#141628] border border-white/10 text-slate-300 file:mr-3 file:py-1 file:px-3 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-cyan-500/20 file:text-cyan-300 hover:file:bg-cyan-500/30 cursor-pointer focus:border-cyan-400 outline-none">
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
                <span class="text-[11px] font-mono text-slate-400">Mã Lớp: PRJ301 (Fall 2026)</span>
            </div>

            <div class="overflow-x-auto">
                <table class="w-full text-left text-xs">
                    <thead class="bg-white/5 text-slate-400 uppercase font-mono text-[10px]">
                        <tr>
                            <th class="py-3 px-4">Môn / Bài Nộp</th>
                            <th class="py-3 px-4">Tập Tin Nộp</th>
                            <th class="py-3 px-4">Mã Băm SHA-256</th>
                            <th class="py-3 px-4">Thời Gian Nộp</th>
                            <th class="py-3 px-4 text-center">Trạng Thái AI</th>
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
                                        <td class="py-3.5 px-4 font-mono text-cyan-300 font-medium">${sub.fileName}</td>
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
                                                        FLAGGED (88.5%)
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
                                                <button type="button" onclick="openStudentInspectModal()" class="px-2.5 py-1 rounded-lg bg-cyan-950/60 hover:bg-cyan-900/60 border border-cyan-500/30 text-cyan-300 text-xs font-medium inline-flex items-center gap-1 transition-all" title="Xem chi tiết đối soát">
                                                    <i data-lucide="eye" class="w-3.5 h-3.5"></i> Đối Soát
                                                </button>
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
                            <c:otherwise>
                                <tr class="hover:bg-white/[0.02] transition-colors">
                                    <td class="py-3.5 px-4 font-medium text-white">
                                        PRJ301 - Assignment 2
                                        <span class="block text-[10px] text-slate-400">E-Commerce Cart &amp; Payment</span>
                                    </td>
                                    <td class="py-3.5 px-4 font-mono text-cyan-300">OrderManager_LongTV.java</td>
                                    <td class="py-3.5 px-4 font-mono text-[11px] text-slate-400" title="d7a8fbb307d7809469ca933b02dd32f974ddb16f5f785228a076d9cfac42a458">
                                        d7a8fbb...ac42a458
                                    </td>
                                    <td class="py-3.5 px-4 text-slate-400 font-mono">2026-10-30 21:15</td>
                                    <td id="submission-status-cell" class="py-3.5 px-4 text-center">
                                        <span class="px-2.5 py-1 rounded-full bg-rose-500/10 border border-rose-500/30 text-rose-300 text-[11px] font-bold inline-flex items-center gap-1.5">
                                            <span class="w-1.5 h-1.5 rounded-full bg-rose-400 animate-pulse"></span>
                                            FLAGGED (88.5%)
                                        </span>
                                    </td>
                                    <td class="py-3.5 px-4 text-right">
                                        <button type="button" onclick="openStudentInspectModal()" class="px-3 py-1.5 rounded-lg bg-cyan-950/60 hover:bg-cyan-900/60 border border-cyan-500/30 text-cyan-300 text-xs font-medium inline-flex items-center gap-1.5 transition-all">
                                            <i data-lucide="eye" class="w-3.5 h-3.5"></i> Xem Đối Soát
                                        </button>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Appeal Dispute Workflow Box -->
        <div class="p-5 rounded-xl bg-[#0b0c16]/90 border border-amber-500/30 text-xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-[0_0_20px_rgba(245,158,11,0.08)]">
            <div class="space-y-1.5 max-w-2xl">
                <div class="flex items-center gap-2 text-amber-400 font-bold">
                    <i data-lucide="message-square-warning" class="w-4 h-4"></i>
                    Quy trình Phản biện Khiếu nại (AI Appeal Dispute Resolver - Nhóm 7)
                </div>
                <p class="text-slate-400 leading-relaxed text-[11px]">
                    Nếu bạn cho rằng độ tương đồng mã nguồn xuất phát từ mã khung (Boilerplate Code) hoặc cấu trúc bắt buộc của đề bài, bạn có thể gửi đơn giải trình trực tiếp kèm giải thích logic AST để hội đồng khảo thí xem xét.
                </p>
            </div>
            <button onclick="openAppealModal()" class="px-4 py-2 rounded-xl bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/40 text-amber-300 font-mono text-xs font-semibold shrink-0 transition-all flex items-center gap-2 shadow-[0_0_15px_rgba(245,158,11,0.15)]">
                <i data-lucide="file-pen-line" class="w-3.5 h-3.5"></i> Gửi Giải Trình
            </button>
        </div>
    </main>

    <!-- Student Code Inspection Modal (Đối soát chi tiết vi phạm cho Sinh viên) -->
    <div id="student-inspect-modal" class="ast-modal-backdrop" onclick="if(event.target === this) closeStudentInspectModal()">
        <div class="ast-modal-card p-6 border border-cyan-500/40 relative max-w-3xl shadow-[0_0_50px_rgba(6,182,212,0.2)]">
            <button onclick="closeStudentInspectModal()" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
            
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-white/10 mb-4">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-xl bg-cyan-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-400 shadow-[0_0_15px_rgba(6,182,212,0.3)]">
                        <i data-lucide="scan-search" class="w-5 h-5"></i>
                    </div>
                    <div>
                        <h3 class="text-base font-bold text-white font-mono flex items-center gap-2">
                            Đối Soát Mã Nguồn &amp; Cây Cú Pháp AST
                            <span class="px-2 py-0.5 rounded text-[10px] bg-rose-950 text-rose-300 border border-rose-500/40 font-bold">88.5% Trùng Lặp</span>
                        </h3>
                        <p class="text-xs text-slate-400 font-mono">Tập tin: <span class="text-cyan-300">OrderManager_LongTV.java</span> • PRJ301 Assignment 2</p>
                    </div>
                </div>
                <div class="flex items-center gap-2">
                    <span class="text-[10px] font-mono px-2.5 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/30 text-cyan-300">Sinh viên: Trần Văn Long</span>
                </div>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-12 gap-4 text-xs font-mono mb-4">
                <!-- Left: Code snippet with highlighted duplicate lines -->
                <div class="lg:col-span-7 bg-[#070810] border border-white/10 rounded-xl p-3 overflow-hidden">
                    <div class="flex items-center justify-between text-[11px] text-slate-400 pb-2 mb-2 border-b border-white/5">
                        <span class="flex items-center gap-1.5"><i data-lucide="code-2" class="w-3.5 h-3.5 text-cyan-400"></i> Đoạn mã bị hệ thống gắn cờ</span>
                        <span class="text-[10px] text-rose-400 bg-rose-950/60 px-2 py-0.5 rounded border border-rose-500/30">Khối AST #1 (Dòng 115 - 122)</span>
                    </div>
                    <div class="space-y-1 font-mono text-[11px] leading-relaxed select-text overflow-x-auto max-h-[220px]">
                        <div class="text-slate-500 flex"><span class="w-7 select-none text-slate-600">114</span><span>&nbsp;&nbsp;&nbsp;&nbsp;// Xử lý cập nhật giỏ hàng và trừ kho</span></div>
                        <div class="text-rose-300 bg-rose-950/40 border-l-2 border-rose-500 pl-1 flex"><span class="w-7 select-none text-rose-400/70">115</span><span>&nbsp;&nbsp;&nbsp;&nbsp;public boolean updateStock(List&lt;Item&gt; items) {</span></div>
                        <div class="text-rose-300 bg-rose-950/40 border-l-2 border-rose-500 pl-1 flex"><span class="w-7 select-none text-rose-400/70">116</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for (Item it : items) {</span></div>
                        <div class="text-rose-300 bg-rose-950/40 border-l-2 border-rose-500 pl-1 flex"><span class="w-7 select-none text-rose-400/70">117</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;int current = db.getQuantity(it.getId());</span></div>
                        <div class="text-rose-300 bg-rose-950/40 border-l-2 border-rose-500 pl-1 flex"><span class="w-7 select-none text-rose-400/70">118</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (current &lt; it.getAmount()) return false;</span></div>
                        <div class="text-rose-300 bg-rose-950/40 border-l-2 border-rose-500 pl-1 flex"><span class="w-7 select-none text-rose-400/70">119</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;db.update(it.getId(), current - it.getAmount());</span></div>
                        <div class="text-rose-300 bg-rose-950/40 border-l-2 border-rose-500 pl-1 flex"><span class="w-7 select-none text-rose-400/70">120</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                        <div class="text-rose-300 bg-rose-950/40 border-l-2 border-rose-500 pl-1 flex"><span class="w-7 select-none text-rose-400/70">121</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return true;</span></div>
                        <div class="text-rose-300 bg-rose-950/40 border-l-2 border-rose-500 pl-1 flex"><span class="w-7 select-none text-rose-400/70">122</span><span>&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                    </div>
                </div>

                <!-- Right: Analysis from Gemini AI & Rubric -->
                <div class="lg:col-span-5 flex flex-col justify-between gap-3">
                    <div class="p-3.5 rounded-xl bg-[#131526] border border-white/5 space-y-2">
                        <div class="flex items-center gap-2 text-violet-400 font-bold text-xs">
                            <i data-lucide="sparkles" class="w-4 h-4 text-cyan-400"></i>
                            <span>Phân Tích AI &amp; AST Gemini</span>
                        </div>
                        <p class="text-[11px] text-slate-300 leading-relaxed font-sans">
                            Cấu trúc Cây Cú Pháp Trừu Tượng (AST) của phương thức <code class="text-violet-300 font-mono">updateStock</code> và <code class="text-violet-300 font-mono">processPayment</code> trùng khớp 100% về luồng điều khiển với bài nộp đối chứng trong cơ sở dữ liệu.
                        </p>
                        <div class="pt-2 border-t border-white/5 text-[10px] text-slate-400 font-mono space-y-1">
                            <div>• Biến đổi tên: <span class="text-cyan-300">_cart &rarr; _basket</span>, <span class="text-cyan-300">total_amt &rarr; final_cost</span></div>
                            <div>• Mức độ tương đồng logic: <span class="text-rose-400 font-bold">88.5% (High Risk)</span></div>
                        </div>
                    </div>

                    <div class="p-3 rounded-xl bg-amber-500/10 border border-amber-500/30 text-[11px] text-amber-300/90 font-sans leading-relaxed">
                        <strong class="text-amber-300 block mb-0.5">Quyền lợi của sinh viên:</strong>
                        Nếu đoạn mã trên là mã khung (boilerplate) của giảng viên hoặc cấu trúc bắt buộc của đề bài, bạn có quyền gửi giải trình để Hội đồng Khảo thí xem xét lại.
                    </div>
                </div>
            </div>

            <!-- Modal Footer Actions -->
            <div class="pt-3 border-t border-white/10 flex flex-wrap items-center justify-between gap-3">
                <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1&role=student" class="text-xs text-slate-400 hover:text-cyan-300 font-mono flex items-center gap-1.5 transition-colors">
                    <i data-lucide="external-link" class="w-3.5 h-3.5"></i> Mở Trình So Soát Toàn Màn Hình
                </a>
                <div class="flex items-center gap-2">
                    <button type="button" onclick="closeStudentInspectModal()" class="px-3.5 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white text-xs font-mono transition-all">
                        Đóng
                    </button>
                    <button type="button" onclick="proceedToAppealFromInspect()" class="btn-shimmer px-4 py-1.5 rounded-lg bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-black font-bold text-xs font-mono flex items-center gap-1.5 shadow-[0_0_15px_rgba(245,158,11,0.3)] transition-all">
                        <i data-lucide="message-square" class="w-3.5 h-3.5"></i> Gửi Đơn Giải Trình Khiếu Nại
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Appeal Dispute Modal -->
    <div id="appeal-modal" class="ast-modal-backdrop" onclick="if(event.target === this) closeAppealModal()">
        <div class="ast-modal-card p-6 border border-amber-500/40 relative max-w-xl">
            <button onclick="closeAppealModal()" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
            <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400 shadow-[0_0_15px_rgba(245,158,11,0.2)]">
                    <i data-lucide="shield-alert" class="w-5 h-5"></i>
                </div>
                <div>
                    <h3 class="text-base font-bold text-white font-mono flex items-center gap-2">
                        Đơn Giải Trình Liêm Chính Học Thuật
                        <span class="px-2 py-0.5 rounded text-[10px] bg-amber-950 text-amber-400 border border-amber-500/30">#DP-2026</span>
                    </h3>
                    <p class="text-xs text-slate-400">Gửi trực tiếp đến Hội đồng Khảo thí & Bộ môn PRJ301</p>
                </div>
            </div>

            <form onsubmit="handleAppealSubmit(event)" class="space-y-4 text-xs font-mono">
                <div>
                    <label class="block text-slate-400 mb-1">Mã bài nộp cần giải trình</label>
                    <input type="text" value="PRJ301 - Assignment 2 (OrderManager_LongTV.java)" readonly class="w-full px-3 py-2 rounded-lg bg-black/40 border border-white/10 text-slate-300 select-none cursor-not-allowed">
                </div>
                <div>
                    <label class="block text-slate-400 mb-1">Loại lý do giải trình</label>
                    <select class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-slate-200 focus:border-amber-500/50 outline-none">
                        <option>Mã khung (Boilerplate) từ đề bài yêu cầu sẵn</option>
                        <option>Áp dụng Design Pattern chuẩn từ giáo trình môn học</option>
                        <option>Sử dụng thư viện công khai (Apache Commons / Spring Framework)</option>
                        <option>Lý do khác (chi tiết bên dưới)</option>
                    </select>
                </div>
                <div>
                    <label class="block text-slate-400 mb-1">Nội dung giải trình & Minh chứng logic</label>
                    <textarea id="appeal-comment" rows="4" required placeholder="Nêu rõ vì sao đoạn mã tương tự là cần thiết, hoặc đối chiếu với tài liệu đề bài chính thức..." class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-slate-200 focus:border-amber-500/50 outline-none font-sans text-xs"></textarea>
                </div>
                <div class="pt-3 border-t border-white/10 flex items-center justify-between">
                    <span class="text-[11px] text-slate-500">Thời gian phản hồi: 24 - 48h</span>
                    <div class="flex items-center gap-2">
                        <button type="button" onclick="closeAppealModal()" class="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-slate-400 text-xs">Hủy</button>
                        <button type="submit" class="px-4 py-1.5 rounded-lg bg-amber-500 hover:bg-amber-400 text-black font-bold text-xs transition-all shadow-[0_0_15px_rgba(245,158,11,0.3)]">
                            Nộp Đơn Giải Trình
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- Modal: Cập Nhật Profile & Mật Khẩu (Sinh Viên) -->
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
                    <input type="text" name="fullName" value="${sessionScope.currentUser.fullName}" required class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-cyan-400 outline-none">
                </div>
                <div>
                    <label class="block text-slate-300 mb-1">URL Ảnh Đại Diện</label>
                    <input type="text" name="avatarUrl" value="${sessionScope.currentUser.avatarUrl}" placeholder="https://..." class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-cyan-400 outline-none">
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
                    <input type="password" name="oldPassword" required class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-rose-400 outline-none">
                </div>
                <div>
                    <label class="block text-slate-300 mb-1">Mật khẩu mới (tối thiểu 6 ký tự)</label>
                    <input type="password" name="newPassword" minlength="6" required class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-rose-400 outline-none">
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
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot-context.js?v=2.0"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot-voice.js?v=2.0"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot-chat.js?v=2.0"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot.js?v=2.0"></script>
</body>
</html>
