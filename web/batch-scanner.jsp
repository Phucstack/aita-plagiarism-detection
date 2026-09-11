<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI CodeGuard - Batch Scanner</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: {
                        deepBg: '#090a14',
                        sidebarBg: '#0d0e1a',
                        cardBg: '#121422',
                        neonCyan: '#06b6d4',
                        neonEmerald: '#10b981',
                        neonAmber: '#f59e0b'
                    }
                }
            }
        }
    </script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=2.6">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/liquid-glass-2026.css?v=2.6">
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
    <style id="quantum-pillars-style">
        @keyframes eqPulse1 { 0%, 100% { transform: scaleY(0.22); } 50% { transform: scaleY(0.92); } }
        @keyframes eqPulse2 { 0%, 100% { transform: scaleY(0.72); } 50% { transform: scaleY(0.18); } }
        @keyframes eqPulse3 { 0%, 100% { transform: scaleY(0.35); } 50% { transform: scaleY(0.98); } }
        @keyframes eqPulse4 { 0%, 100% { transform: scaleY(0.85); } 50% { transform: scaleY(0.32); } }
        @keyframes eqPulse5 { 0%, 100% { transform: scaleY(0.18); } 50% { transform: scaleY(0.78); } }

        .pillar-eq-bar {
            height: 48px !important;
            transform-origin: bottom !important;
            display: block !important;
        }

        .eq-bar-1 { animation: eqPulse1 1.2s ease-in-out infinite !important; }
        .eq-bar-2 { animation: eqPulse2 0.85s ease-in-out infinite !important; }
        .eq-bar-3 { animation: eqPulse3 1.5s ease-in-out infinite !important; }
        .eq-bar-4 { animation: eqPulse4 1.1s ease-in-out infinite !important; }
        .eq-bar-5 { animation: eqPulse5 1.35s ease-in-out infinite !important; }
        .eq-bar-6 { animation: eqPulse3 0.9s ease-in-out infinite !important; }
        .eq-bar-7 { animation: eqPulse1 1.4s ease-in-out infinite !important; }
        .eq-bar-8 { animation: eqPulse4 1.05s ease-in-out infinite !important; }

        .containment-pillar {
            transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .containment-pillar.stage-breach {
            border-color: rgba(239, 68, 68, 0.6) !important;
            box-shadow: 0 0 25px rgba(239, 68, 68, 0.35) !important;
        }
        .containment-pillar.stage-breach .pillar-bracket-corner {
            border-color: #ef4444 !important;
        }
        .containment-pillar.stage-breach .pillar-accent-text {
            color: #f87171 !important;
        }
        .containment-pillar.stage-breach .pillar-coupling-line {
            background: linear-gradient(90deg, transparent, rgba(239, 68, 68, 0.7), #ef4444) !important;
            box-shadow: 0 0 10px #ef4444 !important;
        }
        .containment-pillar.stage-breach .pillar-coupling-line-rev {
            background: linear-gradient(270deg, transparent, rgba(239, 68, 68, 0.7), #ef4444) !important;
            box-shadow: 0 0 10px #ef4444 !important;
        }
        .containment-pillar.stage-breach .pillar-emitter-node {
            background-color: #ef4444 !important;
            box-shadow: 0 0 12px #ef4444 !important;
        }
        .containment-pillar.stage-breach .pillar-eq-bar {
            background: linear-gradient(to top, #7f1d1d, #ef4444, #fca5a5) !important;
            box-shadow: 0 0 8px rgba(239, 68, 68, 0.8) !important;
        }
    </style>
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
                <a href="${pageContext.request.contextPath}/dashboard" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/5 transition-all">
                    <i data-lucide="layout-grid" class="w-4 h-4"></i> Dashboard
                </a>
                <a href="${pageContext.request.contextPath}/batch-scanner" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-cyan-300 bg-cyan-950/50 border border-cyan-500/40 shadow-[0_0_12px_rgba(6,182,212,0.25)] transition-all">
                    <i data-lucide="scan-line" class="w-4 h-4"></i> Batch Scanner
                </a>
                <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/5 transition-all">
                    <i data-lucide="split-square-vertical" class="w-4 h-4"></i> Diff Inspector
                </a>
            </nav>
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

            <button onclick="showSystemHelp()" class="text-slate-400 hover:text-white transition-colors" title="Trợ giúp"><i data-lucide="help-circle" class="w-4 h-4"></i></button>
            <div class="relative">
                <button onclick="showSystemNotifications()" class="text-slate-400 hover:text-white transition-colors" title="Thông báo"><i data-lucide="bell" class="w-4 h-4"></i></button>
                <span class="absolute -top-1 -right-1 w-3.5 h-3.5 rounded-full bg-rose-500 text-[9px] font-bold text-white flex items-center justify-center">1</span>
            </div>
            
            <!-- User Profile Block -->
            <div class="flex items-center gap-2.5 pl-3 border-l border-white/10">
                <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80" alt="Avatar" class="w-8 h-8 rounded-full border border-cyan-400/60 object-cover shadow-[0_0_10px_rgba(6,182,212,0.3)]">
                <div class="text-left leading-tight hidden sm:block text-xs">
                    <div class="font-semibold text-white">TS. Nguyễn Hoàng Hà</div>
                    <div class="text-[10px] text-slate-400 font-mono">ha.nguyen@fpt.edu.vn</div>
                </div>
                <a href="${pageContext.request.contextPath}/login" class="ml-2 text-slate-400 hover:text-rose-400 transition-colors" title="Đăng xuất">
                    <i data-lucide="log-out" class="w-4 h-4"></i>
                </a>
            </div>
        </div>
    </header>

    <!-- Main Container with Master Left Sidebar (Unified Across All Screens) -->
    <div class="flex-1 flex min-h-0 overflow-hidden">
        <!-- Master Left Sidebar (Unified Across All Screens) -->
        <aside id="master-sidebar" class="w-56 border-r border-white/10 bg-[#0c0e1d] flex flex-col py-5 px-3.5 gap-1 shrink-0 select-none h-full overflow-y-auto custom-sidebar-scroll">
            <div class="sidebar-heading text-[10px] font-mono text-slate-500 px-3 mb-2 uppercase tracking-wider font-semibold">Điều Hướng Chính</div>
            
            <a href="${pageContext.request.contextPath}/dashboard" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 font-medium text-xs transition-all" title="Dashboard">
                <div class="flex items-center gap-3">
                    <i data-lucide="layout-grid" class="w-4 h-4 shrink-0"></i>
                    <span class="sidebar-label">Dashboard</span>
                </div>
                <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-white/5 text-slate-400">Live</span>
            </a>

            <a href="${pageContext.request.contextPath}/batch-scanner" class="sidebar-nav-item flex items-center justify-between px-3.5 py-2.5 rounded-xl bg-cyan-950/60 text-cyan-300 font-semibold text-xs border border-cyan-500/40 shadow-[0_0_15px_rgba(6,182,212,0.2)] transition-all" title="Batch Scanner">
                <div class="flex items-center gap-3">
                    <i data-lucide="scan-line" class="w-4 h-4 text-cyan-400 shrink-0"></i>
                    <span class="sidebar-label">Batch Scanner</span>
                </div>
                <span class="sidebar-badge text-[10px] font-mono px-1.5 py-0.5 rounded bg-cyan-500/20 text-cyan-300 border border-cyan-500/30">Laser</span>
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

        <!-- Main Content Area: 2-Column Split (Left 8 cols, Right 4 cols) -->
        <main class="flex-1 p-6 h-full min-h-0 overflow-y-auto max-w-[1600px] mx-auto w-full grid grid-cols-1 lg:grid-cols-12 gap-6">
        
        <!-- Left Column: Big Glowing Laser Dropzone + 4-Step Stepper (8 cols) -->
        <div class="lg:col-span-8 flex flex-col gap-8">
            
            <!-- Glowing Cyan Dropzone Box with Laser Beam (Clicking background does not open file picker) -->
            <div id="dropzone-box" ondragover="handleDragOver(event)" ondragleave="handleDragLeave(event)" ondrop="handleDrop(event)" class="laser-scanner-box p-8 flex flex-col items-center justify-center min-h-[420px] bg-[#0c0f1d]/90 relative border-beam-container spotlight-card group transition-all" title="Kéo thả tệp mã nguồn vào đây">
                <!-- Cyan Laser Traverse Line -->
                <div class="laser-beam-line"></div>

                <!-- Sleek Top-Corner Telemetry Badges (Non-interfering) -->
                <!-- Top-Left: Java AST Standard -->
                <div class="absolute top-6 left-8 px-3.5 py-2 rounded-xl bg-[#14182b]/70 border border-cyan-500/30 flex items-center gap-2.5 shadow-[0_0_15px_rgba(6,182,212,0.15)] backdrop-blur-md pointer-events-none">
                    <span class="text-base">☕</span>
                    <div class="flex flex-col text-left">
                        <span class="text-[11px] font-mono font-bold text-cyan-300">Java AST Suite</span>
                        <span class="text-[9px] font-mono text-slate-400">PRJ301 Standard</span>
                    </div>
                </div>

                <!-- Top-Right: SHA-256 Integrity Verified -->
                <div class="absolute top-6 right-8 px-3.5 py-2 rounded-xl bg-[#14182b]/70 border border-emerald-500/30 flex items-center gap-2.5 shadow-[0_0_15px_rgba(16,185,129,0.15)] backdrop-blur-md pointer-events-none">
                    <i data-lucide="shield-check" class="w-4 h-4 text-emerald-400"></i>
                    <div class="flex flex-col text-left">
                        <span class="text-[11px] font-mono font-bold text-emerald-300">SHA-256 Vault</span>
                        <span class="text-[9px] font-mono text-slate-400">Filter 4.4.2 Verified</span>
                    </div>
                </div>

                <!-- Left Quantum Containment Bracket (Sci-Fi Frequency Spectrum & Emitter Claw) -->
                <div id="containment-pillar-left" class="containment-pillar hidden xl:flex absolute left-8 top-[6.6rem] flex-col items-end gap-2 pointer-events-none select-none z-10 w-28 transition-all">
                    <div class="flex items-center gap-2 font-mono text-[9px] text-cyan-400 whitespace-nowrap">
                        <span class="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-ping"></span>
                        <span class="tracking-wider uppercase font-bold text-slate-300 pillar-accent-text">FIELD // L-01</span>
                        <div class="pillar-bracket-corner w-2.5 h-2.5 border-t-2 border-l-2 border-cyan-400"></div>
                    </div>
                    <div class="p-2.5 rounded-xl bg-[#090b17]/90 border border-cyan-500/30 backdrop-blur-md shadow-[0_0_20px_rgba(6,182,212,0.2)] flex items-end justify-between gap-1.5 h-20 w-full overflow-hidden">
                        <div class="pillar-eq-bar eq-bar-1 w-1.5 rounded-t-sm bg-gradient-to-t from-cyan-950 via-cyan-400 to-white text-cyan-400"></div>
                        <div class="pillar-eq-bar eq-bar-2 w-1.5 rounded-t-sm bg-gradient-to-t from-cyan-950 via-cyan-400 to-white text-cyan-400"></div>
                        <div class="pillar-eq-bar eq-bar-3 w-1.5 rounded-t-sm bg-gradient-to-t from-cyan-950 via-cyan-400 to-white text-cyan-400"></div>
                        <div class="pillar-eq-bar eq-bar-4 w-1.5 rounded-t-sm bg-gradient-to-t from-cyan-950 via-cyan-400 to-white text-cyan-400"></div>
                        <div class="pillar-eq-bar eq-bar-5 w-1.5 rounded-t-sm bg-gradient-to-t from-cyan-950 via-cyan-400 to-white text-cyan-400"></div>
                        <div class="pillar-eq-bar eq-bar-6 w-1.5 rounded-t-sm bg-gradient-to-t from-cyan-950 via-cyan-400 to-white text-cyan-400"></div>
                        <div class="pillar-eq-bar eq-bar-7 w-1.5 rounded-t-sm bg-gradient-to-t from-cyan-950 via-cyan-400 to-white text-cyan-400"></div>
                        <div class="pillar-eq-bar eq-bar-8 w-1.5 rounded-t-sm bg-gradient-to-t from-cyan-950 via-cyan-400 to-white text-cyan-400"></div>
                    </div>
                    <div class="relative w-full flex items-center justify-end my-0.5">
                        <div class="pillar-coupling-line h-[1.5px] w-14 bg-gradient-to-r from-transparent via-cyan-400 to-white shadow-[0_0_8px_#06b6d4]"></div>
                        <div class="pillar-emitter-node w-2 h-2 rotate-45 bg-cyan-400 shadow-[0_0_10px_#06b6d4]"></div>
                    </div>
                    <div class="flex items-center justify-between text-[9px] font-mono text-slate-400 leading-none w-full px-0.5">
                        <span class="text-cyan-300 font-bold pillar-accent-text">4.88 GHz</span>
                        <span class="text-slate-500 font-mono">[0x7F]</span>
                    </div>
                    <div class="w-full flex items-center justify-end pt-0.5">
                        <div class="pillar-bracket-corner w-2.5 h-2.5 border-b-2 border-l-2 border-cyan-400"></div>
                    </div>
                </div>

                <!-- Right Quantum Containment Bracket (Sci-Fi Diagnostic Vector & Emitter Claw) -->
                <div id="containment-pillar-right" class="containment-pillar hidden xl:flex absolute right-8 top-[6.6rem] flex-col items-start gap-2 pointer-events-none select-none z-10 w-28 transition-all">
                    <div class="flex items-center gap-2 font-mono text-[9px] text-cyan-400 whitespace-nowrap">
                        <div class="pillar-bracket-corner w-2.5 h-2.5 border-t-2 border-r-2 border-cyan-400"></div>
                        <span class="tracking-wider uppercase font-bold text-slate-300 pillar-accent-text">VECTOR // R-02</span>
                        <span class="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-ping"></span>
                    </div>
                    <div class="p-2.5 rounded-xl bg-[#090b17]/90 border border-violet-500/30 backdrop-blur-md shadow-[0_0_20px_rgba(139,92,246,0.2)] flex items-end justify-between gap-1.5 h-20 w-full overflow-hidden">
                        <div class="pillar-eq-bar eq-bar-5 w-1.5 rounded-t-sm bg-gradient-to-t from-violet-950 via-violet-400 to-white text-violet-400"></div>
                        <div class="pillar-eq-bar eq-bar-3 w-1.5 rounded-t-sm bg-gradient-to-t from-violet-950 via-violet-400 to-white text-violet-400"></div>
                        <div class="pillar-eq-bar eq-bar-7 w-1.5 rounded-t-sm bg-gradient-to-t from-violet-950 via-violet-400 to-white text-violet-400"></div>
                        <div class="pillar-eq-bar eq-bar-1 w-1.5 rounded-t-sm bg-gradient-to-t from-violet-950 via-violet-400 to-white text-violet-400"></div>
                        <div class="pillar-eq-bar eq-bar-8 w-1.5 rounded-t-sm bg-gradient-to-t from-violet-950 via-violet-400 to-white text-violet-400"></div>
                        <div class="pillar-eq-bar eq-bar-2 w-1.5 rounded-t-sm bg-gradient-to-t from-violet-950 via-violet-400 to-white text-violet-400"></div>
                        <div class="pillar-eq-bar eq-bar-6 w-1.5 rounded-t-sm bg-gradient-to-t from-violet-950 via-violet-400 to-white text-violet-400"></div>
                        <div class="pillar-eq-bar eq-bar-4 w-1.5 rounded-t-sm bg-gradient-to-t from-violet-950 via-violet-400 to-white text-violet-400"></div>
                    </div>
                    <div class="relative w-full flex items-center justify-start my-0.5">
                        <div class="pillar-emitter-node w-2 h-2 rotate-45 bg-violet-400 shadow-[0_0_10px_#8b5cf6]"></div>
                        <div class="pillar-coupling-line-rev h-[1.5px] w-14 bg-gradient-to-l from-transparent via-violet-400 to-white shadow-[0_0_8px_#8b5cf6]"></div>
                    </div>
                    <div class="flex items-center justify-between text-[9px] font-mono text-slate-400 leading-none w-full px-0.5">
                        <span class="text-slate-500 font-mono">[0x9B]</span>
                        <span class="text-violet-300 font-bold pillar-accent-text">768-VEC</span>
                    </div>
                    <div class="w-full flex items-center justify-start pt-0.5">
                        <div class="pillar-bracket-corner w-2.5 h-2.5 border-b-2 border-r-2 border-cyan-400"></div>
                    </div>
                </div>

                <!-- 3D Holographic Cyber Security Shield (Tripo3D/Three.js Studio PBR Engine) -->
                <div class="batch-shield-3d-container relative w-[340px] h-[240px] max-w-full flex flex-col items-center justify-center pointer-events-auto my-0.5 group/shield" title="AITA 3D Cyber Security Shield - Giám sát toàn vẹn mã nguồn thời gian thực (Kéo chuột để xoay 3D)">
                    <div id="batch-shield-3d-viewport" class="w-full h-full cursor-grab active:cursor-grabbing"></div>
                    <div class="absolute bottom-1 text-[10px] font-mono text-cyan-300 bg-[#070914]/90 px-3.5 py-0.5 rounded-full border border-cyan-500/30 backdrop-blur-md pointer-events-none flex items-center gap-1.5 shadow-lg z-10">
                        <span id="shield-status-dot" class="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-pulse"></span>
                        <span id="shield-status-text">Cyber Shield Ready</span>
                    </div>
                </div>

                <!-- Clean Upload Fallback Icon khi TẮT hiệu ứng (không hiển thị 3D Shield) -->
                <div class="batch-clean-upload-fallback hidden flex-col items-center justify-center py-6 pointer-events-none">
                    <div class="w-16 h-16 rounded-2xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400 mb-2 shadow-sm">
                        <i data-lucide="cloud-upload" class="w-8 h-8 text-cyan-400"></i>
                    </div>
                    <span class="text-xs font-mono text-slate-400">Khu vực nhận tệp tải lên (Chế độ tĩnh)</span>
                </div>

                <!-- Center Scanning Text with Stepper Details -->
                <div class="text-center z-10 my-2 flex flex-col items-center pointer-events-none">
                    <div class="text-xs font-mono text-cyan-400 tracking-widest uppercase mb-1 drop-shadow-[0_0_8px_#06b6d4]">
                        Scanning Pipeline
                    </div>
                    <h2 class="text-2xl font-bold text-white tracking-tight group-hover:text-cyan-300 transition-colors">
                        Drag &amp; Drop Student Assignments
                    </h2>
                    <p class="text-xs text-slate-400 font-mono mt-1 pointer-events-auto">
                        Hỗ trợ gói mã nguồn <span class="text-cyan-300 font-semibold">.java, .zip</span> môn PRJ301 (Chuẩn hóa AST 40% &amp; Gemini Pro 60%)
                    </p>
                    <div class="flex flex-wrap items-center justify-center gap-2.5 mt-2.5">
                        <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-950/70 border border-emerald-500/40 text-[10px] font-mono text-emerald-300 shadow-sm">
                            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span> Auto SHA-256 Check (Filter 4.4.2)
                        </span>
                        <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-cyan-950/70 border border-cyan-500/40 text-[10px] font-mono text-cyan-300 shadow-sm">
                            <i data-lucide="cpu" class="w-3 h-3"></i> JavaParser AST Engine 3.25
                        </span>
                    </div>
                    <!-- File Status Alert on Upload -->
                    <div id="dropzone-file-status" class="hidden mt-3 px-3 py-1.5 rounded-xl bg-cyan-950/80 border border-cyan-400/50 text-cyan-300 font-mono text-xs flex items-center gap-2 shadow-[0_0_15px_rgba(6,182,212,0.3)]">
                        <i data-lucide="file-check-2" class="w-4 h-4 text-cyan-400"></i>
                        <span id="file-status-text">Đã chọn 0 tệp</span>
                    </div>
                </div>

                <!-- Action Buttons: Browse Files & Start Batch Scan -->
                <div class="mt-5 z-10" onclick="event.stopPropagation()">
                    <form action="${pageContext.request.contextPath}/batch-scanner" method="POST" enctype="multipart/form-data" class="flex items-center gap-3">
                        <input type="file" id="batch-file-input" name="assignments" multiple accept=".java,.py,.js,.cpp,.zip" class="hidden" onchange="handleFileSelected(event)">
                        <button type="button" onclick="triggerFileInput()" class="px-5 py-2.5 rounded-xl bg-[#14182b] hover:bg-[#1d233d] border border-white/15 hover:border-cyan-500/50 text-slate-300 hover:text-cyan-300 text-xs font-mono font-medium flex items-center gap-2 transition-all cursor-pointer shadow-md active:scale-95">
                            <i data-lucide="folder-up" class="w-4 h-4 text-cyan-400"></i>
                            <span>Duyệt Tệp Từ Máy (.java, .zip)</span>
                        </button>
                        <button type="submit" onclick="showToast('Khởi chạy quét hàng loạt: SHA-256 hoàn tất! AST &amp; Gemini đang phân tích...', 'success')" class="px-6 py-2.5 rounded-xl bg-cyan-950/80 hover:bg-cyan-900 active:scale-95 border border-cyan-500/50 text-cyan-300 text-xs font-bold shadow-[0_0_20px_rgba(6,182,212,0.25)] flex items-center gap-2 transition-all cursor-pointer btn-shimmer relative overflow-hidden">
                            <i data-lucide="play" class="w-3.5 h-3.5 text-cyan-400 fill-cyan-400"></i>
                            <span>Start Batch Scan</span>
                        </button>
                    </form>
                </div>
            </div>

            <!-- 4-Step Circular Stepper Progress Bar -->
            <div id="stepper-progress-container" class="flex items-center justify-between px-6 pt-4">
                
                <!-- Step 1: Green Checkmark Verified -->
                <div id="step-node-1" class="flex flex-col items-center text-center transition-all duration-500">
                    <div class="w-14 h-14 rounded-full bg-[#10b981]/15 border-2 border-emerald-400 flex items-center justify-center shadow-[0_0_20px_rgba(16,185,129,0.3)] mb-2">
                        <i data-lucide="check" class="w-7 h-7 text-emerald-400 stroke-[3]"></i>
                    </div>
                    <span class="text-xs font-bold text-white block">SHA-256 Verification</span>
                    <span class="mt-1 text-[10px] font-mono px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">Verified</span>
                </div>

                <!-- Connecting Line 1 (Green Glow) -->
                <div id="step-line-1" class="flex-1 h-[2px] bg-gradient-to-r from-emerald-400 to-amber-400 mx-2 shadow-[0_0_8px_#10b981] transition-all duration-500"></div>

                <!-- Step 2: Orange Progress Spinner -->
                <div id="step-node-2" class="flex flex-col items-center text-center transition-all duration-500">
                    <div class="w-14 h-14 rounded-full bg-[#f59e0b]/15 border-2 border-dashed border-amber-400 flex items-center justify-center shadow-[0_0_20px_rgba(245,158,11,0.3)] mb-2 animate-[spin_8s_linear_infinite]">
                        <i data-lucide="loader-2" class="w-7 h-7 text-amber-400 animate-spin"></i>
                    </div>
                    <span class="text-xs font-bold text-white block">AST Parsing</span>
                    <span class="mt-1 text-[10px] font-mono px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-400 border border-amber-500/30">In Progress</span>
                </div>

                <!-- Connecting Line 2 -->
                <div id="step-line-2" class="flex-1 h-[2px] bg-white/10 mx-2 transition-all duration-500"></div>

                <!-- Step 3: Gemini Analysis -->
                <div id="step-node-3" class="flex flex-col items-center text-center opacity-40 transition-all duration-500">
                    <div class="w-14 h-14 rounded-full bg-white/5 border border-white/20 flex items-center justify-center mb-2">
                        <i data-lucide="pie-chart" class="w-6 h-6 text-slate-400"></i>
                    </div>
                    <span class="text-xs font-bold text-slate-400 block">Gemini Semantic</span>
                    <span class="text-[10px] text-slate-500 block">Analysis</span>
                </div>

                <!-- Connecting Line 3 -->
                <div id="step-line-3" class="flex-1 h-[2px] bg-white/10 mx-2 transition-all duration-500"></div>

                <!-- Step 4: Plagiarism Matrix -->
                <div id="step-node-4" class="flex flex-col items-center text-center opacity-40 transition-all duration-500">
                    <div class="w-14 h-14 rounded-full bg-white/5 border border-white/20 flex items-center justify-center mb-2">
                        <i data-lucide="file-text" class="w-6 h-6 text-slate-400"></i>
                    </div>
                    <span class="text-xs font-bold text-slate-400 block">Plagiarism Matrix</span>
                    <span class="text-[10px] text-slate-500 block">Generation</span>
                </div>
            </div>

            <!-- Real-time Scan Result Completion Banner (Revealed dynamically after scan) -->
            <div id="scan-completion-banner" class="hidden p-5 rounded-2xl bg-gradient-to-r from-rose-950/50 via-[#131428] to-cyan-950/50 border border-cyan-500/40 shadow-[0_0_25px_rgba(6,182,212,0.2)] animate-[staggerFadeUp_0.6s_ease-out]">
                <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
                    <div class="flex items-center gap-3.5">
                        <div class="w-12 h-12 rounded-xl bg-rose-500/20 border border-rose-500/40 flex items-center justify-center text-rose-400 shadow-[0_0_15px_rgba(244,63,94,0.3)] shrink-0">
                            <i data-lucide="alert-triangle" class="w-6 h-6 animate-pulse"></i>
                        </div>
                        <div>
                            <div class="flex items-center gap-2">
                                <h4 class="text-sm font-bold text-white font-mono">QUÉT HOÀN TẤT: PHÁT HIỆN 1 BÀI NỘP CỜ ĐỎ</h4>
                                <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-rose-500/20 text-rose-300 border border-rose-500/40 font-bold">88.5% Trùng Lặp</span>
                            </div>
                            <p class="text-xs text-slate-300 mt-1">
                                Tệp <strong class="text-rose-400">OrderProcessingService.java</strong> (Lê Hoàng Nam - SE1702) trùng khớp cấu trúc AST 40% &amp; ngữ nghĩa Gemini 60% với <strong class="text-cyan-400">OrderManager.java</strong> (Trần Văn Long - SE1701).
                            </p>
                        </div>
                    </div>

        </div>

        <!-- Right Column: Recent Scans with Wavy Sparkline Curves & Area Gradient Fill -->
        <div class="lg:col-span-4 cyber-card p-5 flex flex-col gap-4">
            <div class="flex items-center justify-between pb-2 border-b border-white/5">
                <h3 class="text-sm font-bold text-white">Recent Scans</h3>
                <span class="text-[11px] font-mono px-2 py-0.5 rounded-full border border-white/10 text-slate-400 bg-white/5">Complete</span>
            </div>

            <!-- Card 1 (8.4% Clean) -->
            <div class="p-3.5 rounded-xl bg-[#0f111e] border border-white/5 hover:border-cyan-500/30 transition-all spotlight-card">
                <div class="flex items-center justify-between">
                    <div>
                        <div class="text-xs font-bold text-white">Lớp SE1701 - PRJ301</div>
                        <div class="text-[10px] text-slate-500">42 bài nộp (OrderManager.java)</div>
                    </div>
                    <span class="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">An Toàn</span>
                </div>
                <!-- Sparkline Wave SVG with Area Gradient -->
                <div class="my-2">
                    <svg class="w-full h-8" viewBox="0 0 200 40" fill="none">
                        <defs>
                            <linearGradient id="sparkGrad1" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="0%" stop-color="#06b6d4" stop-opacity="0.45"/>
                                <stop offset="100%" stop-color="#06b6d4" stop-opacity="0"/>
                            </linearGradient>
                        </defs>
                        <path d="M0,35 Q30,10 60,25 T120,15 T160,28 T200,10 L200,40 L0,40 Z" fill="url(#sparkGrad1)"/>
                        <path d="M0,35 Q30,10 60,25 T120,15 T160,28 T200,10" stroke="#06b6d4" stroke-width="2" fill="none"/>
                    </svg>
                </div>
                <div class="flex items-center justify-between text-[11px]">
                    <span class="text-slate-400">Tỷ lệ tương đồng: <strong class="text-white">8.4%</strong></span>
                    <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="text-cyan-400 hover:underline text-[10px]">Chi tiết đối soát &gt;</a>
                </div>
            </div>

            <!-- Card 2 (11.1% Clean) -->
            <div class="p-3.5 rounded-xl bg-[#0f111e] border border-white/5 hover:border-cyan-500/30 transition-all spotlight-card">
                <div class="flex items-center justify-between">
                    <div>
                        <div class="text-xs font-bold text-white">Lớp SE1702 - PRJ301</div>
                        <div class="text-[10px] text-slate-500">38 bài nộp (UserDAO.java)</div>
                    </div>
                    <span class="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">An Toàn</span>
                </div>
                <div class="my-2">
                    <svg class="w-full h-8" viewBox="0 0 200 40" fill="none">
                        <defs>
                            <linearGradient id="sparkGrad2" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="0%" stop-color="#06b6d4" stop-opacity="0.45"/>
                                <stop offset="100%" stop-color="#06b6d4" stop-opacity="0"/>
                            </linearGradient>
                        </defs>
                        <path d="M0,30 Q40,15 80,30 T140,10 T200,25 L200,40 L0,40 Z" fill="url(#sparkGrad2)"/>
                        <path d="M0,30 Q40,15 80,30 T140,10 T200,25" stroke="#06b6d4" stroke-width="2" fill="none"/>
                    </svg>
                </div>
                <div class="flex items-center justify-between text-[11px]">
                    <span class="text-slate-400">Tỷ lệ tương đồng: <strong class="text-white">11.1%</strong></span>
                    <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="text-cyan-400 hover:underline text-[10px]">Chi tiết đối soát &gt;</a>
                </div>
            </div>

            <!-- Card 3 (88.5% Critical Flagged) -->
            <div class="p-3.5 rounded-xl bg-[#1a0f18] border border-rose-500/40 hover:border-rose-500/60 transition-all spotlight-card shadow-[0_0_15px_rgba(244,63,94,0.15)]">
                <div class="flex items-center justify-between">
                    <div>
                        <div class="text-xs font-bold text-rose-300">Lớp SE1703 - PRJ301</div>
                        <div class="text-[10px] text-rose-400/70">45 bài nộp (OrderProcessingService.java)</div>
                    </div>
                    <span class="text-[10px] font-mono px-2 py-0.5 rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/40 font-bold animate-pulse">Cờ Đỏ AST</span>
                </div>
                <div class="my-2">
                    <svg class="w-full h-8" viewBox="0 0 200 40" fill="none">
                        <defs>
                            <linearGradient id="sparkGrad3" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="0%" stop-color="#f43f5e" stop-opacity="0.5"/>
                                <stop offset="100%" stop-color="#f43f5e" stop-opacity="0"/>
                            </linearGradient>
                        </defs>
                        <path d="M0,35 Q50,5 100,25 T160,15 T200,30 L200,40 L0,40 Z" fill="url(#sparkGrad3)"/>
                        <path d="M0,35 Q50,5 100,25 T160,15 T200,30" stroke="#f43f5e" stroke-width="2" fill="none"/>
                    </svg>
                </div>
                <div class="flex items-center justify-between text-[11px]">
                    <span class="text-slate-400">Tỷ lệ tương đồng: <strong class="text-rose-400 font-bold">88.5%</strong></span>
                    <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="text-rose-400 font-semibold hover:underline text-[10px] flex items-center gap-1">Đối soát ngay &rarr;</a>
                </div>
            </div>

            <!-- Card 4 (2.1% Clean) -->
            <div class="p-3.5 rounded-xl bg-[#0f111e] border border-white/5 hover:border-cyan-500/30 transition-all spotlight-card">
                <div class="flex items-center justify-between">
                    <div>
                        <div class="text-xs font-bold text-white">Lớp SE1704 - PRJ301</div>
                        <div class="text-[10px] text-slate-500">40 bài nộp (ProductDAO.java)</div>
                    </div>
                    <span class="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">An Toàn</span>
                </div>
                <div class="my-2">
                    <svg class="w-full h-8" viewBox="0 0 200 40" fill="none">
                        <defs>
                            <linearGradient id="sparkGrad4" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="0%" stop-color="#06b6d4" stop-opacity="0.45"/>
                                <stop offset="100%" stop-color="#06b6d4" stop-opacity="0"/>
                            </linearGradient>
                        </defs>
                        <path d="M0,38 Q40,25 90,35 T150,18 T200,32 L200,40 L0,40 Z" fill="url(#sparkGrad4)"/>
                        <path d="M0,38 Q40,25 90,35 T150,18 T200,32" stroke="#06b6d4" stroke-width="2" fill="none"/>
                    </svg>
                </div>
                <div class="flex items-center justify-between text-[11px]">
                    <span class="text-slate-400">Tỷ lệ tương đồng: <strong class="text-white">2.1%</strong></span>
                    <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="text-cyan-400 hover:underline text-[10px]">Chi tiết đối soát &gt;</a>
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

        function triggerFileInput() {
            const input = document.getElementById('batch-file-input');
            if (input) input.click();
        }

        function handleDragOver(e) {
            e.preventDefault();
            e.stopPropagation();
            const box = document.getElementById('dropzone-box');
            if (box) box.classList.add('ring-2', 'ring-cyan-400', 'bg-cyan-950/40');
        }

        function handleDragLeave(e) {
            e.preventDefault();
            e.stopPropagation();
            const box = document.getElementById('dropzone-box');
            if (box) box.classList.remove('ring-2', 'ring-cyan-400', 'bg-cyan-950/40');
        }

        function handleDrop(e) {
            e.preventDefault();
            e.stopPropagation();
            handleDragLeave(e);
            const files = e.dataTransfer ? e.dataTransfer.files : null;
            if (files && files.length > 0) {
                updateFileStatus(files);
            }
        }

        function handleFileSelected(e) {
            const files = e.target.files;
            if (files && files.length > 0) {
                updateFileStatus(files);
            }
        }

        function updateFileStatus(files) {
            const statusEl = document.getElementById('dropzone-file-status');
            const textEl = document.getElementById('file-status-text');
            if (statusEl && textEl) {
                statusEl.classList.remove('hidden');
                const names = Array.from(files).slice(0, 2).map(f => f.name).join(', ');
                textEl.innerText = 'Đã chọn ' + files.length + ' tệp (' + names + (files.length > 2 ? ', ...' : '') + ')';
                if (window.lucide) lucide.createIcons();
            }
            if (window.showToast) showToast('Đã nạp ' + files.length + ' tệp nộp bài vào hàng đợi quét!', 'success');
        }

        let isScanning = false;
        function handleStartScan(e) {
            if (isScanning) return;
            isScanning = true;

            const btn = e.target.closest('button');
            const originalText = btn ? btn.innerHTML : 'Start Batch Scan';
            if (btn) {
                btn.disabled = true;
                btn.innerHTML = `<span class="flex items-center gap-2"><i data-lucide="loader-2" class="w-4 h-4 animate-spin text-cyan-400"></i> Đang Phân Tích Đạo Văn...</span>`;
                if (window.lucide) lucide.createIcons();
            }

            if (window.CyberAudio) CyberAudio.playLaser();
            if (window.CyberShield3D) CyberShield3D.setStage(1);
            const shieldStatus = document.getElementById('shield-status-text');
            const shieldDot = document.getElementById('shield-status-dot');
            if (shieldStatus) shieldStatus.textContent = 'Stage 1: SHA-256 Validating';
            if (shieldDot) shieldDot.className = 'w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse';
            if (window.showToast) showToast('BƯỚC 1/4: Kiểm tra SHA-256 mã hash 42 bài nộp... (Khớp chuỗi)', 'info');

            // Stage 2: AST Parsing (after 900ms)
            setTimeout(() => {
                const node2 = document.getElementById('step-node-2');
                const line1 = document.getElementById('step-line-1');
                if (node2) {
                    node2.innerHTML = `
                        <div class="w-14 h-14 rounded-full bg-[#10b981]/15 border-2 border-emerald-400 flex items-center justify-center shadow-[0_0_20px_rgba(16,185,129,0.3)] mb-2">
                            <i data-lucide="check" class="w-7 h-7 text-emerald-400 stroke-[3]"></i>
                        </div>
                        <span class="text-xs font-bold text-white block">AST Parsing</span>
                        <span class="mt-1 text-[10px] font-mono px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">Chuẩn Hóa Xong</span>
                    `;
                }
                if (line1) {
                    line1.className = "flex-1 h-[2px] bg-emerald-400 mx-2 shadow-[0_0_8px_#10b981] transition-all duration-500";
                }

                // Activate Stage 3: Gemini Analysis
                const node3 = document.getElementById('step-node-3');
                const line2 = document.getElementById('step-line-2');
                if (node3) {
                    node3.classList.remove('opacity-40');
                    node3.innerHTML = `
                        <div class="w-14 h-14 rounded-full bg-violet-600/20 border-2 border-dashed border-violet-400 flex items-center justify-center shadow-[0_0_20px_rgba(139,92,246,0.3)] mb-2 animate-[spin_8s_linear_infinite]">
                            <i data-lucide="loader-2" class="w-7 h-7 text-violet-400 animate-spin"></i>
                        </div>
                        <span class="text-xs font-bold text-violet-300 block">Gemini Semantic</span>
                        <span class="mt-1 text-[10px] font-mono px-2.5 py-0.5 rounded-full bg-violet-500/20 text-violet-300 border border-violet-500/30 animate-pulse">Vector Embedding</span>
                    `;
                }
                if (line2) {
                    line2.className = "flex-1 h-[2px] bg-gradient-to-r from-emerald-400 to-violet-400 mx-2 shadow-[0_0_8px_rgba(139,92,246,0.5)] transition-all duration-500";
                }
                if (window.CyberShield3D) CyberShield3D.setStage(2);
                if (shieldStatus) shieldStatus.textContent = 'Stage 2: AST Tokenizing';
                if (shieldDot) shieldDot.className = 'w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse';
                if (window.CyberAudio) CyberAudio.playTick();
                if (window.lucide) lucide.createIcons();
                if (window.showToast) showToast('BƯỚC 2/4: AST chuẩn hóa xong! BƯỚC 3/4: Gemini phân tích ngữ nghĩa...', 'info');
            }, 1000);

            // Stage 3: Gemini Done & Stage 4: Matrix Generation (after 2100ms)
            setTimeout(() => {
                const node3 = document.getElementById('step-node-3');
                const line2 = document.getElementById('step-line-2');
                if (node3) {
                    node3.innerHTML = `
                        <div class="w-14 h-14 rounded-full bg-[#10b981]/15 border-2 border-emerald-400 flex items-center justify-center shadow-[0_0_20px_rgba(16,185,129,0.3)] mb-2">
                            <i data-lucide="check" class="w-7 h-7 text-emerald-400 stroke-[3]"></i>
                        </div>
                        <span class="text-xs font-bold text-white block">Gemini Semantic</span>
                        <span class="mt-1 text-[10px] font-mono px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">Vector Xong</span>
                    `;
                }
                if (line2) {
                    line2.className = "flex-1 h-[2px] bg-emerald-400 mx-2 shadow-[0_0_8px_#10b981] transition-all duration-500";
                }

                // Activate Stage 4: Plagiarism Matrix
                const node4 = document.getElementById('step-node-4');
                const line3 = document.getElementById('step-line-3');
                if (node4) {
                    node4.classList.remove('opacity-40');
                    node4.innerHTML = `
                        <div class="w-14 h-14 rounded-full bg-cyan-600/20 border-2 border-dashed border-cyan-400 flex items-center justify-center shadow-[0_0_20px_rgba(6,182,212,0.3)] mb-2 animate-[spin_8s_linear_infinite]">
                            <i data-lucide="loader-2" class="w-7 h-7 text-cyan-400 animate-spin"></i>
                        </div>
                        <span class="text-xs font-bold text-cyan-300 block">Plagiarism Matrix</span>
                        <span class="mt-1 text-[10px] font-mono px-2.5 py-0.5 rounded-full bg-cyan-500/20 text-cyan-300 border border-cyan-500/30 animate-pulse">Tính Ma Trận</span>
                    `;
                }
                if (line3) {
                    line3.className = "flex-1 h-[2px] bg-gradient-to-r from-emerald-400 to-cyan-400 mx-2 shadow-[0_0_8px_#06b6d4] transition-all duration-500";
                }
                if (window.CyberShield3D) CyberShield3D.setStage(3);
                if (shieldStatus) shieldStatus.textContent = 'Stage 3: Gemini Vectorizing';
                if (shieldDot) shieldDot.className = 'w-1.5 h-1.5 rounded-full bg-violet-400 animate-pulse';
                if (window.CyberAudio) CyberAudio.playTick();
                if (window.lucide) lucide.createIcons();
                if (window.showToast) showToast('BƯỚC 3/4: Vector phân tích xong! BƯỚC 4/4: Tổng hợp ma trận đối soát 40% AST + 60% Gemini...', 'info');
            }, 2100);

            // Stage 4 Completion & Reveal Banner (after 3200ms)
            setTimeout(() => {
                const node4 = document.getElementById('step-node-4');
                const line3 = document.getElementById('step-line-3');
                if (node4) {
                    node4.innerHTML = `
                        <div class="w-14 h-14 rounded-full bg-[#10b981]/15 border-2 border-emerald-400 flex items-center justify-center shadow-[0_0_20px_rgba(16,185,129,0.3)] mb-2">
                            <i data-lucide="check" class="w-7 h-7 text-emerald-400 stroke-[3]"></i>
                        </div>
                        <span class="text-xs font-bold text-white block">Plagiarism Matrix</span>
                        <span class="mt-1 text-[10px] font-mono px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">Hoàn Tất</span>
                    `;
                }
                if (line3) {
                    line3.className = "flex-1 h-[2px] bg-emerald-400 mx-2 shadow-[0_0_8px_#10b981] transition-all duration-500";
                }

                // Reveal Completion Banner
                const banner = document.getElementById('scan-completion-banner');
                if (banner) {
                    banner.classList.remove('hidden');
                    banner.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                }

                if (window.CyberShield3D) CyberShield3D.setStage(4);
                if (shieldStatus) {
                    shieldStatus.textContent = 'BREACH DETECTED: 88.5%';
                    shieldStatus.className = 'text-rose-400 font-bold';
                }
                if (shieldDot) shieldDot.className = 'w-1.5 h-1.5 rounded-full bg-rose-500 animate-ping';

                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = `<i data-lucide="refresh-cw" class="w-4 h-4 text-emerald-400 inline mr-1"></i> Quét Lại Hàng Loạt`;
                    btn.onclick = (evt) => {
                        if (window.CyberShield3D) CyberShield3D.reset();
                        if (shieldStatus) {
                            shieldStatus.textContent = 'Cyber Shield Ready';
                            shieldStatus.className = '';
                        }
                        if (shieldDot) shieldDot.className = 'w-1.5 h-1.5 rounded-full bg-cyan-400 animate-pulse';
                        if (banner) banner.classList.add('hidden');
                        btn.onclick = handleStartScan;
                        handleStartScan(evt);
                    };
                }
                isScanning = false;

                if (window.CyberAudio) CyberAudio.playDangerChord();
                if (window.lucide) lucide.createIcons();
                if (window.showToast) showToast('HOÀN TẤT QUÉT 42 BÀI NỘP: Phát hiện 1 bài cờ đỏ (88.5%)!', 'danger');
            }, 3200);
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
    </script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/loaders/GLTFLoader.js"></script>
    <script src="${pageContext.request.contextPath}/assets/models/cyber-shield-data.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-effects.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-audio.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-particles.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-shield-3d.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/tilt-motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/system-modals.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion-system-2026.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/command-palette.js"></script>
</body>
</html>
