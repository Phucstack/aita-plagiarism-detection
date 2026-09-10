<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${param.title != null ? param.title : 'AITA - AI Plagiarism & Code Similarity'}</title>
    <!-- Tailwind CSS v3 via CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: {
                        cyber: {
                            bg: '#0a0a12',
                            card: '#12121c',
                            cyan: '#06b6d4',
                            violet: '#7c3aed',
                            danger: '#ef4444',
                            warning: '#f59e0b',
                            success: '#10b981'
                        }
                    }
                }
            }
        }
    </script>
    <!-- Lucide Icons -->
    <script src="https://unpkg.com/lucide@latest"></script>
    <!-- Custom Motion CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body class="cinema-grain bg-[#0a0a12] text-slate-100 min-h-screen flex flex-col">
    <!-- Cyber-SaaS Navigation Bar -->
    <header class="border-b border-white/10 bg-[#0c0d18]/80 backdrop-blur-md sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 to-violet-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
                    <i data-lucide="shield-alert" class="w-5 h-5 text-white"></i>
                </div>
                <div>
                    <span class="text-xl font-bold tracking-tight bg-gradient-to-r from-cyan-400 via-violet-400 to-pink-400 bg-clip-text text-transparent">AITA</span>
                    <span class="text-xs px-2 py-0.5 ml-2 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 font-mono">G4 - Code Plagiarism</span>
                </div>
            </div>
            <!-- Navigation Links -->
            <nav class="hidden md:flex items-center gap-1">
                <a href="${pageContext.request.contextPath}/dashboard" class="px-4 py-2 rounded-lg text-sm font-medium transition-colors ${param.active == 'dashboard' ? 'bg-white/10 text-cyan-400' : 'text-slate-400 hover:text-white hover:bg-white/5'}">
                    <span class="flex items-center gap-2"><i data-lucide="layout-dashboard" class="w-4 h-4"></i> Tổng quan</span>
                </a>
                <a href="${pageContext.request.contextPath}/batch-scanner" class="px-4 py-2 rounded-lg text-sm font-medium transition-colors ${param.active == 'scanner' ? 'bg-white/10 text-cyan-400' : 'text-slate-400 hover:text-white hover:bg-white/5'}">
                    <span class="flex items-center gap-2"><i data-lucide="scan-line" class="w-4 h-4"></i> Quét hàng loạt</span>
                </a>
                <a href="${pageContext.request.contextPath}/diff-inspector?reportId=1" class="px-4 py-2 rounded-lg text-sm font-medium transition-colors ${param.active == 'diff' ? 'bg-white/10 text-cyan-400' : 'text-slate-400 hover:text-white hover:bg-white/5'}">
                    <span class="flex items-center gap-2"><i data-lucide="git-compare" class="w-4 h-4"></i> Soi đối chứng kép</span>
                </a>
            </nav>
            <!-- User Status & Audio Toggle -->
            <div class="flex items-center gap-3">
                <!-- Cyber Audio HUD Toggle Button -->
                <button onclick="CyberAudio.toggle()" class="audio-hud-toggle p-2 rounded-xl border border-white/10 bg-white/5 hover:bg-cyan-500/10 hover:border-cyan-500/40 text-slate-400 hover:text-cyan-300 transition-all flex items-center justify-center shrink-0" title="Bật/Tắt Âm Thanh Tương Tác HUD">
                    <i data-lucide="volume-x" class="w-4 h-4"></i>
                </button>

                <div class="hidden sm:block text-right">
                    <div class="text-sm font-medium text-slate-200">TS. Nguyễn Hoàng Hà</div>
                    <div class="text-xs text-slate-400">Giảng viên PRJ301</div>
                </div>
                <div class="w-9 h-9 rounded-full bg-gradient-to-r from-violet-600 to-indigo-600 p-0.5">
                    <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100" alt="Avatar" class="w-full h-full rounded-full object-cover">
                </div>
            </div>
        </div>
    </header>
    <script src="${pageContext.request.contextPath}/assets/js/cyber-audio.js"></script>
    <main class="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
