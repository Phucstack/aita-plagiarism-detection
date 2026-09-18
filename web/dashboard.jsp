<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AITA - Plagiarism Detection Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: {
                        deepBg: '#0b0c16',
                        sidebarBg: '#0e0f1c',
                        cardBg: '#131424',
                        cardBorder: 'rgba(255, 255, 255, 0.08)',
                        neonCyan: '#06b6d4',
                        neonViolet: '#8b5cf6',
                        neonPink: '#ec4899',
                        alertRed: '#ef4444'
                    }
                }
            }
        }
    </script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
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
<body class="data-view cinema-grain bg-[#090a15] text-slate-200 h-screen overflow-hidden flex flex-col font-sans relative">
    <!-- Interactive Cyber Particle Mesh Canvas (AST Synapse) -->
    <canvas id="cyber-canvas"></canvas>

    <!-- Cyber Ambient Aurora Background Glow -->
    <div class="cyber-aurora-bg">
        <div class="aurora-blob-1"></div>
        <div class="aurora-blob-2"></div>
    </div>
    
    <!-- Top Navigation Bar (Unified Across All Screens) -->
    <header class="h-16 shrink-0 border-b border-white/10 bg-[#0c0e1d]/90 backdrop-blur-md px-3 flex items-center justify-between z-50">
        <div class="flex items-center gap-4 lg:gap-8">
            <!-- Sidebar Collapse / Expand Toggle Button -->
            <button id="sidebar-toggle-btn" onclick="toggleSidebar()" class="hidden md:flex p-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white border border-white/5 transition-all flex items-center justify-center shrink-0" title="Ẩn/Hiện Sidebar (Ctrl+B)">
                <i data-lucide="panel-left-close" class="w-4 h-4"></i>
            </button>

            <!-- Unified Brand Logo -->
            <a href="${pageContext.request.contextPath}/dashboard" class="flex items-center gap-3 group">
                <div class="w-9 h-9 rounded-xl bg-gradient-to-tr from-cyan-500 via-indigo-500 to-violet-600 flex items-center justify-center shadow-[0_0_20px_rgba(6,182,212,0.35)] group-hover:scale-105 transition-all">
                    <i data-lucide="shield-alert" class="w-5 h-5 text-white"></i>
                </div>
                <div class="flex flex-col">
                    <div class="flex items-center gap-2">
                        <span class="text-base font-extrabold tracking-wider text-white">AITA</span>
                        <span class="text-xs font-bold text-cyan-400 font-mono">CodeDefend</span>
                        <span class="text-[10px] font-mono px-1.5 py-0.5 rounded bg-cyan-950/60 text-cyan-400 border border-cyan-500/30">G4</span>
                    </div>
                    <span class="text-[9px] font-mono text-slate-400 tracking-tight">AI Plagiarism Suite (PRJ301)</span>
                </div>
            </a>

            <!-- Unified Nav Tabs -->
            <nav class="hidden md:flex items-center gap-2 text-xs font-medium font-mono">
                <a href="${pageContext.request.contextPath}/dashboard" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-cyan-300 bg-cyan-950/50 border border-cyan-500/40 shadow-[0_0_12px_rgba(6,182,212,0.25)] transition-all">
                    <i data-lucide="layout-grid" class="w-4 h-4"></i> Dashboard
                </a>
                <a href="${pageContext.request.contextPath}/batch-scanner" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/5 transition-all">
                    <i data-lucide="scan-line" class="w-4 h-4"></i> Batch Scanner
                </a>
                <a href="${pageContext.request.contextPath}/dashboard#report-heading" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/5 transition-all">
                    <i data-lucide="split-square-vertical" class="w-4 h-4"></i> Diff Inspector
                </a>
            </nav>
        </div>

        <!-- Right Profile & Utilities Section -->
        <div class="flex items-center gap-3 sm:gap-4">
            <!-- Visual Effects Toggle Button (Mặc định: TẮT sau khi đăng nhập) -->
            <button onclick="CyberEffects.toggle()" class="effects-hud-toggle hidden sm:flex px-2.5 py-1.5 rounded-xl border border-white/10 bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white transition-all flex items-center gap-1.5 text-xs font-mono shrink-0" title="Bật/Tắt Toàn Bộ Hiệu Ứng (Mặc định: TẮT)">
                <i data-lucide="zap-off" class="w-3.5 h-3.5 text-amber-400"></i>
                <span class="hidden sm:inline text-[11px]">Hiệu ứng: <span class="effects-status-val font-bold text-amber-400">TẮT</span></span>
            </button>

            <!-- Cyber Audio HUD Toggle Button -->
            <button onclick="CyberAudio.toggle()" class="audio-hud-toggle hidden sm:flex p-2 rounded-xl border border-white/10 bg-white/5 hover:bg-cyan-500/10 hover:border-cyan-500/40 text-slate-400 hover:text-cyan-300 transition-all flex items-center justify-center shrink-0" title="Bật/Tắt Âm Thanh Tương Tác HUD">
                <i data-lucide="volume-x" class="w-4 h-4"></i>
            </button>

            <!-- Command Palette Trigger Button (Ctrl+K) -->
            <button onclick="window.commandPalette && window.commandPalette.open()" class="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-xl border border-white/10 bg-white/5 hover:bg-cyan-500/10 hover:border-cyan-500/40 text-slate-400 hover:text-cyan-300 transition-all text-xs font-mono group" title="Tìm kiếm & Điều hướng nhanh (Ctrl+K)">
                <i data-lucide="search" class="w-3.5 h-3.5 text-cyan-400 group-hover:scale-110 transition-transform"></i>
                <span class="hidden md:inline">Tìm kiếm...</span>
                <span class="px-1.5 py-0.5 rounded bg-black/40 border border-white/10 text-[10px] text-slate-400">Ctrl K</span>
            </button>

            <!-- System Status Indicator -->
            <div class="hidden lg:flex items-center gap-2 px-2.5 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-[11px] font-mono text-emerald-400">
                <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                <span>AI chưa bật</span>
            </div>

            <!-- User Profile Block -->
            <div class="flex items-center gap-2.5 pl-3 border-l border-white/10">
                <img src="${fn:escapeXml(sessionScope.currentUser.avatarUrl != null ? sessionScope.currentUser.avatarUrl : 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80')}" alt="Avatar" class="w-8 h-8 rounded-full border border-cyan-400/60 object-cover shadow-[0_0_10px_rgba(6,182,212,0.3)] cursor-pointer" onclick="openProfileModal()">
                <div class="text-left leading-tight hidden sm:block text-xs cursor-pointer" onclick="openProfileModal()">
                    <div class="font-semibold text-white hover:text-cyan-300 transition-colors"><c:out value="${sessionScope.currentUser.fullName}"/></div>
                    <div class="text-[10px] text-slate-400 font-mono"><c:out value="${sessionScope.currentUser.email}"/></div>
                </div>
                <button type="button" onclick="openProfileModal()" class="ml-1 text-slate-400 hover:text-cyan-300 transition-colors p-1" title="Cài đặt tài khoản &amp; Đổi mật khẩu">
                    <i data-lucide="user-cog" class="w-4 h-4"></i>
                </button>
                <a href="${pageContext.request.contextPath}/logout" class="ml-1 text-slate-400 hover:text-rose-400 transition-colors p-1" title="Đăng xuất">
                    <i data-lucide="log-out" class="w-4 h-4"></i>
                </a>
            </div>
        </div>
    </header>

    <div class="flex-1 flex min-h-0 overflow-hidden">
        <!-- Master Left Sidebar (Unified Across All Screens) -->
        <aside id="master-sidebar" class="hidden md:flex w-56 border-r border-white/10 bg-[#0c0e1d] flex flex-col py-5 px-3.5 gap-1 shrink-0 select-none h-full overflow-y-auto custom-sidebar-scroll">
            <div class="sidebar-heading text-[10px] font-mono text-slate-400 px-3 mb-2 uppercase tracking-wider font-semibold">Điều Hướng Chính</div>
            
            <a href="${pageContext.request.contextPath}/dashboard" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl bg-cyan-950/60 text-cyan-300 font-semibold text-xs border border-cyan-500/40 shadow-[0_0_15px_rgba(6,182,212,0.2)] transition-all" title="Dashboard">
                <div class="flex items-center gap-3">
                    <i data-lucide="layout-grid" class="w-4 h-4 text-cyan-400 shrink-0"></i>
                    <span class="sidebar-label">Dashboard</span>
                </div>
                <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-cyan-500/20 text-cyan-300 border border-cyan-500/30">Live</span>
            </a>

            <a href="${pageContext.request.contextPath}/batch-scanner" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 font-medium text-xs transition-all" title="Batch Scanner">
                <div class="flex items-center gap-3">
                    <i data-lucide="scan-line" class="w-4 h-4 shrink-0"></i>
                    <span class="sidebar-label">Batch Scanner</span>
                </div>
                <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-white/5 text-slate-400">Laser</span>
            </a>

            <a href="${pageContext.request.contextPath}/dashboard#report-heading" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 font-medium text-xs transition-all" title="Diff Inspector">
                <div class="flex items-center gap-3">
                    <i data-lucide="split-square-vertical" class="w-4 h-4 shrink-0"></i>
                    <span class="sidebar-label">Diff Inspector</span>
                </div>
                <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-rose-500/20 text-rose-400 border border-rose-500/30">Báo cáo</span>
            </a>

            <a href="#submission-list" class="sidebar-nav-item px-3 py-2 text-cyan-300 text-xs">Bài nộp của bài tập đã chọn</a>
            <p class="mt-auto text-xs text-slate-400 p-3">Java Servlet · JDBC · SQL Server</p>
        </aside>

        <main class="flex-1 min-w-0 p-4 md:p-6 h-full min-h-0 overflow-y-auto max-w-[1600px] mx-auto w-full">
            <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
                <div>
                    <h1 class="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
                        Dashboard Giám Sát Đạo Văn
                        <span class="text-xs font-mono font-normal px-2 py-0.5 rounded bg-cyan-950/60 border border-cyan-500/30 text-cyan-300">PRJ301</span>
                    </h1>
                    <p class="text-xs text-slate-400 mt-1">Quản lý đối soát cây cú pháp AST và tính liêm chính bài tập thực hành.</p>
                </div>
                
                <!-- Dynamic Course & Assignment Selectors & Full CRUD Toolbar -->
                <div class="flex flex-wrap items-center gap-2.5 text-xs font-mono">
                    <form id="filterForm" action="${pageContext.request.contextPath}/dashboard" method="GET" class="flex flex-wrap items-center gap-2">
                        <!-- Course Select -->
                        <div class="flex min-w-0 max-w-full items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#131424] border border-white/10 text-slate-300">
                            <i data-lucide="book-open" class="w-3.5 h-3.5 text-cyan-400"></i>
                            <select name="courseId" onchange="this.form.elements.namedItem('assignmentId').disabled = true; this.form.submit()" class="bg-transparent border-none text-white focus:outline-none text-xs cursor-pointer">
                                <c:forEach var="c" items="${courses}">
                                    <option value="${c.courseId}" ${c.courseId == selectedCourseId ? 'selected' : ''} class="bg-[#131424] text-white">
                                        <c:out value="${c.courseCode}"/> - <c:out value="${c.courseName}"/>
                                    </option>
                                </c:forEach>
                                <c:if test="${empty courses}">
                                    <option value="0">Chưa có khóa học</option>
                                </c:if>
                            </select>
                        </div>

                        <!-- Assignment Select -->
                        <div class="flex min-w-0 max-w-full items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#131424] border border-white/10 text-slate-300">
                            <i data-lucide="file-text" class="w-3.5 h-3.5 text-violet-400"></i>
                            <select name="assignmentId" onchange="this.form.submit()" class="bg-transparent border-none text-white focus:outline-none text-xs cursor-pointer">
                                <c:forEach var="a" items="${assignments}">
                                    <option value="${a.assignmentId}" ${a.assignmentId == selectedAssignmentId ? 'selected' : ''} class="bg-[#131424] text-white">
                                        <c:out value="${a.title}"/>
                                    </option>
                                </c:forEach>
                                <c:if test="${empty assignments}">
                                    <option value="0">Chưa có bài tập</option>
                                </c:if>
                            </select>
                        </div>
                    </form>

                    <!-- Course CRUD Actions -->
                    <div class="flex items-center gap-1 bg-white/5 p-1 rounded-xl border border-white/10">
                        <button type="button" onclick="openAddCourseModal()" class="px-2.5 py-1 rounded-lg bg-cyan-500/10 hover:bg-cyan-500/20 text-cyan-300 border border-cyan-500/30 transition-all flex items-center gap-1" title="Thêm Khóa Học Mới">
                            <i data-lucide="plus" class="w-3.5 h-3.5"></i>
                            <span>+ Môn</span>
                        </button>
                        <form id="deleteCourseForm" action="${pageContext.request.contextPath}/course-action" method="POST" class="inline m-0 p-0" onsubmit="return confirm('Bạn có chắc chắn muốn xóa khóa học này không? Mọi bài tập liên quan sẽ bị ảnh hưởng!');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="courseId" value="${selectedCourseId}">
                            <button type="submit" class="px-2 py-1 rounded-lg hover:bg-rose-500/20 text-slate-400 hover:text-rose-400 transition-all" title="Xóa môn học hiện tại">
                                <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
                            </button>
                        </form>
                    </div>

                    <!-- Assignment CRUD Actions -->
                    <div class="flex items-center gap-1 bg-white/5 p-1 rounded-xl border border-white/10">
                        <button type="button" onclick="openAddAssignmentModal()" class="px-2.5 py-1 rounded-lg bg-violet-500/10 hover:bg-violet-500/20 text-violet-300 border border-violet-500/30 transition-all flex items-center gap-1" title="Tạo Bài Tập Mới">
                            <i data-lucide="plus-circle" class="w-3.5 h-3.5"></i>
                            <span>+ Bài</span>
                        </button>
                        <button type="button" onclick="openEditAssignmentModal()" class="px-2 py-1 rounded-lg hover:bg-white/10 text-slate-300 hover:text-cyan-300 transition-all" title="Chỉnh sửa bài tập hiện tại">
                            <i data-lucide="edit-3" class="w-3.5 h-3.5"></i>
                        </button>
                        <form id="deleteAssignmentForm" action="${pageContext.request.contextPath}/assignment-action" method="POST" class="inline m-0 p-0" onsubmit="return confirm('Bạn có chắc chắn muốn xóa bài tập này không?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="assignmentId" value="${selectedAssignmentId}">
                            <input type="hidden" name="courseId" value="${selectedCourseId}">
                            <button type="submit" class="px-2 py-1 rounded-lg hover:bg-rose-500/20 text-slate-400 hover:text-rose-400 transition-all" title="Xóa bài tập hiện tại">
                                <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
                            </button>
                        </form>
                    </div>

                    <!-- Plagiarism Scan Trigger Action -->
                    <form action="${pageContext.request.contextPath}/batch-scanner" method="POST" class="inline m-0 p-0">
                        <input type="hidden" name="assignmentId" value="${selectedAssignmentId}">
                        <button type="submit" class="btn-shimmer px-3.5 py-1.5 rounded-xl bg-gradient-to-r from-cyan-500 to-violet-600 hover:from-cyan-400 hover:to-violet-500 text-white font-bold text-xs flex items-center gap-1.5 shadow-[0_0_15px_rgba(6,182,212,0.3)] transition-all">
                            <i data-lucide="scan" class="w-3.5 h-3.5"></i>
                            <span>Quét Đạo Văn</span>
                        </button>
                    </form>
                </div>
            </div>

            <%@ include file="WEB-INF/views/dashboard-data.jspf" %>
        </main>
    </div>

    <!-- AST & Gemini Algorithm Explanation Modal -->
    <div id="ast-modal" class="ast-modal-backdrop" onclick="if(event.target === this) closeAstModal()">
        <div class="ast-modal-card p-6 border border-violet-500/40 relative">
            <button onclick="closeAstModal()" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
            <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-xl bg-violet-600/20 border border-violet-500/40 flex items-center justify-center text-violet-400 shadow-[0_0_15px_rgba(139,92,246,0.3)]">
                    <i data-lucide="cpu" class="w-5 h-5"></i>
                </div>
                <div>
                    <h3 class="text-base font-bold text-white font-mono flex items-center gap-2">
                        Quy Trình Thuật Toán AST &amp; Gemini Semantic
                        <span class="px-2 py-0.5 rounded text-[10px] bg-cyan-950 text-cyan-400 border border-cyan-500/30">SE20C Nhóm 7 PRJ301</span>
                    </h3>
                    <p class="text-xs text-slate-400">Cơ chế phát hiện đạo văn mã nguồn đa tầng chống thủ thuật làm mờ (Obfuscation)</p>
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 my-4 font-mono text-xs">
                <div class="p-3.5 rounded-xl bg-[#141628] border border-white/5 space-y-2">
                    <div class="flex items-center gap-2 text-cyan-400 font-bold">
                        <span class="w-5 h-5 rounded-full bg-cyan-950 flex items-center justify-center text-[10px] border border-cyan-500/40">1</span>
                        <span>AST Structural Normalization</span>
                    </div>
                    <p class="text-[11px] text-slate-300 font-sans leading-relaxed">
                        Phân tích cây cú pháp trừu tượng, loại bỏ toàn bộ khoảng trắng, chú thích (comments), và chuẩn hóa cấu trúc khối lệnh nhằm vô hiệu hóa kỹ thuật chèn code rác.
                    </p>
                </div>

                <div class="p-3.5 rounded-xl bg-[#141628] border border-white/5 space-y-2">
                    <div class="flex items-center gap-2 text-amber-400 font-bold">
                        <span class="w-5 h-5 rounded-full bg-amber-950 flex items-center justify-center text-[10px] border border-amber-500/40">2</span>
                        <span>Variable &amp; Scope Aliasing</span>
                    </div>
                    <p class="text-[11px] text-slate-300 font-sans leading-relaxed">
                        Ánh xạ mọi tên biến cục bộ và định danh thành chuỗi token hình thức (<code class="text-amber-300">var_0, var_1</code>), bắt trọn hành vi đổi tên biến có chủ đích (<code class="text-slate-400">_cart &rarr; _basket</code>).
                    </p>
                </div>

                <div class="p-3.5 rounded-xl bg-[#141628] border border-white/5 space-y-2">
                    <div class="flex items-center gap-2 text-violet-400 font-bold">
                        <span class="w-5 h-5 rounded-full bg-violet-950 flex items-center justify-center text-[10px] border border-violet-500/40">3</span>
                        <span>Gemini 1.5 Semantic Vector</span>
                    </div>
                    <p class="text-[11px] text-slate-300 font-sans leading-relaxed">
                        Trích xuất các đoạn mã nghi vấn gửi qua Gemini API để đánh giá mức độ tương đồng ngữ nghĩa logic (như chuyển từ vòng lặp <code class="text-violet-300">for</code> sang <code class="text-violet-300">while</code>).
                    </p>
                </div>

                <div class="p-3.5 rounded-xl bg-[#141628] border border-white/5 space-y-2">
                    <div class="flex items-center gap-2 text-rose-400 font-bold">
                        <span class="w-5 h-5 rounded-full bg-rose-950 flex items-center justify-center text-[10px] border border-rose-500/40">4</span>
                        <span>Weighted Scoring &amp; Rubric</span>
                    </div>
                    <p class="text-[11px] text-slate-300 font-sans leading-relaxed">
                        Tổng hợp điểm số tương đồng theo ma trận trọng số: <strong class="text-white">40% AST Token Match + 60% Semantic Deep Learning</strong>, phân hạng mức rủi ro theo Rubric đánh giá.
                    </p>
                </div>
            </div>

            <div class="pt-3 border-t border-white/10 flex items-center justify-between">
                <span class="text-[11px] font-mono text-slate-400">Mã giải thuật: <code class="text-slate-400">services/GeminiPlagiarismService.java</code></span>
                <button onclick="closeAstModal()" class="px-4 py-1.5 rounded-lg bg-violet-600 hover:bg-violet-500 text-white text-xs font-mono font-semibold transition-all shadow-[0_0_15px_rgba(139,92,246,0.3)]">
                    Đã hiểu thuật toán
                </button>
            </div>
        </div>
    </div>

    <!-- Modal 1: Thêm Khóa Học Mới -->
    <div id="modal-add-course" class="ast-modal-backdrop" onclick="if(event.target === this) closeAddCourseModal()">
        <div class="ast-modal-card p-6 border border-cyan-500/40 relative max-w-md w-full liquid-glass-card specular-rim-border">
            <button type="button" onclick="closeAddCourseModal()" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
            <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-xl bg-cyan-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-400">
                    <i data-lucide="book-plus" class="w-5 h-5"></i>
                </div>
                <div>
                    <h3 class="text-base font-bold text-white font-mono">Thêm Khóa Học Mới</h3>
                    <p class="text-xs text-slate-400">Tạo môn học quản lý đối soát học thuật</p>
                </div>
            </div>
            <form action="${pageContext.request.contextPath}/course-action" method="POST" class="space-y-4 text-xs font-mono">
                <input type="hidden" name="action" value="create">
                <div>
                    <label class="block text-slate-300 mb-1">Mã môn học (Course Code)*</label>
                    <input aria-label="Mã khóa học" type="text" name="courseCode" required placeholder="Ví dụ: PRJ301, CSD201, PRF192" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-cyan-400 outline-none">
                </div>
                <div>
                    <label class="block text-slate-300 mb-1">Tên môn học (Course Name)*</label>
                    <input aria-label="Tên khóa học" type="text" name="courseName" required placeholder="Ví dụ: Java Web Applications" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-cyan-400 outline-none">
                </div>
                <div>
                    <label class="block text-slate-300 mb-1">Học kỳ (Semester)</label>
                    <input aria-label="Học kỳ" type="text" name="semester" value="Fall 2026" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-cyan-400 outline-none">
                </div>
                <div class="pt-3 border-t border-white/10 flex items-center justify-end gap-2">
                    <button type="button" onclick="closeAddCourseModal()" class="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-slate-400 text-xs">Hủy</button>
                    <button type="submit" class="px-4 py-1.5 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-black font-bold text-xs transition-all shadow-[0_0_15px_rgba(6,182,212,0.3)]">Lưu Khóa Học</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Modal 2: Thêm Bài Tập Mới -->
    <div id="modal-add-assignment" class="ast-modal-backdrop" onclick="if(event.target === this) closeAddAssignmentModal()">
        <div class="ast-modal-card p-6 border border-violet-500/40 relative max-w-lg w-full liquid-glass-card specular-rim-border">
            <button type="button" onclick="closeAddAssignmentModal()" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
            <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-xl bg-violet-500/20 border border-violet-500/40 flex items-center justify-center text-violet-400">
                    <i data-lucide="file-plus" class="w-5 h-5"></i>
                </div>
                <div>
                    <h3 class="text-base font-bold text-white font-mono">Tạo Bài Tập Mới</h3>
                    <p class="text-xs text-slate-400">Thiết lập bài tập &amp; ngưỡng cảnh báo đạo văn</p>
                </div>
            </div>
            <form action="${pageContext.request.contextPath}/assignment-action" method="POST" class="space-y-3.5 text-xs font-mono">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="courseId" value="${selectedCourseId}">
                <div>
                    <label class="block text-slate-300 mb-1">Tiêu đề bài tập*</label>
                    <input aria-label="Tiêu đề bài tập" type="text" name="title" required placeholder="Ví dụ: Assignment 3 - MVC Servlet Online Store" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-violet-400 outline-none">
                </div>
                <div>
                    <label class="block text-slate-300 mb-1">Mô tả / Yêu cầu đề bài</label>
                    <textarea name="description" rows="2" placeholder="Yêu cầu viết bằng Java Servlet, tuân thủ mô hình MVC2..." class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-violet-400 outline-none font-sans"></textarea>
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-slate-300 mb-1">Ngưỡng cảnh báo (%)</label>
                        <input aria-label="Ngưỡng tương đồng (%)" type="number" name="similarityThreshold" step="0.01" min="0" max="100" value="75.0" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-violet-400 outline-none">
                    </div>
                    <div>
                        <label class="block text-slate-300 mb-1">Điểm tối đa</label>
                        <input aria-label="Điểm tối đa" type="number" name="maxScore" step="0.01" min="0.01" max="999.99" value="100.0" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-violet-400 outline-none">
                    </div>
                </div>
                <div>
                    <label class="block text-slate-300 mb-1">Hạn nộp bài (Deadline)</label>
                    <input aria-label="Hạn nộp bài" type="datetime-local" name="deadline" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-violet-400 outline-none">
                </div>
                <div class="pt-3 border-t border-white/10 flex items-center justify-end gap-2">
                    <button type="button" onclick="closeAddAssignmentModal()" class="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-slate-400 text-xs">Hủy</button>
                    <button type="submit" class="px-4 py-1.5 rounded-lg bg-violet-600 hover:bg-violet-500 text-white font-bold text-xs transition-all shadow-[0_0_15px_rgba(139,92,246,0.3)]">Tạo Bài Tập</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Modal 3: Chỉnh Sửa Bài Tập -->
    <div id="modal-edit-assignment" class="ast-modal-backdrop" onclick="if(event.target === this) closeEditAssignmentModal()">
        <div class="ast-modal-card p-6 border border-amber-500/40 relative max-w-lg w-full liquid-glass-card specular-rim-border">
            <button type="button" onclick="closeEditAssignmentModal()" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
            <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400">
                    <i data-lucide="edit" class="w-5 h-5"></i>
                </div>
                <div>
                    <h3 class="text-base font-bold text-white font-mono">Chỉnh Sửa Bài Tập</h3>
                    <p class="text-xs text-slate-400">Cập nhật thông số bài tập #${selectedAssignmentId}</p>
                </div>
            </div>
            <form action="${pageContext.request.contextPath}/assignment-action" method="POST" class="space-y-3.5 text-xs font-mono">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="assignmentId" value="${selectedAssignmentId}">
                <input type="hidden" name="courseId" value="${selectedCourseId}">
                <div>
                    <label class="block text-slate-300 mb-1">Tiêu đề bài tập*</label>
                    <input aria-label="Tiêu đề bài tập" type="text" name="title" value="${fn:escapeXml(currentAssignment.title)}" required class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-amber-400 outline-none">
                </div>
                <div>
                    <label class="block text-slate-300 mb-1">Mô tả / Yêu cầu đề bài</label>
                    <textarea name="description" rows="2" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-amber-400 outline-none font-sans">${fn:escapeXml(currentAssignment.description)}</textarea>
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-slate-300 mb-1">Ngưỡng cảnh báo (%)</label>
                        <input aria-label="Ngưỡng tương đồng (%)" type="number" name="similarityThreshold" step="0.01" min="0" max="100" value="${currentAssignment != null ? currentAssignment.similarityThreshold : 75.0}" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-amber-400 outline-none">
                    </div>
                    <div>
                        <label class="block text-slate-300 mb-1">Điểm tối đa</label>
                        <input aria-label="Điểm tối đa" type="number" name="maxScore" step="0.01" min="0.01" max="999.99" value="${currentAssignment.maxScore != 0 ? currentAssignment.maxScore : 100.0}" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-amber-400 outline-none">
                    </div>
                </div>
                <div>
                    <label class="block text-slate-300 mb-1">Hạn nộp bài (Deadline)</label>
                    <input aria-label="Hạn nộp bài" type="datetime-local" name="deadline" value="${selectedDeadline}" class="w-full px-3 py-2 rounded-lg bg-[#141628] border border-white/10 text-white focus:border-amber-400 outline-none">
                </div>
                <div class="pt-3 border-t border-white/10 flex items-center justify-end gap-2">
                    <button type="button" onclick="closeEditAssignmentModal()" class="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-slate-400 text-xs">Hủy</button>
                    <button type="submit" class="px-4 py-1.5 rounded-lg bg-amber-500 hover:bg-amber-400 text-black font-bold text-xs transition-all shadow-[0_0_15px_rgba(245,158,11,0.3)]">Lưu Thay Đổi</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Modal 4: Cập Nhật Profile & Mật Khẩu -->
    <div id="modal-user-profile" class="ast-modal-backdrop" onclick="if(event.target === this) closeProfileModal()">
        <div class="ast-modal-card p-6 border border-cyan-500/40 relative max-w-lg w-full liquid-glass-card specular-rim-border">
            <button type="button" onclick="closeProfileModal()" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
            <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-xl bg-cyan-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-400">
                    <i data-lucide="user-check" class="w-5 h-5"></i>
                </div>
                <div>
                    <h3 class="text-base font-bold text-white font-mono">Hồ Sơ &amp; Bảo Mật</h3>
                    <p class="text-xs text-slate-400">Cập nhật thông tin giảng viên &amp; bảo mật mật khẩu</p>
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
        lucide.createIcons();

        function openAddCourseModal() {
            const m = document.getElementById('modal-add-course');
            if (m) m.classList.add('active');
            if (window.CyberAudio) CyberAudio.playTick();
        }
        function closeAddCourseModal() {
            const m = document.getElementById('modal-add-course');
            if (m) m.classList.remove('active');
        }
        function openAddAssignmentModal() {
            const m = document.getElementById('modal-add-assignment');
            if (m) m.classList.add('active');
            if (window.CyberAudio) CyberAudio.playTick();
        }
        function closeAddAssignmentModal() {
            const m = document.getElementById('modal-add-assignment');
            if (m) m.classList.remove('active');
        }
        function openEditAssignmentModal() {
            const m = document.getElementById('modal-edit-assignment');
            if (m) m.classList.add('active');
            if (window.CyberAudio) CyberAudio.playTick();
        }
        function closeEditAssignmentModal() {
            const m = document.getElementById('modal-edit-assignment');
            if (m) m.classList.remove('active');
        }
        function openProfileModal() {
            const m = document.getElementById('modal-user-profile');
            if (m) m.classList.add('active');
            if (window.CyberAudio) CyberAudio.playTick();
        }
        function closeProfileModal() {
            const m = document.getElementById('modal-user-profile');
            if (m) m.classList.remove('active');
        }

        // Toast notifications from URL Params
        window.addEventListener('DOMContentLoaded', () => {
            const params = new URLSearchParams(window.location.search);
            if (window.showToast) {
                if (params.get('courseMsg') === 'created') showToast('Khóa học mới đã được tạo thành công!', 'success');
                if (params.get('courseMsg') === 'updated') showToast('Đã cập nhật thông tin khóa học!', 'success');
                if (params.get('courseMsg') === 'deleted') showToast('Đã xóa khóa học thành công!', 'info');
                if (params.get('assignmentMsg') === 'created') showToast('Bài tập mới đã được tạo thành công!', 'success');
                if (params.get('assignmentMsg') === 'updated') showToast('Đã cập nhật bài tập thành công!', 'success');
                if (params.get('assignmentMsg') === 'deleted') showToast('Đã xóa bài tập thành công!', 'info');
                if (params.get('scanSuccess') === 'true') showToast('Quét đối soát hoàn tất! Đã cập nhật ma trận vi phạm.', 'success');
                if (params.get('profileMsg') === 'profile_updated') showToast('Cập nhật thông tin tài khoản thành công!', 'success');
                if (params.get('profileMsg') === 'password_changed') showToast('Đổi mật khẩu thành công!', 'success');
                if (params.get('profileMsg') === 'invalid_password_format') showToast('Mật khẩu mới cần từ 8 đến 1024 ký tự.', 'error');
                if (params.get('profileMsg') === 'wrong_old_password') showToast('Mật khẩu cũ không chính xác!', 'error');
                if (params.get('courseError') || params.get('assignmentError')) showToast('Thao tác không thành công, vui lòng thử lại!', 'error');
            }
        });

        function openAstModal() {
            const modal = document.getElementById('ast-modal');
            if (modal) {
                modal.classList.remove('hidden');
                modal.classList.add('active');
            }
            if (window.CyberAudio) CyberAudio.playTick();
        }

        function closeAstModal() {
            const modal = document.getElementById('ast-modal');
            if (modal) {
                modal.classList.remove('active');
                modal.classList.add('hidden');
            }
        }

        function toggleSubFilterMenu(e) {
            e.stopPropagation();
            const menu = document.getElementById('sub-filter-menu');
            if (menu) menu.classList.toggle('hidden');
        }

        function filterDashboardRows(criteria) {
            const rows = document.querySelectorAll('.dashboard-data-row');
            let count = 0;
            rows.forEach(row => {
                const matchVal = parseFloat(row.getAttribute('data-match')) || 0;
                let show = true;
                if (criteria === 'flagged' || criteria === 'high') {
                    show = matchVal >= 80;
                } else if (criteria === 'medium') {
                    show = matchVal >= 40 && matchVal < 80;
                } else if (criteria === 'clean') {
                    show = matchVal < 40;
                }
                row.style.display = show ? '' : 'none';
                if (show) count++;
            });
            return count;
        }

        function selectSubFilter(label, btn) {
            const labelEl = document.getElementById('current-sub-filter');
            if (labelEl) labelEl.innerText = label;
            const menu = document.getElementById('sub-filter-menu');
            if (menu) menu.classList.add('hidden');

            let criteria = 'all';
            if (label.includes('Flagged')) criteria = 'flagged';
            else if (label.includes('Clean')) criteria = 'clean';

            const count = filterDashboardRows(criteria);
            if (window.showToast) showToast(`Bộ lọc bảng: ${label} (${count} kết quả)`, 'info');
            if (window.CyberAudio) CyberAudio.playTick();
        }

        document.addEventListener('click', () => {
            const menu = document.getElementById('sub-filter-menu');
            if (menu) menu.classList.add('hidden');
        });
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-effects.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-audio.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-particles.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/tilt-motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/terminal-stream.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/system-modals.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion-system-2026.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/command-palette.js"></script>
    <!-- AITA Realtime AI Copilot (ZeroTTS Voice Engine) -->
</body>
</html>
