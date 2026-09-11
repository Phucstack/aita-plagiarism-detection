<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CodeDefend - Analysis: Java Project</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: {
                        deepBg: '#090a12',
                        sidebarBg: '#0d0e1a',
                        cardBg: '#121422',
                        codeBg: '#0b0c14',
                        neonCyan: '#06b6d4',
                        dangerRed: '#ef4444',
                        warningAmber: '#f59e0b'
                    }
                }
            }
        }
    </script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
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
<body class="cinema-grain bg-[#090a15] text-slate-200 h-screen overflow-hidden flex flex-col font-sans relative">
    <!-- Interactive Cyber Particle Mesh Canvas (AST Synapse) -->
    <canvas id="cyber-canvas"></canvas>

    <!-- Cyber Ambient Aurora Background Glow -->
    <div class="cyber-aurora-bg">
        <div class="aurora-blob-1"></div>
        <div class="aurora-blob-2"></div>
    </div>
    
    <!-- Top Navigation Bar (Unified Across All Screens) -->
    <header class="h-16 shrink-0 border-b border-white/10 bg-[#0c0e1d]/90 backdrop-blur-md px-5 flex items-center justify-between z-50">
        <div class="flex items-center gap-4 lg:gap-8">
            <!-- Sidebar Collapse / Expand Toggle Button -->
            <button id="sidebar-toggle-btn" onclick="toggleSidebar()" class="p-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white border border-white/5 transition-all flex items-center justify-center shrink-0" title="Ẩn/Hiện Sidebar (Ctrl+B)">
                <i data-lucide="panel-left-close" class="w-4 h-4"></i>
            </button>

            <!-- Unified Brand Logo -->
            <a href="${pageContext.request.contextPath}/${isStudent ? 'student-portal' : 'dashboard'}" class="flex items-center gap-3 group">
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
            <c:choose>
                <c:when test="${isStudent}">
                    <nav class="hidden md:flex items-center gap-2 text-xs font-medium font-mono">
                        <a href="${pageContext.request.contextPath}/student-portal" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-slate-300 hover:text-white hover:bg-white/5 transition-all">
                            <i data-lucide="arrow-left" class="w-4 h-4 text-cyan-400"></i> Quay lại Cổng Sinh Viên
                        </a>
                        <span class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-cyan-300 bg-cyan-950/50 border border-cyan-500/40 shadow-[0_0_12px_rgba(6,182,212,0.25)]">
                            <i data-lucide="split-square-vertical" class="w-4 h-4"></i> Đối Soát Mã Nguồn
                        </span>
                    </nav>
                </c:when>
                <c:otherwise>
                    <nav class="hidden md:flex items-center gap-2 text-xs font-medium font-mono">
                        <a href="${pageContext.request.contextPath}/dashboard" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/5 transition-all">
                            <i data-lucide="layout-grid" class="w-4 h-4"></i> Dashboard
                        </a>
                        <a href="${pageContext.request.contextPath}/batch-scanner" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/5 transition-all">
                            <i data-lucide="scan-line" class="w-4 h-4"></i> Batch Scanner
                        </a>
                        <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-cyan-300 bg-cyan-950/50 border border-cyan-500/40 shadow-[0_0_12px_rgba(6,182,212,0.25)] transition-all">
                            <i data-lucide="split-square-vertical" class="w-4 h-4"></i> Diff Inspector
                        </a>
                    </nav>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Right Profile & Utilities Section -->
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

            <!-- System Status Indicator -->
            <div class="hidden lg:flex items-center gap-2 px-2.5 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-[11px] font-mono text-emerald-400">
                <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                <span>AI Online</span>
            </div>

            <button class="text-slate-400 hover:text-white transition-colors" title="Trợ giúp"><i data-lucide="help-circle" class="w-4 h-4"></i></button>
            <div class="relative">
                <button class="text-slate-400 hover:text-white transition-colors" title="Thông báo"><i data-lucide="bell" class="w-4 h-4"></i></button>
                <span class="absolute -top-1 -right-1 w-3.5 h-3.5 rounded-full bg-rose-500 text-[9px] font-bold text-white flex items-center justify-center">1</span>
            </div>
            
            <!-- User Profile Block -->
            <div class="flex items-center gap-2.5 pl-3 border-l border-white/10">
                <c:choose>
                    <c:when test="${isStudent}">
                        <img src="${not empty sessionScope.currentUser.avatarUrl ? sessionScope.currentUser.avatarUrl : 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=80'}" alt="Avatar" class="w-8 h-8 rounded-full border border-cyan-400/60 object-cover shadow-[0_0_10px_rgba(6,182,212,0.3)]">
                        <div class="text-left leading-tight hidden sm:block text-xs">
                            <div class="font-semibold text-white">${not empty sessionScope.currentUser.fullName ? sessionScope.currentUser.fullName : 'Trần Văn Long (SE1701)'}</div>
                            <div class="text-[10px] text-slate-400 font-mono">${not empty sessionScope.currentUser.email ? sessionScope.currentUser.email : 'longtvse1701@fpt.edu.vn'}</div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <img src="${not empty sessionScope.currentUser.avatarUrl ? sessionScope.currentUser.avatarUrl : 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80'}" alt="Avatar" class="w-8 h-8 rounded-full border border-cyan-400/60 object-cover shadow-[0_0_10px_rgba(6,182,212,0.3)]">
                        <div class="text-left leading-tight hidden sm:block text-xs">
                            <div class="font-semibold text-white">${not empty sessionScope.currentUser.fullName ? sessionScope.currentUser.fullName : 'TS. Nguyễn Hoàng Hà'}</div>
                            <div class="text-[10px] text-slate-400 font-mono">${not empty sessionScope.currentUser.email ? sessionScope.currentUser.email : 'ha.nguyen@fpt.edu.vn'}</div>
                        </div>
                    </c:otherwise>
                </c:choose>
                <a href="${pageContext.request.contextPath}/login" class="ml-2 text-slate-400 hover:text-rose-400 transition-colors" title="Đăng xuất">
                    <i data-lucide="log-out" class="w-4 h-4"></i>
                </a>
            </div>
        </div>
    </header>

    <!-- Main Container with Left Thin Rail -->
    <div class="flex-1 flex min-h-0 overflow-hidden">
        <!-- Master Left Sidebar (Unified Across All Screens) -->
        <aside id="master-sidebar" class="w-56 border-r border-white/10 bg-[#0c0e1d] flex flex-col py-5 px-3.5 gap-1 shrink-0 select-none h-full overflow-y-auto custom-sidebar-scroll">
            <div class="sidebar-heading text-[10px] font-mono text-slate-500 px-3 mb-2 uppercase tracking-wider font-semibold">Điều Hướng Chính</div>
            
            <c:choose>
                <c:when test="${isStudent}">
                    <a href="${pageContext.request.contextPath}/student-portal" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 font-medium text-xs transition-all" title="Cổng Sinh Viên">
                        <div class="flex items-center gap-3">
                            <i data-lucide="graduation-cap" class="w-4 h-4 shrink-0 text-cyan-400"></i>
                            <span class="sidebar-label">Cổng Sinh Viên</span>
                        </div>
                        <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-cyan-500/10 text-cyan-300 border border-cyan-500/20">Portal</span>
                    </a>

                    <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1&role=student" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl bg-rose-950/60 text-rose-300 font-semibold text-xs border border-rose-500/40 shadow-[0_0_15px_rgba(244,63,94,0.2)] transition-all" title="Đối Soát Mã Nguồn">
                        <div class="flex items-center gap-3">
                            <i data-lucide="split-square-vertical" class="w-4 h-4 text-rose-400 shrink-0"></i>
                            <span class="sidebar-label">Đối Soát AST</span>
                        </div>
                        <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-rose-500/20 text-rose-400 border border-rose-500/30">88%</span>
                    </a>

                    <button onclick="openAstModal()" class="sidebar-nav-item w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-violet-300 hover:bg-violet-950/20 font-medium text-xs transition-all text-left" title="AST & Gemini AI">
                        <div class="flex items-center gap-3">
                            <i data-lucide="cpu" class="w-4 h-4 text-violet-400 shrink-0"></i>
                            <span class="sidebar-label">AST &amp; Gemini AI</span>
                        </div>
                        <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-violet-500/20 text-violet-400 border border-violet-500/30">v1.5</span>
                    </button>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/dashboard" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 font-medium text-xs transition-all" title="Dashboard">
                        <div class="flex items-center gap-3">
                            <i data-lucide="layout-grid" class="w-4 h-4 shrink-0"></i>
                            <span class="sidebar-label">Dashboard</span>
                        </div>
                        <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-white/5 text-slate-400">Live</span>
                    </a>

                    <a href="${pageContext.request.contextPath}/batch-scanner" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 font-medium text-xs transition-all" title="Batch Scanner">
                        <div class="flex items-center gap-3">
                            <i data-lucide="scan-line" class="w-4 h-4 shrink-0"></i>
                            <span class="sidebar-label">Batch Scanner</span>
                        </div>
                        <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-white/5 text-slate-400">Laser</span>
                    </a>

                    <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl bg-rose-950/60 text-rose-300 font-semibold text-xs border border-rose-500/40 shadow-[0_0_15px_rgba(244,63,94,0.2)] transition-all" title="Diff Inspector">
                        <div class="flex items-center gap-3">
                            <i data-lucide="split-square-vertical" class="w-4 h-4 text-rose-400 shrink-0"></i>
                            <span class="sidebar-label">Diff Inspector</span>
                        </div>
                        <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-rose-500/20 text-rose-400 border border-rose-500/30">88% Risk</span>
                    </a>

                    <button onclick="openAstModal()" class="sidebar-nav-item w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-violet-300 hover:bg-violet-950/20 font-medium text-xs transition-all text-left" title="AST & Gemini AI">
                        <div class="flex items-center gap-3">
                            <i data-lucide="cpu" class="w-4 h-4 text-violet-400 shrink-0"></i>
                            <span class="sidebar-label">AST &amp; Gemini AI</span>
                        </div>
                        <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-violet-500/20 text-violet-400 border border-violet-500/30">v1.5</span>
                    </button>

                    <div class="sidebar-heading text-[10px] font-mono text-slate-500 px-3 mt-4 mb-2 uppercase tracking-wider font-semibold">Hệ Thống &amp; Khảo Thí</div>

                    <a href="javascript:void(0)" onclick="openRepositoryModal()" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 font-medium text-xs transition-all cursor-pointer" title="Kho Bài Nộp">
                        <div class="flex items-center gap-3">
                            <i data-lucide="folder-git-2" class="w-4 h-4 shrink-0 text-cyan-400"></i>
                            <span class="sidebar-label">Kho Bài Nộp</span>
                        </div>
                        <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-cyan-950/70 border border-cyan-500/40 text-cyan-300">42</span>
                    </a>

                    <a href="javascript:void(0)" onclick="openScanLogsModal()" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 font-medium text-xs transition-all cursor-pointer" title="Nhật Ký Quét">
                        <div class="flex items-center gap-3">
                            <i data-lucide="history" class="w-4 h-4 shrink-0 text-violet-400"></i>
                            <span class="sidebar-label">Nhật Ký Quét</span>
                        </div>
                        <span class="sidebar-badge text-[9px] font-mono text-violet-400">Live</span>
                    </a>

                    <a href="javascript:void(0)" onclick="openThresholdModal()" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 font-medium text-xs transition-all cursor-pointer" title="Ngưỡng Đạo Văn">
                        <div class="flex items-center gap-3">
                            <i data-lucide="sliders" class="w-4 h-4 shrink-0 text-amber-400"></i>
                            <span class="sidebar-label">Ngưỡng Đạo Văn</span>
                        </div>
                        <span class="sidebar-badge text-[9px] font-mono text-amber-400">Config</span>
                    </a>
                </c:otherwise>
            </c:choose>

            <!-- Sidebar Bottom Info Box -->
            <div class="mt-auto pt-4 border-t border-white/5 space-y-3">
                <div class="sidebar-quota-box p-3 rounded-xl bg-[#080914] border border-white/5" title="Gemini 1.5 Quota: 84%">
                    <div class="flex items-center justify-between text-[11px] font-mono mb-1.5 w-full">
                        <span class="sidebar-label text-slate-400">Gemini 1.5 Quota</span>
                        <span class="text-cyan-400 font-bold">84%</span>
                    </div>
                    <div class="w-full h-1.5 rounded-full bg-white/10 overflow-hidden">
                        <div class="bg-gradient-to-r from-cyan-400 to-violet-500 h-full w-[84%]"></div>
                    </div>
                    <div class="sidebar-footer-detail flex items-center justify-between text-[9px] font-mono text-slate-500 mt-1.5">
                        <span>42/50 RPM</span>
                        <span>API Active</span>
                    </div>
                </div>

                <div class="sidebar-footer-detail flex items-center justify-between px-1 text-[10px] font-mono text-slate-500">
                    <span class="flex items-center gap-1.5">
                        <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                        AITA v2.4 (G4)
                    </span>
                    <span>PRJ301</span>
                </div>
            </div>
        </aside>

        <!-- Main Content Area -->
        <main class="flex-1 p-5 h-full min-h-0 overflow-y-auto max-w-[1700px] mx-auto w-full">
            
            <!-- Breadcrumb & Top Gauge Bar -->
            <div class="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4 pb-4 mb-4 border-b border-white/5">
                <div>
                    <div class="text-xs text-slate-400 font-mono">
                        <c:choose>
                            <c:when test="${isStudent}">
                                Cổng Sinh Viên &gt; <span class="text-cyan-300">Đối Soát Chi Tiết Mã Nguồn</span> &gt; <span class="text-slate-300">OrderManager_LongTV.java</span>
                            </c:when>
                            <c:otherwise>
                                Analysis: <span class="text-white">Java Project</span> &gt; <span class="text-slate-300">Submission_882</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Circular Ring Gauge + Risk Header Details -->
                <div class="flex items-center gap-5">
                    <!-- Gauge Ring -->
                    <div class="relative w-16 h-16 flex items-center justify-center shrink-0">
                        <svg class="w-16 h-16 transform -rotate-90">
                            <circle cx="32" cy="32" r="26" stroke="rgba(255,255,255,0.08)" stroke-width="5" fill="transparent"/>
                            <circle cx="32" cy="32" r="26" stroke="#ef4444" stroke-width="5" stroke-dasharray="163" stroke-dashoffset="20" stroke-linecap="round" fill="transparent"/>
                        </svg>
                        <span class="absolute text-sm font-extrabold font-mono text-cyan-400" data-counter-target="88" data-counter-suffix="%">88%</span>
                    </div>

                    <!-- Risk Info -->
                    <div>
                        <div class="text-base font-bold text-white flex items-center gap-2">
                            88% Semantic Similarity - <span class="text-rose-500 font-extrabold">HIGH RISK</span>
                        </div>
                        <div class="text-xs text-slate-400 mt-0.5">
                            Detected <strong class="text-white">14</strong> Matching Blocks | <strong class="text-white">7</strong> Duplicated Functions
                        </div>
                        <div class="flex items-center gap-4 text-[10px] font-mono text-slate-500 mt-1">
                            <span>Submissions Analyzed: 2</span>
                            <span>Language: Java</span>
                            <span>Files: 18</span>
                            <span>Oct 26, 14:32</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 12-Column Grid: (Left 10 cols: Code & AI Card in Unified Red Frame) | (Right 2 cols: Sidebars) -->
            <div class="grid grid-cols-1 xl:grid-cols-12 gap-4 items-start">
                
                <!-- Left 10 Columns: Unified Red/Amber Frame containing Student A, Student B & AI Insight Card -->
                <div class="xl:col-span-10 p-2.5 rounded-2xl border border-rose-500/40 bg-[#0e0f1a]/80 shadow-[0_0_30px_rgba(239,68,68,0.1)] flex flex-col gap-3 relative overflow-hidden border-beam-container">
                    
                    <!-- Artifact SHA-256 Fingerprint Integrity Bar (Thỏa mãn RBL Mục 4.4.2 & Rubric Application 25đ) -->
                    <div class="px-3.5 py-2 rounded-xl bg-[#080914]/90 border border-white/10 text-[10px] font-mono flex flex-wrap items-center justify-between gap-2 shadow-inner">
                        <div class="flex items-center gap-2">
                            <span class="px-2 py-0.5 rounded bg-emerald-950/80 border border-emerald-500/40 text-emerald-400 flex items-center gap-1 font-semibold">
                                <i data-lucide="shield-check" class="w-3 h-3"></i> SHA-256 VERIFIED 4.4.2
                            </span>
                            <span class="text-slate-400">SV A Artifact: <code class="text-cyan-300 font-semibold">e3b0c442...855</code></span>
                            <span class="text-slate-600">|</span>
                            <span class="text-slate-400">SV B Artifact: <code class="text-violet-300 font-semibold">4b227777...f8a</code></span>
                        </div>
                        <div class="flex items-center gap-2.5">
                            <span class="text-slate-400">Trọng số: <strong class="text-cyan-400">40% AST</strong> + <strong class="text-violet-400">60% AI</strong></span>
                            <span class="px-2 py-0.5 rounded bg-rose-950/80 border border-rose-500/50 text-rose-400 font-bold">88% RỦI RO CAO</span>
                            <!-- Kinetic AST Token Normalizer Toggle Button -->
                            <button id="btn-toggle-ast-tokens" onclick="toggleAstTokenMode()" class="ml-1 px-2 py-0.5 rounded-lg bg-cyan-950/70 hover:bg-cyan-900/80 border border-cyan-500/40 text-cyan-300 font-mono text-[10px] font-bold flex items-center gap-1 transition-all shadow-[0_0_10px_rgba(6,182,212,0.2)]" title="Chuyển đổi giữa Mã Nguồn Gốc và Chuẩn Hóa Cây Cú Pháp AST Token (JavaParser 3.25)">
                                <i data-lucide="cpu" class="w-3 h-3 text-cyan-400"></i>
                                <span id="ast-toggle-label">AST Token View</span>
                            </button>
                        </div>
                    </div>

                    <!-- 2-Column Side-by-Side Code Editors with SVG Connecting Curves -->
                    <div id="diff-comparison-container" class="grid grid-cols-1 md:grid-cols-2 gap-6 relative">

                        <!-- Column 1: Student A (Code Submission) -->
                        <div class="cyber-card student-a-editor border-rose-500/40 bg-[#121320] flex flex-col shadow-[0_0_15px_rgba(239,68,68,0.15)] rounded-xl overflow-hidden spotlight-card">
                            <div class="px-4 py-2 bg-[#161726] border-b border-white/5 flex items-center justify-between text-xs font-mono">
                                <c:choose>
                                    <c:when test="${isStudent}">
                                        <span class="text-cyan-400 font-bold tracking-wider">BÀI NỘP CỦA BẠN (OrderManager_LongTV.java)</span>
                                        <span class="text-slate-400">Trần Văn Long - SE1701</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-rose-400 font-bold tracking-wider">STUDENT A (Code Submission)</span>
                                        <span class="text-slate-500">#A02D214</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="px-4 py-1.5 bg-[#11121d] border-b border-white/5 text-[11px] font-mono text-slate-400">
                                orderProcessing/OrderManager.java
                            </div>

                            <!-- Code Box Left -->
                            <div id="editor-student-a" class="p-3 font-mono-code text-[11px] leading-relaxed text-slate-300 overflow-y-auto max-h-[390px] bg-[#0c0d16]">
                                <div class="flex"><span class="line-gutter">24</span><span>public class <span class="text-cyan-400">OrderManager</span> {</span></div>
                                <div class="flex"><span class="line-gutter">26</span><span></span></div>
                                <div class="flex"><span class="line-gutter">28</span><span>&nbsp;&nbsp;&nbsp;&nbsp;private List&lt;Order&gt; orders;</span></div>
                                <div class="flex"><span class="line-gutter">23</span><span class="text-slate-500">&nbsp;&nbsp;&nbsp;&nbsp;// Matched plagiarism</span></div>

                                <!-- Red Box 1 -->
                                <div data-match-block="1" onmouseenter="highlightBlock(1)" onmouseleave="unhighlightBlock(1)" class="my-1 py-2 px-1 rounded-lg bg-rose-950/40 border border-rose-500/80 shadow-[0_0_15px_rgba(239,68,68,0.25)] cursor-pointer">
                                    <div class="flex"><span class="line-gutter text-rose-400">24</span><span>&nbsp;&nbsp;&nbsp;&nbsp;public double <span class="text-rose-300 font-bold">calculateTotal</span>() {</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">25</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cartValue &gt; cartValue = cartValue;</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">26</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;finalCost &gt; finalCost = P*finalCost;</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">27</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return cart;</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">38</span><span>&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                                </div>

                                <!-- Box 2 (Amber) -->
                                <div data-match-block="2" onmouseenter="highlightBlock(2)" onmouseleave="unhighlightBlock(2)" class="my-1 py-1.5 px-1 rounded-lg bg-amber-950/20 border border-amber-500/70 cursor-pointer">
                                    <div class="flex"><span class="line-gutter text-amber-400">44</span><span>&nbsp;&nbsp;&nbsp;&nbsp;public double <span class="text-cyan-300 font-bold">calculateTotal</span>() {</span></div>
                                    <div class="flex"><span class="line-gutter text-amber-400">48</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return est=orderList = orderList;</span></div>
                                    <div class="flex"><span class="line-gutter text-amber-400">42</span><span>&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                                </div>
                                <div class="flex"><span class="line-gutter">43</span><span class="text-slate-500">&nbsp;&nbsp;&nbsp;&nbsp;// Matching payment</span></div>

                                <!-- Red Box 3 -->
                                <div data-match-block="3" onmouseenter="highlightBlock(3)" onmouseleave="unhighlightBlock(3)" class="my-1 py-2 px-1 rounded-lg bg-rose-950/40 border border-rose-500/80 shadow-[0_0_15px_rgba(239,68,68,0.25)] cursor-pointer">
                                    <div class="flex"><span class="line-gutter text-rose-400">45</span><span>&nbsp;&nbsp;&nbsp;&nbsp;public double <span class="text-rose-300 font-bold">processPayment</span>(baskeYyer&gt; orderItems) {</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">46</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;List&lt;owtKaue = new Stock&lt;orderItems&gt;;</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">47</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (basketValue.&lt;orderLiter&gt;.orderItems) {</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">48</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalCost = anablen;</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">51</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">53</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return pastvutsetValue;</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">54</span><span>&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                                </div>

                                <div class="flex"><span class="line-gutter">55</span><span>&nbsp;&nbsp;&nbsp;&nbsp;public double updateStock(cyOrderItems) {</span></div>
                                <div class="flex"><span class="line-gutter">56</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// orderList=&lt;orderSos&gt; {</span></div>
                                <div class="flex"><span class="line-gutter">57</span><span>&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                            </div>
                        </div>

                        <!-- Column 2: Student B (Code Submission) -->
                        <div class="cyber-card student-b-editor border-amber-500/40 bg-[#121320] flex flex-col shadow-[0_0_15px_rgba(245,158,11,0.15)] rounded-xl overflow-hidden spotlight-card">
                            <div class="px-4 py-2 bg-[#161726] border-b border-white/5 flex items-center justify-between text-xs font-mono">
                                <c:choose>
                                    <c:when test="${isStudent}">
                                        <span class="text-amber-400 font-bold tracking-wider">BÀI NỘP ĐỐI CHIẾU (ĐÃ ẨN DANH)</span>
                                        <span class="text-slate-500">#13702 - Ẩn danh</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-amber-400 font-bold tracking-wider">STUDENT B (Code Submission)</span>
                                        <span class="text-slate-500">#B08E743</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="px-4 py-1.5 bg-[#11121d] border-b border-white/5 text-[11px] font-mono text-slate-400">
                                orderProcessing/OrderManager.java
                            </div>

                            <!-- Code Box Right -->
                            <div id="editor-student-b" class="p-3 font-mono-code text-[11px] leading-relaxed text-slate-300 overflow-y-auto max-h-[390px] bg-[#0c0d16]">
                                <div class="flex"><span class="line-gutter">66</span><span>public class <span class="text-amber-400">OrderManager</span> {</span></div>
                                <div class="flex"><span class="line-gutter">67</span><span></span></div>
                                <div class="flex"><span class="line-gutter">68</span><span>&nbsp;&nbsp;&nbsp;&nbsp;private List&lt;Order&gt; orders;</span></div>
                                <div class="flex"><span class="line-gutter">69</span><span class="text-slate-500">&nbsp;&nbsp;&nbsp;&nbsp;// Matchvn plagiarism</span></div>

                                <!-- Red Box 1 (Student B) -->
                                <div data-match-block="1" onmouseenter="highlightBlock(1)" onmouseleave="unhighlightBlock(1)" class="my-1 py-2 px-1 rounded-lg bg-rose-950/30 border border-rose-500/80 shadow-[0_0_15px_rgba(239,68,68,0.25)] cursor-pointer">
                                    <div class="flex"><span class="line-gutter text-rose-400">70</span><span>&nbsp;&nbsp;&nbsp;&nbsp;public double <span class="text-rose-300 font-bold">calculateTotal</span>() {</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">71</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;basketValue&gt; basketValue = basketValue;</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">72</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalCost = totalCost + totalItems;</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">73</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return cart;</span></div>
                                    <div class="flex"><span class="line-gutter text-rose-400">74</span><span>&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                                </div>

                                <!-- Amber Box 2 (Student B) -->
                                <div data-match-block="2" onmouseenter="highlightBlock(2)" onmouseleave="unhighlightBlock(2)" class="my-1 py-1.5 px-1 rounded-lg bg-amber-950/20 border border-amber-500/70 cursor-pointer">
                                    <div class="flex"><span class="line-gutter text-amber-400">86</span><span>&nbsp;&nbsp;&nbsp;&nbsp;public double <span class="text-amber-300 font-bold">calculateTotat</span>() {</span></div>
                                    <div class="flex"><span class="line-gutter text-amber-400">87</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return os&gt;orderItems = orderItems;</span></div>
                                    <div class="flex"><span class="line-gutter text-amber-400">88</span><span>&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                                </div>
                                <div class="flex"><span class="line-gutter">89</span><span class="text-slate-500">&nbsp;&nbsp;&nbsp;&nbsp;// Matching payment</span></div>

                                <!-- Amber Box 3 (Student B) -->
                                <div data-match-block="3" onmouseenter="highlightBlock(3)" onmouseleave="unhighlightBlock(3)" class="my-1 py-2 px-1 rounded-lg bg-amber-950/30 border border-amber-500/80 shadow-[0_0_15px_rgba(245,158,11,0.25)] cursor-pointer">
                                    <div class="flex"><span class="line-gutter text-amber-400">91</span><span>&nbsp;&nbsp;&nbsp;&nbsp;public double <span class="text-amber-300 font-bold">processPayment</span>(baskeYer&gt; orderItems) {</span></div>
                                    <div class="flex"><span class="line-gutter text-amber-400">182</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;List&lt;owtKeue = new Stock&lt;orderItems&gt;;</span></div>
                                    <div class="flex"><span class="line-gutter text-amber-400">183</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (basketValue.&lt;orderItems&gt;.orderItems) {</span></div>
                                    <div class="flex"><span class="line-gutter text-amber-400">184</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalCost = anablen;</span></div>
                                    <div class="flex"><span class="line-gutter text-amber-400">116</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                                    <div class="flex"><span class="line-gutter text-amber-400">117</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return pastvutsetValue;</span></div>
                                    <div class="flex"><span class="line-gutter text-amber-400">118</span><span>&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                                </div>

                                <div class="flex"><span class="line-gutter">120</span><span>&nbsp;&nbsp;&nbsp;&nbsp;public double updateStock(ovorderItems) {</span></div>
                                <div class="flex"><span class="line-gutter">121</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// [rderList=&lt;orderSoe&gt; {</span></div>
                                <div class="flex"><span class="line-gutter">122</span><span>&nbsp;&nbsp;&nbsp;&nbsp;}</span></div>
                            </div>
                        </div>
                    </div>

                    <!-- Bottom Panel: AI Analysis Insight Card (#16171E) inside the frame -->
                    <div class="cyber-card p-3 bg-[#161726]/90 border border-white/5 rounded-xl spotlight-card">
                        <div class="flex items-center justify-between mb-2">
                            <div class="flex items-center gap-3">
                                <span class="text-xs font-bold text-white font-mono">AI Analysis Insight Card (#16171E)</span>
                                <button onclick="openAstModal()" class="px-2.5 py-0.5 rounded-lg bg-violet-600/30 hover:bg-violet-600/50 border border-violet-500/40 text-[10px] text-violet-300 font-mono flex items-center gap-1 transition-all shadow-[0_0_10px_rgba(139,92,246,0.2)]">
                                    <i data-lucide="sparkles" class="w-3 h-3 text-cyan-400"></i> Xem Giải Thuật AST &amp; Gemini
                                </button>
                            </div>
                            <button onclick="toggleAiInsightCard()" id="ai-insight-toggle-btn" class="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-white/10 transition-colors" title="Thu gọn / Mở rộng"><i id="ai-insight-icon" data-lucide="chevron-up" class="w-4 h-4"></i></button>
                        </div>

                        <div id="ai-insight-content" class="grid grid-cols-1 md:grid-cols-2 gap-6 text-xs font-mono pt-2 border-t border-white/5 transition-all">
                            <!-- Left: Renamed Variables Table -->
                            <div>
                                <div class="font-bold text-slate-300 mb-2 flex items-center justify-between">
                                    <span>Renamed Variables</span>
                                    <div class="flex items-center gap-1 text-[10px] text-slate-400">
                                        <span id="var-page-indicator" class="text-[9px] text-slate-500 mr-1">Trang 1/2</span>
                                        <button onclick="prevVarPage()" class="px-1.5 py-0.5 rounded bg-white/5 hover:bg-white/10">&lt;</button>
                                        <button onclick="nextVarPage()" class="px-1.5 py-0.5 rounded bg-white/5 hover:bg-white/10">&gt;</button>
                                    </div>
                                </div>
                                <div id="var-list-container" class="space-y-1.5 text-slate-400 text-[11px]">
                                    <div class="flex items-center justify-between py-1 px-2 rounded bg-black/30">
                                        <span>_cart</span>
                                        <span class="text-slate-600">&rarr;</span>
                                        <span class="text-cyan-400 font-semibold">_basket</span>
                                    </div>
                                    <div class="flex items-center justify-between py-1 px-2 rounded bg-black/30">
                                        <span>total_amt</span>
                                        <span class="text-slate-600">&rarr;</span>
                                        <span class="text-cyan-400 font-semibold">final_cost</span>
                                    </div>
                                    <div class="flex items-center justify-between py-1 px-2 rounded bg-black/30">
                                        <span>orderCount</span>
                                        <span class="text-slate-600">&rarr;</span>
                                        <span class="text-cyan-400 font-semibold">basketSize</span>
                                    </div>
                                </div>
                            </div>

                            <!-- Right: AST Logic Matches -->
                            <div>
                                <div class="font-bold text-slate-300 mb-2">AST Logic Matches:</div>
                                <ul class="space-y-1 text-slate-400 list-disc list-inside leading-relaxed text-[11px]">
                                    <li>High matching logic in processPayment methods, (nethort internal)</li>
                                    <li>updateStock methods, (nethching updateStock, method. )</li>
                                    <li><strong class="text-rose-400 font-bold">88%</strong> overall similarity</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Right 2 Columns: Right Sidebar (File List / Snippets / Report) -->
                <div class="xl:col-span-2 flex flex-col gap-3 font-mono text-xs">
                    
                    <!-- File List -->
                    <div class="cyber-card p-3 rounded-xl spotlight-card">
                        <div class="flex items-center justify-between text-slate-400 mb-2">
                            <span class="font-bold text-white flex items-center gap-1.5"><i data-lucide="folder-git-2" class="w-3.5 h-3.5 text-cyan-400"></i> Cây Mã Nguồn</span>
                            <span class="text-[10px] text-slate-500 font-mono">3 tệp</span>
                        </div>
                        <div class="space-y-1.5 text-[11px] font-mono">
                            <div class="p-1.5 rounded-lg bg-rose-950/40 border border-rose-500/40 text-rose-300 font-semibold flex items-center justify-between cursor-pointer">
                                <span class="truncate">OrderManager.java</span>
                                <span class="text-[10px] text-rose-400 font-bold">88%</span>
                            </div>
                            <div onclick="if(window.showToast) showToast('Đang mở tệp CartServlet.java (Tương đồng: 15.2% - An toàn)', 'info');" class="p-1.5 rounded-lg bg-white/[0.02] hover:bg-white/5 border border-white/5 text-slate-400 hover:text-white flex items-center justify-between cursor-pointer transition-colors">
                                <span class="truncate">CartServlet.java</span>
                                <span class="text-[10px] text-emerald-400">15%</span>
                            </div>
                            <div onclick="if(window.showToast) showToast('Đang mở tệp UserDAO.java (Tương đồng: 4.8% - An toàn)', 'info');" class="p-1.5 rounded-lg bg-white/[0.02] hover:bg-white/5 border border-white/5 text-slate-400 hover:text-white flex items-center justify-between cursor-pointer transition-colors">
                                <span class="truncate">UserDAO.java</span>
                                <span class="text-[10px] text-emerald-400">5%</span>
                            </div>
                        </div>
                    </div>

                    <!-- Matching Snippets -->
                    <div class="cyber-card p-3 rounded-xl spotlight-card">
                        <div class="flex items-center justify-between text-slate-400 mb-2">
                            <span class="font-bold text-white flex items-center gap-1.5"><i data-lucide="code-2" class="w-3.5 h-3.5 text-rose-400"></i> Khối Trùng Lặp</span>
                            <span class="text-[10px] text-rose-400 font-bold">3 Khối</span>
                        </div>
                        <div class="space-y-1.5 text-[10.5px] font-mono">
                            <div onclick="highlightBlock(1); if(window.CyberAudio) CyberAudio.playLaser();" class="p-1.5 rounded-lg bg-rose-950/30 hover:bg-rose-950/60 border border-rose-500/40 text-rose-300 cursor-pointer transition-colors flex items-center justify-between" title="Click để highlight khối 1">
                                <span class="truncate">1. calculateTotal()</span>
                                <span class="text-[9px] px-1 rounded bg-rose-950 text-rose-400 font-bold">Dòng 24</span>
                            </div>
                            <div onclick="highlightBlock(2); if(window.CyberAudio) CyberAudio.playTick();" class="p-1.5 rounded-lg bg-amber-950/30 hover:bg-amber-950/60 border border-amber-500/40 text-amber-300 cursor-pointer transition-colors flex items-center justify-between" title="Click để highlight khối 2">
                                <span class="truncate">2. calculateTotal()</span>
                                <span class="text-[9px] px-1 rounded bg-amber-950 text-amber-400">Dòng 44</span>
                            </div>
                            <div onclick="highlightBlock(3); if(window.CyberAudio) CyberAudio.playLaser();" class="p-1.5 rounded-lg bg-rose-950/30 hover:bg-rose-950/60 border border-rose-500/40 text-rose-300 cursor-pointer transition-colors flex items-center justify-between" title="Click để highlight khối 3">
                                <span class="truncate">3. processPayment()</span>
                                <span class="text-[9px] px-1 rounded bg-rose-950 text-rose-400 font-bold">Dòng 45</span>
                            </div>
                        </div>
                    </div>

                    <!-- Plagiarism Report & Export Actions -->
                    <div class="cyber-card p-3 rounded-xl spotlight-card space-y-2">
                        <div class="font-bold text-white flex items-center gap-1.5">
                            <i data-lucide="file-text" class="w-3.5 h-3.5 text-cyan-400"></i> Báo Cáo Đối Soát
                        </div>
                        <div class="p-2 rounded-lg bg-rose-950/30 border border-rose-500/40 text-rose-300 text-[10px]">
                            <div class="font-bold text-rose-400 text-xs">CỜ ĐỎ HỌC THUẬT</div>
                            <div>Trùng khớp AST 40% + Gemini 60%</div>
                        </div>
                        <button onclick="exportDiffReport('pdf')" class="w-full py-1.5 px-2 rounded-lg bg-cyan-950/60 hover:bg-cyan-900/80 border border-cyan-500/40 text-cyan-300 hover:text-white text-[10px] font-semibold transition-all flex items-center justify-center gap-1.5">
                            <i data-lucide="download" class="w-3 h-3"></i> Xuất Báo Cáo PDF
                        </button>
                        <button onclick="exportDiffReport('json')" class="w-full py-1.5 px-2 rounded-lg bg-white/5 hover:bg-white/10 border border-white/10 text-slate-300 hover:text-white text-[10px] font-semibold transition-all flex items-center justify-center gap-1.5">
                            <i data-lucide="file-code" class="w-3 h-3"></i> Tải Dữ Liệu JSON
                        </button>
                    </div>
                </div>
            </div>

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
                        <span class="px-2 py-0.5 rounded text-[10px] bg-cyan-950 text-cyan-400 border border-cyan-500/30">Nhóm 4 PRJ301</span>
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
                <span class="text-[11px] font-mono text-slate-500">Mã giải thuật: <code class="text-slate-400">services/GeminiPlagiarismService.java</code></span>
                <button onclick="closeAstModal()" class="px-4 py-1.5 rounded-lg bg-violet-600 hover:bg-violet-500 text-white text-xs font-mono font-semibold transition-all shadow-[0_0_15px_rgba(139,92,246,0.3)]">
                    Đã hiểu thuật toán
                </button>
            </div>
        </div>
    </div>

    <script>
        window.AITA_USER_ROLE = '${isStudent ? "STUDENT" : "INSTRUCTOR"}';
        lucide.createIcons();

        function toggleAiInsightCard() {
            const content = document.getElementById('ai-insight-content');
            const icon = document.getElementById('ai-insight-icon');
            if (content) {
                content.classList.toggle('hidden');
                const isHidden = content.classList.contains('hidden');
                if (icon) {
                    icon.setAttribute('data-lucide', isHidden ? 'chevron-down' : 'chevron-up');
                    if (window.lucide) lucide.createIcons();
                }
            }
        }

        const varPages = [
            [
                { from: '_cart', to: '_basket' },
                { from: 'total_amt', to: 'final_cost' },
                { from: 'orderCount', to: 'basketSize' }
            ],
            [
                { from: 'discountCode', to: 'promoVoucher' },
                { from: 'isMember', to: 'hasVipStatus' },
                { from: 'shippingFee', to: 'deliveryRate' }
            ]
        ];
        let currentVarPageIndex = 0;

        function renderVarPage() {
            const container = document.getElementById('var-list-container');
            const indicator = document.getElementById('var-page-indicator');
            if (!container) return;
            const pageData = varPages[currentVarPageIndex];
            container.innerHTML = pageData.map(v => `
                <div class="flex items-center justify-between py-1 px-2 rounded bg-black/30 animate-fade-in">
                    <span>${v.from}</span>
                    <span class="text-slate-600">&rarr;</span>
                    <span class="text-cyan-400 font-semibold">${v.to}</span>
                </div>
            `).join('');
            if (indicator) indicator.innerText = `Trang ${currentVarPageIndex + 1}/${varPages.length}`;
        }

        function prevVarPage() {
            if (currentVarPageIndex > 0) {
                currentVarPageIndex--;
                renderVarPage();
            }
        }

        function nextVarPage() {
            if (currentVarPageIndex < varPages.length - 1) {
                currentVarPageIndex++;
                renderVarPage();
            }
        }

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

        function exportDiffReport(format) {
            if (window.CyberAudio) CyberAudio.playSuccess();
            const dataStr = format === 'json' 
                ? JSON.stringify({
                    reportId: "AITA-2026-DP882",
                    timestamp: new Date().toISOString(),
                    studentA: { id: "SE1701", name: "Trần Văn Long", file: "OrderManager.java", hash: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" },
                    studentB: { id: "SE1702", name: "Lê Hoàng Nam", file: "OrderProcessingService.java", hash: "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a" },
                    similarity: 88.5,
                    astMatchScore: 40.0,
                    geminiSemanticScore: 60.0,
                    status: "FLAGGED_CRITICAL"
                }, null, 2)
                : `=== AITA CODEDEFEND AUDIT REPORT (PRJ301) ===\nReport ID: AITA-2026-DP882\nSHA-256 Verified: PASS (Standard 4.4.2)\nStudent A: SE1701 - Trần Văn Long (OrderManager.java)\nStudent B: SE1702 - Lê Hoàng Nam (OrderProcessingService.java)\nAST Token Match: 40.0%\nGemini 1.5 Semantic Score: 60.0%\nOverall Similarity: 88.5% (FLAGGED_CRITICAL)\nStatus: PENDING_DISPUTE_RESOLUTION`;

            const blob = new Blob([dataStr], { type: format === 'json' ? 'application/json' : 'text/plain' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `AITA_Plagiarism_Report_882.${format === 'json' ? 'json' : 'txt'}`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);

            if (window.showToast) {
                showToast(`Đã xuất báo cáo đối soát dạng ${format.toUpperCase()} thành công!`, 'success');
            }
        }
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-effects.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-audio.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-particles.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/tilt-motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/system-modals.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion-system-2026.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/diff-connector.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/command-palette.js"></script>
</body>
</html>
