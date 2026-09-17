<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AITA CodeDefend - Đăng Nhập Hệ Thống</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: { deepBg: '#080911', cardBg: '#0e101d', neonCyan: '#06b6d4', neonViolet: '#8b5cf6', neonEmerald: '#10b981' }
                }
            }
        }
    </script>
    <script src="https://unpkg.com/lucide@latest"></script>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/motion-effects.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/aita-copilot.css?v=2.0">
</head>
<body class="cinema-grain bg-[#080911] text-slate-200 min-h-screen flex flex-col justify-center font-sans relative overflow-x-hidden p-4 sm:p-6 lg:p-8 pt-16 sm:pt-16 lg:pt-8">

    <!-- Top Right Cyber HUD Utility Bar (Audio & Rubric Badge) -->
    <div class="fixed top-4 right-4 z-50 flex items-center gap-2.5">
        <button onclick="CyberAudio.toggle()" class="audio-hud-toggle px-3 py-1.5 rounded-xl bg-[#0e101d]/90 border border-white/10 text-slate-300 hover:text-cyan-300 text-xs font-mono flex items-center gap-2 backdrop-blur-md transition-all shadow-lg" title="Bật/Tắt Âm Thanh Tương Tác HUD">
            <i data-lucide="volume-x" class="w-3.5 h-3.5"></i>
            <span class="hidden sm:inline text-[11px]">Cyber Audio</span>
        </button>
        <div class="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-cyan-950/70 border border-cyan-500/30 text-[11px] font-mono text-cyan-300 backdrop-blur-md shadow-lg">
            <span class="w-2 h-2 rounded-full bg-cyan-400 animate-pulse"></span>
            <span>PRJ301 • RBL G4</span>
        </div>
    </div>

    <!-- Interactive Cyber Particle Mesh Canvas (AST Synapse) -->
    <canvas id="cyber-canvas"></canvas>

    <!-- Cyber Ambient Aurora & Grid Background -->
    <div class="cyber-aurora-bg">
        <div class="aurora-blob-1"></div>
        <div class="aurora-blob-2"></div>
    </div>
    <div class="fixed inset-0 cyber-grid-bg pointer-events-none opacity-40 z-0"></div>

    <!-- Main Responsive Split-Screen Container -->
    <div class="relative z-10 w-full max-w-6xl mx-auto my-auto grid grid-cols-1 lg:grid-cols-12 gap-5 lg:gap-8 items-center">

        <!-- Left Column: Contextual AI Code Defense Showcase (7 cols) -->
        <div class="lg:col-span-7 space-y-3 animate-cyber-in stagger-1">
            <!-- Active Defense Badge & Hero Header Compact -->
            <div class="space-y-1.5">
                <div class="inline-flex items-center gap-2 px-3 py-0.5 rounded-full bg-cyan-950/70 border border-cyan-500/40 text-cyan-300 text-[11px] font-mono shadow-[0_0_15px_rgba(6,182,212,0.2)]">
                    <span class="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-ping"></span>
                    <span>AITA v2.4 • Active AST Defense Suite</span>
                </div>
                <h1 class="text-2xl sm:text-3xl font-extrabold text-white tracking-tight leading-snug">
                    Hệ Thống Giám Sát <span class="text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 via-indigo-300 to-violet-400">Liêm Chính Học Thuật & Đạo Văn Code</span>
                </h1>
                <p class="text-[11px] sm:text-xs text-slate-400 leading-snug line-clamp-2 max-w-xl">
                    Tự động bóc tách cây cú pháp trừu tượng AST Java, đối chứng vân tay băm SHA-256 và nhận diện biến đổi cấu trúc mã nguồn bằng Google Gemini 1.5 Pro.
                </p>
            </div>

            <!-- Live Cyber Terminal (Real-time AST Code Inspection) -->
            <div class="rounded-xl bg-[#0b0d18]/95 border border-cyan-500/30 shadow-[0_0_25px_rgba(6,182,212,0.12)] backdrop-blur-xl relative overflow-hidden spotlight-card border-beam-container">
                <div class="scanline-beam"></div>
                <div class="px-3.5 py-1.5 bg-slate-900/80 border-b border-white/5 flex items-center justify-between">
                    <div class="flex items-center gap-1.5">
                        <span class="w-2 h-2 rounded-full bg-rose-500/80 inline-block"></span>
                        <span class="w-2 h-2 rounded-full bg-amber-500/80 inline-block"></span>
                        <span class="w-2 h-2 rounded-full bg-emerald-500/80 inline-block"></span>
                        <span class="text-[10.5px] font-mono text-slate-400 ml-1.5">OrderManager.java — Live AST Stream</span>
                    </div>
                    <span class="text-[9.5px] font-mono text-cyan-400 bg-cyan-950/60 px-2 py-0.5 rounded border border-cyan-500/40 flex items-center gap-1 shadow-[0_0_8px_rgba(6,182,212,0.25)]">
                        <span class="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-pulse"></span> 60 FPS STREAM
                    </span>
                </div>
                <div id="terminal-stream-body" class="p-3 h-20 overflow-y-auto space-y-1 font-mono text-[11px]"></div>
                <div class="px-3.5 py-1 text-[9.5px] font-mono text-slate-500 flex items-center justify-between border-t border-white/5 bg-[#080911]/50">
                    <span>Engine: JavaParser-3.25 + Gemini-1.5</span>
                    <span>Monitoring: <span class="text-emerald-400 font-semibold">Active Scanning</span><span class="terminal-cursor"></span></span>
                </div>
            </div>

            <!-- 3D AST Syntax Topology Tree (JavaParser 3.25 Visualizer) -->
            <div class="liquid-glass-card p-3 rounded-xl border border-cyan-500/30 relative overflow-hidden spotlight-card specular-rim-border hud-corner-box">
                <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-1.5 mb-1.5">
                    <div class="flex items-center gap-1.5">
                        <i data-lucide="network" class="w-3.5 h-3.5 text-cyan-400"></i>
                        <span class="text-[11px] font-mono text-cyan-300 font-bold uppercase tracking-wider">3D AST Syntax Graph (Cây Cú Pháp Java)</span>
                    </div>
                    <div class="flex items-center gap-2 text-[9px] font-mono">
                        <span class="flex items-center gap-1 text-rose-400"><span class="w-1.5 h-1.5 rounded-full bg-rose-500 animate-pulse"></span> Trùng lặp (88.5%)</span>
                        <span class="flex items-center gap-1 text-amber-400"><span class="w-1.5 h-1.5 rounded-full bg-amber-500"></span> Đổi tên biến</span>
                        <span class="flex items-center gap-1 text-emerald-400"><span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> An toàn</span>
                    </div>
                </div>
                <div id="cyber-3d-viewport" class="w-full h-32 relative cursor-grab active:cursor-grabbing flex items-center justify-center rounded-lg bg-[#060710]/60 border border-white/5 overflow-hidden"></div>
                <div class="flex items-center justify-between text-[9.5px] font-mono text-slate-400 mt-1.5 pt-1.5 border-t border-white/5">
                    <span>Cấu trúc: <strong class="text-cyan-300">OrderManager.java</strong> (JavaParser 3.25)</span>
                    <span class="text-cyan-400 flex items-center gap-1">
                        <span class="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-ping"></span> 🖱️ Kéo xoay 3D • Rê soi Node
                    </span>
                </div>
            </div>

            <!-- Contextual Floating Bento Metrics with 3D Tilt & Counters (Compact Cyber Cards) -->
            <div class="grid grid-cols-3 gap-2.5 pt-0.5">
                <div data-tilt class="p-2.5 rounded-xl bg-[#0e101d]/90 border border-cyan-500/25 shadow-md float-slow cursor-pointer hover:border-cyan-400/50 transition-all">
                    <div class="flex items-center justify-between gap-1 mb-0.5">
                        <span class="text-[10px] text-slate-400 font-mono">Độ chuẩn AST</span>
                        <div class="flex items-center gap-1 text-cyan-400 text-xs font-bold font-mono">
                            <i data-lucide="cpu" class="w-3 h-3"></i>
                            <span data-counter-target="99.8" data-counter-suffix="%" data-counter-decimals="1">99.8%</span>
                        </div>
                    </div>
                    <p class="text-[9px] text-slate-500 leading-tight">Đối soát cú pháp trừu tượng</p>
                </div>
                <div data-tilt class="p-2.5 rounded-xl bg-[#0e101d]/90 border border-violet-500/25 shadow-md float-delayed cursor-pointer hover:border-violet-400/50 transition-all">
                    <div class="flex items-center justify-between gap-1 mb-0.5">
                        <span class="text-[10px] text-slate-400 font-mono">Gemini AI</span>
                        <div class="flex items-center gap-1 text-violet-400 text-xs font-bold font-mono">
                            <i data-lucide="zap" class="w-3 h-3"></i>
                            <span data-counter-target="120" data-counter-prefix="<" data-counter-suffix="ms">120ms</span>
                        </div>
                    </div>
                    <p class="text-[9px] text-slate-500 leading-tight">Độ trễ nhúng ngữ nghĩa code</p>
                </div>
                <div data-tilt class="p-2.5 rounded-xl bg-[#0e101d]/90 border border-emerald-500/25 shadow-md float-slow cursor-pointer hover:border-emerald-400/50 transition-all">
                    <div class="flex items-center justify-between gap-1 mb-0.5">
                        <span class="text-[10px] text-slate-400 font-mono">Kho đối chứng</span>
                        <div class="flex items-center gap-1 text-emerald-400 text-xs font-bold font-mono">
                            <i data-lucide="database" class="w-3 h-3"></i>
                            <span data-counter-target="1280" data-counter-suffix="+">1,280+</span>
                        </div>
                    </div>
                    <p class="text-[9px] text-slate-500 leading-tight">Bài nộp sinh viên lưu trữ</p>
                </div>
            </div>
        </div>

        <!-- Right Column: Login Form Card (5 cols) -->
        <div class="lg:col-span-5 w-full animate-cyber-in stagger-2">
            <div id="login-card-container" class="p-5 sm:p-6 rounded-2xl liquid-glass-card border border-cyan-500/30 shadow-[0_0_35px_rgba(6,182,212,0.15)] relative border-beam-container spotlight-card specular-rim-border hud-corner-box">
                <div class="flex flex-col items-center text-center mb-4">
                    <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 via-indigo-500 to-violet-600 flex items-center justify-center shadow-[0_0_20px_rgba(6,182,212,0.35)] mb-2">
                        <i data-lucide="shield-alert" class="w-5 h-5 text-white font-bold"></i>
                    </div>
                    <h2 class="text-lg font-bold text-white tracking-wide flex items-center gap-1.5">
                        AITA <span class="text-cyan-400 font-mono text-[11px] px-2 py-0.5 rounded-full border border-cyan-500/40 bg-cyan-950/30">G7 - CodeDefend</span>
                    </h2>
                    <p class="text-[11px] text-slate-400 mt-0.5">Hệ Thống Kiểm Soát Mã Nguồn &amp; Phòng Thi (PRJ301)</p>
                </div>

                <!-- Role Selector Tabs with Morphing Sliding Pill -->
                <div id="role-tab-container" class="relative grid grid-cols-3 gap-1 p-1 rounded-xl bg-[#080911] border border-white/5 mb-3.5 text-xs font-medium text-slate-400">
                    <div id="role-pill-indicator" class="role-pill-indicator"></div>
                    <button type="button" onclick="selectRole('lecturer', this)" id="role-lecturer" class="role-tab-btn active relative z-10 py-1.5 rounded-lg text-white font-bold flex items-center justify-center gap-1 transition-all text-xs">
                        <i data-lucide="graduation-cap" class="w-3.5 h-3.5 text-violet-400"></i> Giảng Viên
                    </button>
                    <button type="button" onclick="selectRole('student', this)" id="role-student" class="role-tab-btn relative z-10 py-1.5 rounded-lg hover:text-white flex items-center justify-center gap-1 transition-all text-xs">
                        <i data-lucide="user" class="w-3.5 h-3.5"></i> Sinh Viên
                    </button>
                    <button type="button" onclick="selectRole('admin', this)" id="role-admin" class="role-tab-btn relative z-10 py-1.5 rounded-lg hover:text-white flex items-center justify-center gap-1 transition-all text-xs">
                        <i data-lucide="shield-check" class="w-3.5 h-3.5"></i> Khảo Thí
                    </button>
                </div>

                <!-- Alert Messages (Error & Info) -->
                <c:if test="${not empty errorMessage}">
                    <div class="mb-3 p-2.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-[11px] flex items-center gap-2">
                        <i data-lucide="alert-circle" class="w-3.5 h-3.5 shrink-0 text-rose-400"></i>
                        <span>${errorMessage}</span>
                    </div>
                </c:if>
                <c:if test="${param.message == 'logged_out'}">
                    <div class="mb-3 p-2.5 rounded-xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-300 text-[11px] flex items-center gap-2">
                        <i data-lucide="check-circle" class="w-3.5 h-3.5 shrink-0 text-cyan-400"></i>
                        <span>Bạn đã đăng xuất an toàn khỏi hệ thống AITA.</span>
                    </div>
                </c:if>
                <c:if test="${param.error == 'unauthorized'}">
                    <div class="mb-3 p-2.5 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-300 text-[11px] flex items-center gap-2">
                        <i data-lucide="shield-alert" class="w-3.5 h-3.5 shrink-0 text-amber-400"></i>
                        <span>Phiên đăng nhập đã hết hạn hoặc chưa xác thực JWT. Vui lòng đăng nhập lại.</span>
                    </div>
                </c:if>

                <!-- Login Form -->
                <form id="login-form" action="${pageContext.request.contextPath}/login" method="POST" class="space-y-3">
                    <div>
                        <label class="block text-[11px] font-mono text-slate-400 mb-1 flex items-center justify-between">
                            <span>Tài Khoản / Email EDU</span>
                            <span class="text-[10px] text-cyan-400 font-sans">@fpt.edu.vn</span>
                        </label>
                        <div class="relative">
                            <i data-lucide="mail" class="w-3.5 h-3.5 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2"></i>
                            <input type="text" id="email" name="email" required value="${lastEmail != null ? lastEmail : 'ha.nh@fpt.edu.vn'}" placeholder="nhap.email@fpt.edu.vn hoặc username" 
                                   class="w-full pl-9 pr-3 py-2 rounded-xl bg-[#080911] border border-white/10 text-xs text-white focus:outline-none focus:border-cyan-400 focus:shadow-[0_0_12px_rgba(6,182,212,0.2)] transition-all font-mono">
                        </div>
                    </div>

                    <div>
                        <label class="block text-[11px] font-mono text-slate-400 mb-1 flex items-center justify-between">
                            <span>Mật Khẩu</span>
                            <a href="javascript:void(0)" onclick="if(window.showToast) showToast('Vui lòng liên hệ Hội đồng Khảo thí (khaothi@fpt.edu.vn) để khôi phục mật khẩu tài khoản FPTU.', 'info'); else alert('Vui lòng liên hệ Phòng Khảo thí (khaothi@fpt.edu.vn)');" class="text-[10px] text-slate-500 hover:text-cyan-400 transition-colors">Quên mật khẩu?</a>
                        </label>
                        <div class="relative">
                            <i data-lucide="lock" class="w-3.5 h-3.5 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2"></i>
                            <input type="password" id="password" name="password" required value="123456" placeholder="Nhập mật khẩu của bạn" 
                                   class="w-full pl-9 pr-9 py-2 rounded-xl bg-[#080911] border border-white/10 text-xs text-white focus:outline-none focus:border-cyan-400 focus:shadow-[0_0_12px_rgba(6,182,212,0.2)] transition-all font-mono">
                            <button type="button" onclick="togglePasswordVisibility()" class="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-cyan-400 transition-colors p-1" title="Hiện/Ẩn mật khẩu">
                                <i id="password-toggle-icon" data-lucide="eye" class="w-3.5 h-3.5"></i>
                            </button>
                        </div>
                    </div>

                    <div class="flex items-center justify-between text-[11px] text-slate-400 pt-0.5">
                        <label class="flex items-center gap-1.5 cursor-pointer">
                            <input type="checkbox" checked class="w-3.5 h-3.5 rounded border-white/10 text-cyan-500 focus:ring-0 bg-[#080911]">
                            <span>Duy trì phiên (JWT 24h)</span>
                        </label>
                        <span class="text-[10px] font-mono text-emerald-400 flex items-center gap-1">
                            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-ping"></span> RFC-7519
                        </span>
                    </div>

                    <button type="submit" class="w-full py-2.5 rounded-xl bg-gradient-to-r from-cyan-500 via-indigo-600 to-violet-600 hover:from-cyan-400 hover:to-violet-500 text-xs font-bold text-white shadow-[0_0_20px_rgba(6,182,212,0.3)] transition-all flex items-center justify-center gap-2 mt-1.5 btn-shimmer relative overflow-hidden">
                        <span>Xác Thực & Vào Hệ Thống</span>
                        <i data-lucide="arrow-right" class="w-4 h-4"></i>
                    </button>
                </form>

                <!-- Google OAuth 2.0 Sign-In Divider & Button (Google Identity Services) -->
                <div class="flex items-center my-2.5">
                    <div class="flex-1 border-t border-white/10"></div>
                    <span class="px-2 text-[10px] font-mono text-slate-500 uppercase tracking-wider">Hoặc Xác Thực Google</span>
                    <div class="flex-1 border-t border-white/10"></div>
                </div>

                <div id="google-login" data-endpoint="${pageContext.request.contextPath}/login-google" class="w-full">
                    <div id="google-identity-button" class="flex justify-center"></div>
                    <button type="button" id="google-login-btn" class="w-full py-2.5 px-4 rounded-xl bg-white text-slate-800 text-xs font-medium">Đăng nhập bằng Google</button>
                    <p id="google-login-message" role="status" aria-live="polite" class="text-xs text-slate-400 mt-2"></p>
                </div>
                <script src="${pageContext.request.contextPath}/assets/js/google-login.js" defer></script>

                <!-- Bảng Chọn Nhanh Tài Khoản Demo (Kiểm Thử Nhanh) -->
                <div class="mt-3 pt-2.5 border-t border-white/5 space-y-1.5">
                    <div class="flex items-center justify-between text-[10px] font-mono text-slate-400">
                        <span>Bảng Tài Khoản (Nhấp để điền):</span>
                        <span class="text-cyan-400">Pass: 123456</span>
                    </div>
                    <div class="grid grid-cols-2 gap-1.5 text-[10.5px]">
                        <button type="button" onclick="fillAccount('ha.nh@fpt.edu.vn', 'lecturer')" class="px-2 py-1.5 rounded-lg bg-slate-900/90 border border-violet-500/30 hover:border-violet-400 text-slate-300 hover:text-white flex items-center gap-1.5 transition-all truncate text-left">
                            <i data-lucide="graduation-cap" class="w-3 h-3 text-violet-400 shrink-0"></i>
                            <span class="truncate">TS. Hoàng Hà (GV)</span>
                        </button>
                        <button type="button" onclick="fillAccount('kietnta@fpt.edu.vn', 'lecturer')" class="px-2 py-1.5 rounded-lg bg-slate-900/90 border border-cyan-500/30 hover:border-cyan-400 text-slate-300 hover:text-white flex items-center gap-1.5 transition-all truncate text-left">
                            <i data-lucide="award" class="w-3 h-3 text-cyan-400 shrink-0"></i>
                            <span class="truncate">Anh Kiệt (Leader)</span>
                        </button>
                        <button type="button" onclick="fillAccount('phuctv@fpt.edu.vn', 'student')" class="px-2 py-1.5 rounded-lg bg-slate-900/90 border border-emerald-500/30 hover:border-emerald-400 text-slate-300 hover:text-white flex items-center gap-1.5 transition-all truncate text-left">
                            <i data-lucide="user" class="w-3 h-3 text-emerald-400 shrink-0"></i>
                            <span class="truncate">Văn Phúc (SV)</span>
                        </button>
                        <button type="button" onclick="fillAccount('admin@aita.edu.vn', 'admin')" class="px-2 py-1.5 rounded-lg bg-slate-900/90 border border-amber-500/30 hover:border-amber-400 text-slate-300 hover:text-white flex items-center gap-1.5 transition-all truncate text-left">
                            <i data-lucide="shield-check" class="w-3 h-3 text-amber-400 shrink-0"></i>
                            <span class="truncate">Khảo Thí (Admin)</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <script>
        lucide.createIcons();
        let currentRole = 'lecturer';

        function selectRole(role, btn) {
            currentRole = role;
            document.querySelectorAll('.role-tab-btn').forEach(b => {
                b.classList.remove('active', 'text-white', 'font-bold');
                b.classList.add('text-slate-400');
            });
            const target = btn || document.getElementById('role-' + role);
            if (target) {
                target.classList.add('active', 'text-white', 'font-bold');
                target.classList.remove('text-slate-400');
                if (window.updateTabPill) window.updateTabPill(target);
            }
        }

        function fillAccount(email, role) {
            document.getElementById('email').value = email;
            selectRole(role);
        }

        document.getElementById('login-form').addEventListener('submit', function(e) {
            const email = document.getElementById('email').value.trim();
            const pass = document.getElementById('password').value.trim();
            const card = document.getElementById('login-card-container');

            if (!email || !pass) {
                e.preventDefault();
                if (window.triggerHapticShake) triggerHapticShake(card);
                if (window.CyberAudio) CyberAudio.playDanger();
                if (window.showToast) showToast('Vui lòng điền đầy đủ tài khoản và mật khẩu!', 'danger');
                return;
            }

            if (currentRole === 'student' && !email.includes('@')) {
                e.preventDefault();
                if (window.triggerHapticShake) triggerHapticShake(card);
                if (window.CyberAudio) CyberAudio.playDanger();
                if (window.showToast) showToast('Tài khoản sinh viên FPTU phải có định dạng email hợp lệ (@fpt.edu.vn)!', 'warning');
                return;
            }

            if (window.CyberAudio) CyberAudio.playSuccess();
        });
        function togglePasswordVisibility() {
            const passInput = document.getElementById('password');
            const icon = document.getElementById('password-toggle-icon');
            if (!passInput || !icon) return;
            if (passInput.type === 'password') {
                passInput.type = 'text';
                icon.setAttribute('data-lucide', 'eye-off');
            } else {
                passInput.type = 'password';
                icon.setAttribute('data-lucide', 'eye');
            }
            if (window.lucide) lucide.createIcons();
            if (window.CyberAudio) CyberAudio.playTick();
        }

    </script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-audio.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-particles.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-core-3d.js?v=ast_tree_2026"></script>
    <script src="${pageContext.request.contextPath}/assets/js/tilt-motion.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/terminal-stream.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion-system-2026.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/motion.js"></script>
    <!-- AITA Realtime AI Copilot (ZeroTTS Voice Engine) -->
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot-context.js?v=2.0"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot-voice.js?v=2.0"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot-chat.js?v=2.0"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot.js?v=2.0"></script>
</body>
</html>
