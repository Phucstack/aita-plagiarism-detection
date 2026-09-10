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
                <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/5 transition-all">
                    <i data-lucide="split-square-vertical" class="w-4 h-4"></i> Diff Inspector
                </a>
            </nav>
        </div>

        <!-- Right Profile & Utilities Section -->
        <div class="flex items-center gap-3 sm:gap-4">
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
                <img src="${sessionScope.currentUser.avatarUrl != null ? sessionScope.currentUser.avatarUrl : 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80'}" alt="Avatar" class="w-8 h-8 rounded-full border border-cyan-400/60 object-cover shadow-[0_0_10px_rgba(6,182,212,0.3)]">
                <div class="text-left leading-tight hidden sm:block text-xs">
                    <div class="font-semibold text-white">${sessionScope.currentUser.fullName != null ? sessionScope.currentUser.fullName : 'TS. Nguyễn Hoàng Hà'}</div>
                    <div class="text-[10px] text-slate-400 font-mono">${sessionScope.currentUser.email != null ? sessionScope.currentUser.email : 'ha.nh@fpt.edu.vn'}</div>
                </div>
                <a href="${pageContext.request.contextPath}/logout" class="ml-2 text-slate-400 hover:text-rose-400 transition-colors" title="Đăng xuất">
                    <i data-lucide="log-out" class="w-4 h-4"></i>
                </a>
            </div>
        </div>
    </header>

    <div class="flex-1 flex min-h-0 overflow-hidden">
        <!-- Master Left Sidebar (Unified Across All Screens) -->
        <aside id="master-sidebar" class="w-56 border-r border-white/10 bg-[#0c0e1d] flex flex-col py-5 px-3.5 gap-1 shrink-0 select-none h-full overflow-y-auto custom-sidebar-scroll">
            <div class="sidebar-heading text-[10px] font-mono text-slate-500 px-3 mb-2 uppercase tracking-wider font-semibold">Điều Hướng Chính</div>
            
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

            <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 font-medium text-xs transition-all" title="Diff Inspector">
                <div class="flex items-center gap-3">
                    <i data-lucide="split-square-vertical" class="w-4 h-4 shrink-0"></i>
                    <span class="sidebar-label">Diff Inspector</span>
                </div>
                <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-rose-500/20 text-rose-400 border border-rose-500/30">88% Risk</span>
            </a>

            <button onclick="openAstModal()" class="sidebar-nav-item w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-violet-300 hover:bg-violet-950/20 font-medium text-xs transition-all text-left" title="AST &amp; Gemini AI">
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

            <!-- Sidebar Bottom Info Box -->
            <div class="mt-auto pt-4 border-t border-white/5 space-y-2.5">
                <!-- École 42 Peer-Review & Correction Points (Thỏa mãn RBL Mục 2) -->
                <div class="p-2.5 rounded-xl bg-[#080914] border border-violet-500/30 text-xs font-mono space-y-1.5 shadow-sm">
                    <div class="flex items-center justify-between text-violet-300 font-bold">
                        <span class="flex items-center gap-1 text-[11px]"><i data-lucide="award" class="w-3.5 h-3.5"></i> École 42</span>
                        <span class="text-[9px] px-1.5 py-0.5 rounded bg-violet-950 text-violet-300 border border-violet-500/40">5 Pts</span>
                    </div>
                    <div class="text-[9px] text-slate-400 flex items-center justify-between">
                        <span>Chấm chéo:</span>
                        <span class="text-cyan-300 font-semibold">G4 &rarr; G5</span>
                    </div>
                    <div class="text-[8.5px] text-emerald-400 font-mono flex items-center gap-1 border-t border-white/5 pt-1">
                        <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span> Docker Sandbox 4.5.2 Pass
                    </div>
                </div>

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

        <main class="flex-1 p-6 h-full min-h-0 overflow-y-auto max-w-[1600px] mx-auto w-full">
            <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
                <div>
                    <h1 class="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
                        Dashboard Giám Sát Đạo Văn
                        <span class="text-xs font-mono font-normal px-2 py-0.5 rounded bg-cyan-950/60 border border-cyan-500/30 text-cyan-300">PRJ301</span>
                    </h1>
                    <p class="text-xs text-slate-400 mt-1">Quản lý đối soát cây cú pháp AST và tính liêm chính bài tập thực hành.</p>
                </div>
                
                <!-- Dynamic Course & Assignment Selectors (Course Management) -->
                <form action="${pageContext.request.contextPath}/dashboard" method="GET" class="flex flex-wrap items-center gap-2 text-xs font-mono">
                    <div class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#131424] border border-white/10 text-slate-300">
                        <i data-lucide="book-open" class="w-3.5 h-3.5 text-cyan-400"></i>
                        <select name="courseId" onchange="this.form.submit()" class="bg-transparent border-none text-white focus:outline-none text-xs cursor-pointer">
                            <c:forEach var="c" items="${courses}">
                                <option value="${c.courseId}" ${c.courseId == selectedCourseId ? 'selected' : ''} class="bg-[#131424] text-white">
                                    ${c.courseCode} - ${c.courseName}
                                </option>
                            </c:forEach>
                            <c:if test="${empty courses}">
                                <option value="1" class="bg-[#131424] text-white">PRJ301 - Java Web Application</option>
                            </c:if>
                        </select>
                    </div>

                    <div class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#131424] border border-white/10 text-slate-300">
                        <i data-lucide="file-text" class="w-3.5 h-3.5 text-violet-400"></i>
                        <select name="assignmentId" onchange="this.form.submit()" class="bg-transparent border-none text-white focus:outline-none text-xs cursor-pointer">
                            <c:forEach var="a" items="${assignments}">
                                <option value="${a.assignmentId}" ${a.assignmentId == selectedAssignmentId ? 'selected' : ''} class="bg-[#131424] text-white">
                                    ${a.title}
                                </option>
                            </c:forEach>
                            <c:if test="${empty assignments}">
                                <option value="1" class="bg-[#131424] text-white">Assignment 2 - E-Commerce Cart</option>
                            </c:if>
                        </select>
                    </div>
                </form>
            </div>

            <!-- Infinite Two-Row Tech Marquee (ai-kinetic-3d-web) -->
            <div class="mb-6 rounded-2xl bg-[#0a0c18]/80 border border-white/5 p-3 overflow-hidden space-y-2 marquee-mask-fade shadow-lg backdrop-blur-md">
                <!-- Track 1: Running Left -->
                <div class="marquee-track flex gap-4 whitespace-nowrap animate-marquee-left">
                    <div class="flex items-center gap-4 text-xs font-mono">
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-cyan-500/20 text-cyan-300 flex items-center gap-2">
                            <i data-lucide="cpu" class="w-3.5 h-3.5 text-cyan-400"></i> JavaParser 3.25 AST Engine
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-violet-500/20 text-violet-300 flex items-center gap-2">
                            <i data-lucide="sparkles" class="w-3.5 h-3.5 text-violet-400"></i> Google Gemini 1.5 Pro Semantic Reasoning
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-emerald-500/20 text-emerald-300 flex items-center gap-2">
                            <i data-lucide="shield-check" class="w-3.5 h-3.5 text-emerald-400"></i> Docker Isolation Sandbox (Mục 4.5.2)
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-amber-500/20 text-amber-300 flex items-center gap-2">
                            <i data-lucide="award" class="w-3.5 h-3.5 text-amber-400"></i> École 42 Peer Grading Model
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-sky-500/20 text-sky-300 flex items-center gap-2">
                            <i data-lucide="graduation-cap" class="w-3.5 h-3.5 text-sky-400"></i> FPT University Academic Integrity
                        </span>
                    </div>
                    <!-- Duplicate for seamless infinite loop -->
                    <div class="flex items-center gap-4 text-xs font-mono" aria-hidden="true">
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-cyan-500/20 text-cyan-300 flex items-center gap-2">
                            <i data-lucide="cpu" class="w-3.5 h-3.5 text-cyan-400"></i> JavaParser 3.25 AST Engine
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-violet-500/20 text-violet-300 flex items-center gap-2">
                            <i data-lucide="sparkles" class="w-3.5 h-3.5 text-violet-400"></i> Google Gemini 1.5 Pro Semantic Reasoning
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-emerald-500/20 text-emerald-300 flex items-center gap-2">
                            <i data-lucide="shield-check" class="w-3.5 h-3.5 text-emerald-400"></i> Docker Isolation Sandbox (Mục 4.5.2)
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-amber-500/20 text-amber-300 flex items-center gap-2">
                            <i data-lucide="award" class="w-3.5 h-3.5 text-amber-400"></i> École 42 Peer Grading Model
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-sky-500/20 text-sky-300 flex items-center gap-2">
                            <i data-lucide="graduation-cap" class="w-3.5 h-3.5 text-sky-400"></i> FPT University Academic Integrity
                        </span>
                    </div>
                </div>

                <!-- Track 2: Running Right (Reverse) -->
                <div class="marquee-track flex gap-4 whitespace-nowrap animate-marquee-right">
                    <div class="flex items-center gap-4 text-xs font-mono">
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-emerald-500/20 text-emerald-300 flex items-center gap-2">
                            <i data-lucide="hash" class="w-3.5 h-3.5 text-emerald-400"></i> SHA-256 Anti-Tamper Digest (Mục 4.4.2)
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-cyan-500/20 text-cyan-300 flex items-center gap-2">
                            <i data-lucide="radar" class="w-3.5 h-3.5 text-cyan-400"></i> Sonar Active Plagiarism Radar
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-indigo-500/20 text-indigo-300 flex items-center gap-2">
                            <i data-lucide="key" class="w-3.5 h-3.5 text-indigo-400"></i> RFC-7519 JWT Session Security
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-rose-500/20 text-rose-300 flex items-center gap-2">
                            <i data-lucide="git-commit" class="w-3.5 h-3.5 text-rose-400"></i> AST Token Renaming Normalizer
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-amber-500/20 text-amber-300 flex items-center gap-2">
                            <i data-lucide="scale" class="w-3.5 h-3.5 text-amber-400"></i> In-Place AST Diff Inspection
                        </span>
                    </div>
                    <!-- Duplicate for seamless infinite loop -->
                    <div class="flex items-center gap-4 text-xs font-mono" aria-hidden="true">
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-emerald-500/20 text-emerald-300 flex items-center gap-2">
                            <i data-lucide="hash" class="w-3.5 h-3.5 text-emerald-400"></i> SHA-256 Anti-Tamper Digest (Mục 4.4.2)
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-cyan-500/20 text-cyan-300 flex items-center gap-2">
                            <i data-lucide="radar" class="w-3.5 h-3.5 text-cyan-400"></i> Sonar Active Plagiarism Radar
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-indigo-500/20 text-indigo-300 flex items-center gap-2">
                            <i data-lucide="key" class="w-3.5 h-3.5 text-indigo-400"></i> RFC-7519 JWT Session Security
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-rose-500/20 text-rose-300 flex items-center gap-2">
                            <i data-lucide="git-commit" class="w-3.5 h-3.5 text-rose-400"></i> AST Token Renaming Normalizer
                        </span>
                        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-amber-500/20 text-amber-300 flex items-center gap-2">
                            <i data-lucide="scale" class="w-3.5 h-3.5 text-amber-400"></i> In-Place AST Diff Inspection
                        </span>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-12 gap-5 mb-6">
                <!-- Card 1: Radar -->
                <div class="lg:col-span-5 cyber-card p-5 flex flex-col justify-between relative overflow-hidden spotlight-card border-beam-container">
                    <div class="flex items-center justify-between">
                        <div>
                            <h3 class="text-sm font-semibold text-white">Analyzing Scan</h3>
                            <p class="text-xs text-slate-500">Student Submissions</p>
                        </div>
                        <span class="text-[11px] px-2.5 py-0.5 rounded-full border border-white/10 bg-white/5 text-slate-400 font-mono">Active Scan</span>
                    </div>

                    <!-- Circular Radar Scope with Concentric Circles, Compass Graduation & Azimuth Crosshairs -->
                    <div class="flex justify-center my-3 relative items-center">
                        <div class="absolute w-[240px] h-[240px] rounded-full border border-dashed border-violet-500/40 pointer-events-none shadow-[0_0_20px_rgba(139,92,246,0.15)] flex items-center justify-center">
                            <div class="absolute inset-1.5 rounded-full border border-violet-500/20"></div>
                            <div class="absolute top-1 text-[8px] font-mono text-violet-400/80">0°</div>
                            <div class="absolute right-1 text-[8px] font-mono text-violet-400/80">90°</div>
                            <div class="absolute bottom-1 text-[8px] font-mono text-violet-400/80">180°</div>
                            <div class="absolute left-1 text-[8px] font-mono text-violet-400/80">270°</div>
                        </div>

                        <div class="radar-circle flex items-center justify-center z-10">
                            <div class="absolute w-[160px] h-[160px] rounded-full border border-violet-500/20"></div>
                            <div class="absolute w-[100px] h-[100px] rounded-full border border-violet-500/20"></div>
                            <div class="absolute w-[40px] h-[40px] rounded-full border border-violet-500/20"></div>
                            <div class="absolute w-full h-[1px] bg-violet-500/20"></div>
                            <div class="absolute h-full w-[1px] bg-violet-500/20"></div>
                            <div class="radar-beam"></div>
                            <div class="absolute top-10 left-12 w-2.5 h-2.5 rounded-full bg-violet-400 radar-dot shadow-[0_0_10px_#a855f7]"></div>
                            <div class="absolute top-16 right-10 w-2 h-2 rounded-full bg-cyan-400 radar-dot shadow-[0_0_10px_#06b6d4]"></div>
                            <div class="absolute bottom-12 left-16 w-2 h-2 rounded-full bg-pink-400 radar-dot shadow-[0_0_10px_#ec4899]"></div>
                            <div class="absolute bottom-9 right-16 w-3 h-3 rounded-full bg-cyan-400 radar-dot shadow-[0_0_12px_#06b6d4]"></div>
                        </div>
                    </div>

                    <div class="flex items-center justify-between pt-3 border-t border-white/5 text-xs font-mono">
                        <div>
                            <span class="text-slate-500 block text-[10px]">Matching</span>
                            <span class="text-pink-400 font-bold">88% MATCHED</span>
                        </div>
                        <div class="text-right">
                            <span class="text-slate-500 block text-[10px]">Analyzing</span>
                            <span class="text-cyan-400 font-bold">Users: 1,200</span>
                        </div>
                    </div>
                </div>

                <!-- Card 2: Stats -->
                <div class="lg:col-span-3 flex flex-col justify-between gap-3">
                    <div class="cyber-card p-3.5 flex items-center justify-between spotlight-card cursor-pointer hover:border-cyan-500/40 transition-colors">
                        <div>
                            <span class="text-[11px] text-slate-400 block">Active Scans</span>
                            <span class="text-xl font-bold font-mono text-white" data-counter-target="15">0</span>
                        </div>
                        <span class="text-[11px] font-mono px-2.5 py-0.5 rounded-full border border-cyan-500/40 text-cyan-400 bg-cyan-950/20">LIVE</span>
                    </div>

                    <div class="cyber-card p-3.5 flex items-center justify-between spotlight-card cursor-pointer hover:border-violet-500/40 transition-colors">
                        <div>
                            <span class="text-[11px] text-slate-400 block">Similarity Flags</span>
                            <span class="text-xl font-bold font-mono text-white" data-counter-target="42">0</span>
                        </div>
                        <span class="text-[11px] font-mono px-2.5 py-0.5 rounded-full border border-violet-500/40 text-violet-400 bg-violet-950/20">FLAG</span>
                    </div>

                    <div class="cyber-card p-3.5 flex items-center justify-between spotlight-card cursor-pointer hover:border-pink-500/40 transition-colors">
                        <div>
                            <span class="text-[11px] text-slate-400 block">Avg. Match %</span>
                            <span class="text-xl font-bold font-mono text-white" data-counter-target="35" data-counter-suffix="%">0%</span>
                        </div>
                        <span class="text-[11px] font-mono px-2.5 py-0.5 rounded-full border border-pink-500/40 text-pink-400 bg-pink-950/20">ALERT</span>
                    </div>

                    <div class="cyber-card p-3.5 flex items-center justify-between spotlight-card cursor-pointer hover:border-cyan-500/40 transition-colors">
                        <div>
                            <span class="text-[11px] text-slate-400 block">Users</span>
                            <span class="text-xl font-bold font-mono text-white" data-counter-target="1200">0</span>
                        </div>
                        <span class="text-[11px] font-mono px-2.5 py-0.5 rounded-full border border-cyan-500/40 text-cyan-400 bg-cyan-950/20">TOTAL</span>
                    </div>
                </div>

                <!-- Card 3: Chart -->
                <div class="lg:col-span-4 cyber-card p-5 flex flex-col justify-between spotlight-card">
                    <div class="flex items-center justify-between">
                        <h3 class="text-sm font-semibold text-white">Similarity Trends Over Time</h3>
                        <span class="text-[11px] px-2 py-0.5 rounded-md border border-white/10 bg-white/5 text-slate-400 font-mono">Animated Chart</span>
                    </div>

                    <div class="my-3 relative">
                        <div class="flex items-center justify-between text-[10px] font-mono text-slate-500 mb-1">
                            <span>120</span>
                        </div>
                        <svg class="w-full h-36" viewBox="0 0 400 150" fill="none">
                            <defs>
                                <linearGradient id="cyanGradJsp" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="0%" stop-color="#06b6d4" stop-opacity="0.35"/>
                                    <stop offset="100%" stop-color="#06b6d4" stop-opacity="0"/>
                                </linearGradient>
                                <linearGradient id="violetGradJsp" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="0%" stop-color="#a855f7" stop-opacity="0.3"/>
                                    <stop offset="100%" stop-color="#a855f7" stop-opacity="0"/>
                                </linearGradient>
                            </defs>
                            <line x1="0" y1="30" x2="400" y2="30" stroke="rgba(255,255,255,0.05)" stroke-dasharray="3 3"/>
                            <line x1="0" y1="70" x2="400" y2="70" stroke="rgba(255,255,255,0.05)" stroke-dasharray="3 3"/>
                            <line x1="0" y1="110" x2="400" y2="110" stroke="rgba(255,255,255,0.05)" stroke-dasharray="3 3"/>

                            <path d="M0,145 C40,125 70,85 100,90 C140,95 160,70 200,60 C240,50 260,25 300,20 C340,15 360,55 400,45 L400,150 L0,150 Z" fill="url(#violetGradJsp)" />
                            <path d="M0,145 C40,125 70,85 100,90 C140,95 160,70 200,60 C240,50 260,25 300,20 C340,15 360,55 400,45" stroke="#c084fc" stroke-width="2.5" stroke-linecap="round"/>

                            <path d="M0,140 C40,105 70,55 110,65 C150,75 190,45 230,55 C270,65 310,15 350,15 C370,15 385,25 400,10 L400,150 L0,150 Z" fill="url(#cyanGradJsp)" />
                            <path d="M0,140 C40,105 70,55 110,65 C150,75 190,45 230,55 C270,65 310,15 350,15 C370,15 385,25 400,10" stroke="#06b6d4" stroke-width="2.5" stroke-linecap="round"/>
                        </svg>
                    </div>

                    <div class="flex items-center justify-between text-[10px] font-mono text-slate-500 pt-2 border-t border-white/5">
                        <span>Jan</span><span>Feb</span><span>Mar</span><span>Apr</span><span>May</span><span>Jun</span><span>Jul</span>
                    </div>
                </div>
            </div>

            <!-- Bento Row 2: Table & Snippets -->
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-5">
                <div class="lg:col-span-8 cyber-card p-5 spotlight-card">
                    <div class="flex items-center justify-between mb-4">
                        <h3 class="text-sm font-bold text-white flex items-center gap-2">Live threat Detection</h3>
                        <div class="relative inline-block text-left">
                            <button onclick="toggleSubFilterMenu(event)" class="text-xs px-2.5 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-slate-300 border border-white/10 hover:border-cyan-500/40 flex items-center gap-1 font-mono transition-colors">
                                <span id="current-sub-filter">All 5 Submissions</span> <i data-lucide="chevron-down" class="w-3.5 h-3.5"></i>
                            </button>
                            <div id="sub-filter-menu" class="hidden absolute right-0 mt-1 w-44 rounded-xl bg-[#0e101d] border border-white/10 shadow-2xl py-1 z-30 font-mono text-xs">
                                <button type="button" onclick="selectSubFilter('All 5 Submissions', this)" class="w-full text-left px-3 py-1.5 text-cyan-400 bg-cyan-950/30">All 5 Submissions</button>
                                <button type="button" onclick="selectSubFilter('Flagged Only (1)', this)" class="w-full text-left px-3 py-1.5 text-rose-300 hover:bg-white/5">Flagged Only (1)</button>
                                <button type="button" onclick="selectSubFilter('Clean Only (4)', this)" class="w-full text-left px-3 py-1.5 text-emerald-300 hover:bg-white/5">Clean Only (4)</button>
                            </div>
                        </div>
                    </div>

                    <div class="grid grid-cols-12 text-[11px] font-mono text-slate-500 pb-2 border-b border-white/5 px-3">
                        <span class="col-span-4">Student</span>
                        <span class="col-span-4">Assignment</span>
                        <span class="col-span-2">Matching %</span>
                        <span class="col-span-2 text-right">Timestamp</span>
                    </div>

                    <div class="divide-y divide-white/5 text-xs font-mono mt-1">
                        <div onclick="window.location.href='${pageContext.request.contextPath}/diff-inspector?reportId=1'" class="grid grid-cols-12 items-center py-2.5 px-3 hover:bg-white/[0.05] rounded-lg transition-colors cursor-pointer group" title="Nhấp để mở Diff Inspector đối soát chi tiết">
                            <div class="col-span-4 flex items-center gap-2 text-slate-300 group-hover:text-cyan-300 transition-colors">
                                <i data-lucide="user" class="w-4 h-4 text-slate-500 group-hover:text-cyan-400"></i>
                                <div><div class="font-medium">Student Namer ID</div><div class="text-[10px] text-slate-500">Student 13701</div></div>
                            </div>
                            <div class="col-span-4 text-slate-400">Assignment Title - Testarchant 1</div>
                            <div class="col-span-2 text-slate-300 font-bold">12%</div>
                            <div class="col-span-2 text-right text-slate-500 flex items-center justify-end gap-1">2023-10-25 13:43 <i data-lucide="chevron-right" class="w-3.5 h-3.5 text-slate-600 group-hover:text-cyan-400 transition-colors"></i></div>
                        </div>

                        <div onclick="window.location.href='${pageContext.request.contextPath}/diff-inspector?reportId=1'" class="grid grid-cols-12 items-center py-2.5 px-3 hover:bg-white/[0.05] rounded-lg transition-colors cursor-pointer group" title="Nhấp để mở Diff Inspector đối soát chi tiết">
                            <div class="col-span-4 flex items-center gap-2 text-slate-300 group-hover:text-cyan-300 transition-colors">
                                <i data-lucide="user" class="w-4 h-4 text-slate-500 group-hover:text-cyan-400"></i>
                    <!-- Table Rows -->
                    <div id="dashboard-table-rows" class="divide-y divide-white/5 text-xs font-mono mt-1">
                        <!-- Row 1 -->
                        <div data-match="12" data-type="clean" onclick="window.location.href='${pageContext.request.contextPath}/diff-inspector?reportId=1'" class="dashboard-data-row grid grid-cols-12 items-center py-2.5 px-3 hover:bg-white/[0.05] rounded-lg transition-colors cursor-pointer group" title="Nhấp để mở Diff Inspector đối soát chi tiết">
                            <div class="col-span-4 flex items-center gap-2 text-slate-300 group-hover:text-cyan-300 transition-colors">
                                <i data-lucide="user" class="w-4 h-4 text-slate-500 group-hover:text-cyan-400"></i>
                                <div><div class="font-medium">Đỗ Gia Huy</div><div class="text-[10px] text-slate-500">SE1701</div></div>
                            </div>
                            <div class="col-span-4 text-slate-400">PRJ301_Assignment_OnlineShop</div>
                            <div class="col-span-2 text-emerald-400 font-bold">12%</div>
                            <div class="col-span-2 text-right text-slate-500 flex items-center justify-end gap-1">2026-10-25 13:43 <i data-lucide="chevron-right" class="w-3.5 h-3.5 text-slate-600 group-hover:text-cyan-400 transition-colors"></i></div>
                        </div>

                        <!-- Row 2 -->
                        <div data-match="24" data-type="clean" onclick="window.location.href='${pageContext.request.contextPath}/diff-inspector?reportId=1'" class="dashboard-data-row grid grid-cols-12 items-center py-2.5 px-3 hover:bg-white/[0.05] rounded-lg transition-colors cursor-pointer group" title="Nhấp để mở Diff Inspector đối soát chi tiết">
                            <div class="col-span-4 flex items-center gap-2 text-slate-300 group-hover:text-cyan-300 transition-colors">
                                <i data-lucide="user" class="w-4 h-4 text-slate-500 group-hover:text-cyan-400"></i>
                                <div><div class="font-medium">Lê Hoàng Nam</div><div class="text-[10px] text-slate-500">SE1702</div></div>
                            </div>
                            <div class="col-span-4 text-slate-400">PRJ301_Assignment_OnlineShop</div>
                            <div class="col-span-2 text-emerald-400 font-bold">24%</div>
                            <div class="col-span-2 text-right text-slate-500 flex items-center justify-end gap-1">2026-10-25 18:42 <i data-lucide="chevron-right" class="w-3.5 h-3.5 text-slate-600 group-hover:text-cyan-400 transition-colors"></i></div>
                        </div>

                        <!-- Alert Row -->
                        <div data-match="88" data-type="flagged" class="dashboard-data-row grid grid-cols-12 items-center py-3 px-3 rounded-xl bg-rose-950/20 border border-rose-500/60 alert-pulse-danger my-1">
                            <div class="col-span-4 flex items-center gap-2.5 text-rose-300">
                                <div class="w-7 h-7 rounded-lg bg-rose-500/20 flex items-center justify-center text-rose-400 shrink-0">
                                    <i data-lucide="alert-triangle" class="w-4 h-4"></i>
                                </div>
                                <div>
                                    <div class="font-bold text-rose-400 text-xs">88.5% TRÙNG KHỚP AST</div>
                                    <div class="text-[10px] text-rose-300/80">Trần Văn Long (SE1703) vs SE1702</div>
                                </div>
                            </div>
                            <div class="col-span-4 text-rose-200/90 text-xs">OrderManager.java (Đổi tên biến & đảo hàm)</div>
                            <div class="col-span-2 text-rose-400 font-extrabold text-sm">88.5%</div>
                            <div class="col-span-2 text-right flex items-center justify-end gap-2">
                                <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="px-2.5 py-1 rounded bg-rose-500 text-white font-bold text-[11px] hover:bg-rose-600 transition-colors shadow-[0_0_10px_rgba(239,68,68,0.4)]">
                                    Đối Soát &gt;
                                </a>
                            </div>
                        </div>

                        <!-- Row 4 -->
                        <div data-match="18" data-type="clean" onclick="window.location.href='${pageContext.request.contextPath}/diff-inspector?reportId=1'" class="dashboard-data-row grid grid-cols-12 items-center py-2.5 px-3 hover:bg-white/[0.05] rounded-lg transition-colors cursor-pointer group" title="Nhấp để mở Diff Inspector đối soát chi tiết">
                            <div class="col-span-4 flex items-center gap-2 text-slate-300 group-hover:text-cyan-300 transition-colors">
                                <i data-lucide="user" class="w-4 h-4 text-slate-500 group-hover:text-cyan-400"></i>
                                <div><div class="font-medium">Nguyễn Mạnh Hà</div><div class="text-[10px] text-slate-500">SE1704</div></div>
                            </div>
                            <div class="col-span-4 text-slate-400">PRJ301_Assignment_OnlineShop</div>
                            <div class="col-span-2 text-emerald-400 font-bold">18%</div>
                            <div class="col-span-2 text-right text-slate-500 flex items-center justify-end gap-1">2026-10-26 15:10 <i data-lucide="chevron-right" class="w-3.5 h-3.5 text-slate-600 group-hover:text-cyan-400 transition-colors"></i></div>
                        </div>

                        <!-- Row 5 -->
                        <div data-match="45" data-type="warning" onclick="window.location.href='${pageContext.request.contextPath}/diff-inspector?reportId=1'" class="dashboard-data-row grid grid-cols-12 items-center py-2.5 px-3 hover:bg-white/[0.05] rounded-lg transition-colors cursor-pointer group" title="Nhấp để mở Diff Inspector đối soát chi tiết">
                            <div class="col-span-4 flex items-center gap-2 text-slate-300 group-hover:text-cyan-300 transition-colors">
                                <i data-lucide="user" class="w-4 h-4 text-slate-500 group-hover:text-cyan-400"></i>
                                <div><div class="font-medium">Phạm Thùy Linh</div><div class="text-[10px] text-slate-500">SE1705</div></div>
                            </div>
                            <div class="col-span-4 text-slate-400">PRJ301_Assignment_OnlineShop</div>
                            <div class="col-span-2 text-amber-400 font-bold">45%</div>
                            <div class="col-span-2 text-right text-slate-500 flex items-center justify-end gap-1">2026-10-26 16:05 <i data-lucide="chevron-right" class="w-3.5 h-3.5 text-slate-600 group-hover:text-cyan-400 transition-colors"></i></div>
                        </div>
                    </div>
                    </div>
                </div>

                <div class="lg:col-span-4 cyber-card p-5 flex flex-col justify-between spotlight-card">
                    <div>
                        <div class="flex items-center justify-between mb-4">
                            <h3 class="text-sm font-bold text-white">High-Risk Users</h3>
                            <button class="text-slate-500 hover:text-white"><i data-lucide="more-horizontal" class="w-4 h-4"></i></button>
                        </div>
                        <div class="p-3 rounded-lg bg-[#0e0f1c] border border-rose-500/30 mb-3 font-mono text-[11px]">
                            <div class="flex items-center justify-between mb-2">
                                <div class="flex items-center gap-2">
                                    <img src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=60" class="w-5 h-5 rounded-full object-cover" alt="user">
                                    <span class="text-rose-400 text-xs font-semibold">SE1703 (Trần Văn Long)</span>
                                </div>
                                <span class="text-[9px] px-1.5 py-0.5 rounded bg-rose-950 text-rose-400 font-bold border border-rose-500/40">88.5% AST</span>
                            </div>
                            <div class="text-slate-400 text-[10px]">OrderManager.java (Đổi tên biến):</div>
                            <div class="text-rose-300 pl-2 text-[10px]">double calculateTotal() {</div>
                            <div class="text-rose-400 pl-4 text-[10px] font-bold">return basketValue * finalCost;</div>
                            <div class="text-rose-300 pl-2 text-[10px]">}</div>
                        </div>
                        <div class="p-3 rounded-lg bg-[#0e0f1c] border border-amber-500/30 mb-3 font-mono text-[11px]">
                            <div class="flex items-center justify-between mb-2">
                                <div class="flex items-center gap-2">
                                    <img src="https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=60" class="w-5 h-5 rounded-full object-cover" alt="user">
                                    <span class="text-amber-400 text-xs font-semibold">SE1702 (Lê Hoàng Nam)</span>
                                </div>
                                <span class="text-[9px] px-1.5 py-0.5 rounded bg-amber-950 text-amber-400 font-bold border border-amber-500/40">Mã Đối Chứng</span>
                            </div>
                            <div class="text-slate-400 text-[10px]">OrderProcessingService.java:</div>
                            <div class="text-amber-300 pl-2 text-[10px]">double calculateTotal() {</div>
                            <div class="text-cyan-300 pl-4 text-[10px]">return cartValue * finalCost;</div>
                            <div class="text-amber-300 pl-2 text-[10px]">}</div>
                        </div>
                        <!-- Action Link -->
                        <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="p-2.5 rounded-lg bg-[#0e0f1c] hover:bg-cyan-950/40 border border-white/10 hover:border-cyan-500/40 flex items-center justify-between font-mono text-xs text-slate-300 hover:text-cyan-300 transition-colors">
                            <span class="flex items-center gap-2">
                                <i data-lucide="split-square-vertical" class="w-4 h-4 text-cyan-400"></i>
                                <span>Mở Đối Soát Cặp Đôi Này</span>
                            </span>
                            <i data-lucide="arrow-right" class="w-3.5 h-3.5 text-cyan-400"></i>
                        </a>
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
        lucide.createIcons();

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
    <script src="${pageContext.request.contextPath}/assets/js/cyber-audio.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-particles.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/tilt-motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/terminal-stream.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/system-modals.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion-system-2026.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/command-palette.js"></script>
</body>
</html>
