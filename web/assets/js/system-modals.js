/**
 * AITA CodeDefend - System Modals & Telemetry Engine (2026 Edition)
 * Quản lý 3 tính năng Khảo Thí: Kho Bài Nộp, Nhật Ký Quét, Ngưỡng Đạo Văn
 */

(function () {
    function ensureModalContainer() {
        if (document.getElementById("aita-system-modals-root")) return;

        const container = document.createElement("div");
        container.id = "aita-system-modals-root";
        container.innerHTML = `
        <!-- Modal 1: Kho Bài Nộp Sinh Viên (Repository Explorer) -->
        <div id="modal-repo-explorer" class="ast-modal-backdrop hidden" onclick="if(event.target === this) closeSystemModal('modal-repo-explorer')">
            <div class="ast-modal-card p-6 border border-cyan-500/40 relative max-w-5xl w-full max-h-[90vh] flex flex-col liquid-glass-card specular-rim-border">
                <button onclick="closeSystemModal('modal-repo-explorer')" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
                
                <div class="flex items-center justify-between pb-4 mb-4 border-b border-white/10 shrink-0">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-xl bg-cyan-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-400 shadow-[0_0_15px_rgba(6,182,212,0.2)]">
                            <i data-lucide="folder-git-2" class="w-5 h-5"></i>
                        </div>
                        <div>
                            <h3 class="text-base font-bold text-white font-mono flex items-center gap-2">
                                Kho Bài Nộp Sinh Viên PRJ301
                                <span class="px-2 py-0.5 rounded text-[10px] bg-cyan-950 text-cyan-300 border border-cyan-500/40 font-bold">42 BÀI NỘP</span>
                            </h3>
                            <p class="text-xs text-slate-400 font-mono">Quản lý kho bài thi & đối soát mã băm SHA-256 (Filter 4.4.2)</p>
                        </div>
                    </div>
                    <div class="flex items-center gap-2">
                        <span class="px-2.5 py-1 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-mono flex items-center gap-1.5">
                            <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span> Docker Sandbox Verified
                        </span>
                    </div>
                </div>

                <!-- Search & Filters -->
                <div class="flex items-center justify-between gap-3 mb-4 shrink-0">
                    <div class="relative flex-1 max-w-sm">
                        <i data-lucide="search" class="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2"></i>
                        <input type="text" id="repo-search-input" oninput="filterRepoTable(this.value)" placeholder="Tìm kiếm theo MSSV, Tên hoặc Mã Băm SHA-256..." class="w-full pl-9 pr-3 py-1.5 rounded-lg bg-black/40 border border-white/10 text-xs text-white font-mono focus:border-cyan-400 focus:outline-none">
                    </div>
                    <div class="flex items-center gap-2 text-xs font-mono">
                        <span class="text-slate-400">Lọc theo:</span>
                        <select onchange="filterRepoStatus(this.value)" class="px-2.5 py-1.5 rounded-lg bg-[#111322] border border-white/10 text-slate-300 text-xs focus:outline-none">
                            <option value="all">Tất cả bài nộp (42)</option>
                            <option value="flagged">Bị gắn cờ đỏ (>80%)</option>
                            <option value="safe">An toàn (<30%)</option>
                        </select>
                    </div>
                </div>

                <!-- Table Content -->
                <div class="flex-1 overflow-y-auto rounded-xl border border-white/5 bg-[#090a14]">
                    <table class="w-full text-left text-xs font-mono">
                        <thead class="bg-white/5 text-slate-400 uppercase text-[10px] sticky top-0 backdrop-blur-md">
                            <tr>
                                <th class="py-2.5 px-3">MSSV / Sinh Viên</th>
                                <th class="py-2.5 px-3">Tệp Mã Nguồn</th>
                                <th class="py-2.5 px-3">Vân Tay SHA-256</th>
                                <th class="py-2.5 px-3">Độ Trùng Lặp</th>
                                <th class="py-2.5 px-3">Trạng Thái</th>
                                <th class="py-2.5 px-3 text-right">Hành Động</th>
                            </tr>
                        </thead>
                        <tbody id="repo-table-body" class="divide-y divide-white/5 text-slate-300">
                            <!-- Injected by script -->
                        </tbody>
                    </table>
                </div>

                <!-- Footer Stats -->
                <div class="pt-3 mt-3 border-t border-white/10 flex items-center justify-between text-xs font-mono text-slate-400 shrink-0">
                    <span>Hiển thị <b class="text-white" id="repo-count-shown">42</b> / 42 bài nộp đã khóa sổ</span>
                    <button onclick="exportRepoList()" class="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-cyan-300 border border-cyan-500/30 text-xs flex items-center gap-1.5">
                        <i data-lucide="download" class="w-3.5 h-3.5"></i> Xuất Báo Cáo SHA-256
                    </button>
                </div>
            </div>
        </div>

        <!-- Modal 2: Nhật Ký Quét Hệ Thống (Scan Logs & Audit Trail) -->
        <div id="modal-scan-logs" class="ast-modal-backdrop hidden" onclick="if(event.target === this) closeSystemModal('modal-scan-logs')">
            <div class="ast-modal-card p-6 border border-violet-500/40 relative max-w-4xl w-full max-h-[85vh] flex flex-col liquid-glass-card specular-rim-border">
                <button onclick="closeSystemModal('modal-scan-logs')" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
                
                <div class="flex items-center justify-between pb-4 mb-4 border-b border-white/10 shrink-0">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-xl bg-violet-500/20 border border-violet-500/40 flex items-center justify-center text-violet-400 shadow-[0_0_15px_rgba(139,92,246,0.2)]">
                            <i data-lucide="history" class="w-5 h-5"></i>
                        </div>
                        <div>
                            <h3 class="text-base font-bold text-white font-mono flex items-center gap-2">
                                Nhật Ký Quét &amp; Kiểm Định Liêm Chính
                                <span class="px-2 py-0.5 rounded text-[10px] bg-violet-950 text-violet-300 border border-violet-500/40 font-bold">AUDIT TRAIL</span>
                            </h3>
                            <p class="text-xs text-slate-400 font-mono">Bản ghi vết sự kiện kiểm soát gian lận mã nguồn PRJ301</p>
                        </div>
                    </div>
                    <div class="flex items-center gap-2">
                        <span class="text-[10px] font-mono px-2.5 py-1 rounded bg-cyan-950/80 border border-cyan-500/40 text-cyan-300 flex items-center gap-1">
                            <span class="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-pulse"></span> 60 FPS Engine Stream
                        </span>
                    </div>
                </div>

                <!-- Log Stream Console Body -->
                <div id="scan-log-stream-content" class="flex-1 overflow-y-auto p-4 rounded-xl bg-[#070812] border border-white/10 font-mono text-xs space-y-1.5 select-text shadow-inner">
                    <!-- Logs injected dynamically -->
                </div>

                <!-- Footer Console Actions -->
                <div class="pt-3 mt-3 border-t border-white/10 flex items-center justify-between text-xs font-mono shrink-0">
                    <div class="flex items-center gap-2 text-slate-400">
                        <i data-lucide="shield-check" class="w-3.5 h-3.5 text-emerald-400"></i>
                        <span>SHA-256 Signature: <code class="text-slate-300">d41d8cd98f00b204e9800998ecf8427e</code></span>
                    </div>
                    <div class="flex items-center gap-2">
                        <button onclick="clearScanLogs()" class="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-rose-950/40 text-slate-400 hover:text-rose-300 border border-white/10 text-xs">
                            Xóa màn hình
                        </button>
                        <button onclick="exportScanLogs()" class="px-3 py-1.5 rounded-lg bg-violet-600/30 hover:bg-violet-600/50 text-violet-200 border border-violet-500/40 text-xs font-semibold flex items-center gap-1">
                            <i data-lucide="download" class="w-3.5 h-3.5"></i> Tải Log (.txt)
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal 3: Ngưỡng Đạo Văn (Similarity Thresholds Configuration) -->
        <div id="modal-threshold-config" class="ast-modal-backdrop hidden" onclick="if(event.target === this) closeSystemModal('modal-threshold-config')">
            <div class="ast-modal-card p-6 border border-amber-500/40 relative max-w-xl w-full flex flex-col liquid-glass-card specular-rim-border">
                <button onclick="closeSystemModal('modal-threshold-config')" class="absolute top-4 right-4 text-slate-400 hover:text-white text-xl leading-none">&times;</button>
                
                <div class="flex items-center justify-between pb-4 mb-5 border-b border-white/10 shrink-0">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400 shadow-[0_0_15px_rgba(245,158,11,0.2)]">
                            <i data-lucide="sliders" class="w-5 h-5"></i>
                        </div>
                        <div>
                            <h3 class="text-base font-bold text-white font-mono flex items-center gap-2">
                                Cấu Hình Ngưỡng Đạo Văn
                                <span class="px-2 py-0.5 rounded text-[10px] bg-amber-950 text-amber-300 border border-amber-500/40 font-bold">CONFIG</span>
                            </h3>
                            <p class="text-xs text-slate-400 font-mono">Thiết lập thuật toán bóc tách cú pháp &amp; phân tích ngữ nghĩa AI</p>
                        </div>
                    </div>
                </div>

                <div class="space-y-5 font-mono text-xs">
                    <!-- Slider 1: Trọng số AST vs Gemini -->
                    <div class="p-3.5 rounded-xl bg-[#0a0c18] border border-white/5 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-slate-300 font-bold flex items-center gap-1.5">
                                <i data-lucide="cpu" class="w-3.5 h-3.5 text-cyan-400"></i> Trọng Số Cây Cú Pháp AST
                            </span>
                            <span id="ast-weight-display" class="text-cyan-400 font-extrabold text-sm">40%</span>
                        </div>
                        <input type="range" id="ast-weight-slider" min="10" max="90" value="40" step="5" oninput="updateWeights(this.value)" class="w-full accent-cyan-400 cursor-pointer">
                        <div class="flex items-center justify-between text-[10px] text-slate-500">
                            <span>JavaParser Token AST: <b id="ast-sub-val" class="text-cyan-300">40%</b></span>
                            <span>Google Gemini 1.5 Semantic: <b id="ai-sub-val" class="text-violet-400">60%</b></span>
                        </div>
                    </div>

                    <!-- Slider 2: Ngưỡng Rủi Ro Cao (Cờ đỏ) -->
                    <div class="p-3.5 rounded-xl bg-[#0a0c18] border border-white/5 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-rose-300 font-bold flex items-center gap-1.5">
                                <i data-lucide="alert-octagon" class="w-3.5 h-3.5 text-rose-400"></i> Ngưỡng Rủi Ro Cao (Báo Cờ Đỏ)
                            </span>
                            <span id="red-flag-display" class="text-rose-400 font-extrabold text-sm">85%</span>
                        </div>
                        <input type="range" id="red-flag-slider" min="60" max="95" value="85" step="1" oninput="document.getElementById('red-flag-display').innerText = this.value + '%'" class="w-full accent-rose-500 cursor-pointer">
                        <p class="text-[10px] text-slate-500">Tự động đình chỉ bài thi và kích hoạt cơ chế giải trình khiếu nại (Nhóm 7).</p>
                    </div>

                    <!-- Slider 3: Ngưỡng Cảnh Báo (Cờ vàng) -->
                    <div class="p-3.5 rounded-xl bg-[#0a0c18] border border-white/5 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-amber-300 font-bold flex items-center gap-1.5">
                                <i data-lucide="alert-triangle" class="w-3.5 h-3.5 text-amber-400"></i> Ngưỡng Cảnh Báo (Cờ Vàng)
                            </span>
                            <span id="yellow-flag-display" class="text-amber-400 font-extrabold text-sm">60%</span>
                        </div>
                        <input type="range" id="yellow-flag-slider" min="40" max="75" value="60" step="1" oninput="document.getElementById('yellow-flag-display').innerText = this.value + '%'" class="w-full accent-amber-500 cursor-pointer">
                        <p class="text-[10px] text-slate-500">Yêu cầu giảng viên chấm thi rà soát thủ công trước khi công bố điểm.</p>
                    </div>

                    <!-- System Invariants Checkboxes -->
                    <div class="space-y-2 pt-1 border-t border-white/5 text-[11px]">
                        <label class="flex items-center gap-2 cursor-pointer text-slate-300">
                            <input type="checkbox" checked id="chk-sandbox" class="rounded border-white/10 text-cyan-500 bg-[#080911]">
                            <span>Bật môi trường cách ly Docker Sandbox 4.5.2 khi quét</span>
                        </label>
                        <label class="flex items-center gap-2 cursor-pointer text-slate-300">
                            <input type="checkbox" checked id="chk-sha256" class="rounded border-white/10 text-cyan-500 bg-[#080911]">
                            <span>Yêu cầu khóa băm SHA-256 toàn vẹn mã nguồn 4.4.2</span>
                        </label>
                    </div>
                </div>

                <!-- Footer Actions -->
                <div class="pt-4 mt-4 border-t border-white/10 flex items-center justify-between shrink-0">
                    <button type="button" onclick="closeSystemModal('modal-threshold-config')" class="px-4 py-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 text-xs font-mono">
                        Đóng
                    </button>
                    <button type="button" onclick="saveThresholdConfig()" class="px-5 py-2 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-black font-mono text-xs font-bold shadow-[0_0_15px_rgba(245,158,11,0.3)] transition-all">
                        Lưu Cấu Hình
                    </button>
                </div>
            </div>
        </div>
        `;
        document.body.appendChild(container);
        if (window.lucide) lucide.createIcons();
    }

    const sampleSubmissions = [
        { id: "SE1701", name: "Trần Văn Long", file: "OrderManager_LongTV.java", hash: "e3b0c44298fc1c149afbf4c8996fb924", sim: 88.5, status: "flagged" },
        { id: "SE1702", name: "Lê Hoàng Nam", file: "OrderManager_NamLH.java", hash: "4b227777d4dd1fc61c6f884f48641d02", sim: 88.5, status: "flagged" },
        { id: "SE1703", name: "Nguyễn Minh Quang", file: "CartController_QuangNM.java", hash: "8f9a4c7e2b10a9c84e6f112233445566", sim: 14.2, status: "safe" },
        { id: "SE1704", name: "Phạm Thùy Linh", file: "PaymentGateway_LinhPT.java", hash: "7c23a5b8e99120bcdeff123456789abc", sim: 21.0, status: "safe" },
        { id: "SE1705", name: "Vũ Gia Bảo", file: "UserDAO_BaoVG.java", hash: "1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d", sim: 64.5, status: "warning" },
        { id: "SE1706", name: "Đỗ Anh Tuấn", file: "ProductServlet_TuanDA.java", hash: "99887766554433221100aabbccddeeff", sim: 12.0, status: "safe" },
        { id: "SE1707", name: "Bùi Khánh Huyền", file: "DBContext_HuyenBK.java", hash: "a1b2c3d4e5f60718293a4b5c6d7e8f90", sim: 8.5, status: "safe" },
        { id: "SE1708", name: "Ngô Quốc Huy", file: "AuthenticationFilter_HuyNQ.java", hash: "f0e1d2c3b4a5968778695a4b3c2d1e0f", sim: 82.0, status: "flagged" },
        { id: "SE1709", name: "Hoàng Mai Chi", file: "OrderManager_ChiHM.java", hash: "3344556677889900aabbccddeeff1122", sim: 18.3, status: "safe" },
        { id: "SE1710", name: "Đinh Văn Tiến", file: "InventoryService_TienDV.java", hash: "55667788990011223344aabbccddeeff", sim: 29.4, status: "safe" }
    ];

    for (let i = 11; i <= 42; i++) {
        const sim = (Math.random() * 45 + 5).toFixed(1);
        sampleSubmissions.push({
            id: `SE17${i < 10 ? '0' + i : i}`,
            name: `Sinh Viên PRJ301 #${i}`,
            file: `OrderService_SE17${i}.java`,
            hash: `hash_${i}_` + Math.random().toString(16).substring(2, 10) + `9988`,
            sim: parseFloat(sim),
            status: sim > 80 ? "flagged" : (sim > 50 ? "warning" : "safe")
        });
    }

    function renderRepoTable(items) {
        const tbody = document.getElementById("repo-table-body");
        const countShown = document.getElementById("repo-count-shown");
        if (!tbody) return;
        tbody.innerHTML = items.map(s => {
            let badge = `<span class="px-2 py-0.5 rounded text-[10px] bg-emerald-950 text-emerald-400 border border-emerald-500/30">An toàn</span>`;
            let simColor = "text-emerald-400";
            if (s.status === "flagged" || s.sim >= 80) {
                badge = `<span class="px-2 py-0.5 rounded text-[10px] bg-rose-950 text-rose-400 border border-rose-500/40 font-bold">Cờ Đỏ AST</span>`;
                simColor = "text-rose-400 font-bold";
            } else if (s.status === "warning" || s.sim >= 50) {
                badge = `<span class="px-2 py-0.5 rounded text-[10px] bg-amber-950 text-amber-400 border border-amber-500/40">Cảnh báo</span>`;
                simColor = "text-amber-400";
            }

            return `
                <tr class="hover:bg-white/[0.02] transition-colors">
                    <td class="py-2.5 px-3">
                        <span class="font-bold text-white">${s.id}</span>
                        <div class="text-[10px] text-slate-400">${s.name}</div>
                    </td>
                    <td class="py-2.5 px-3 text-cyan-300 font-semibold">${s.file}</td>
                    <td class="py-2.5 px-3">
                        <code class="text-[10px] text-slate-400 bg-black/50 px-1.5 py-0.5 rounded border border-white/5">${s.hash.substring(0, 16)}...</code>
                    </td>
                    <td class="py-2.5 px-3 ${simColor}">${s.sim}%</td>
                    <td class="py-2.5 px-3">${badge}</td>
                    <td class="py-2.5 px-3 text-right">
                        <a href="diff-inspector.html?reportId=1" onclick="if(window.CyberAudio) CyberAudio.playLaser();" class="px-2.5 py-1 rounded bg-cyan-950/70 hover:bg-cyan-900 border border-cyan-500/30 text-cyan-300 text-[10px] transition-colors inline-flex items-center gap-1">
                            <i data-lucide="split-square-vertical" class="w-3 h-3"></i> Đối Soát
                        </a>
                    </td>
                </tr>
            `;
        }).join("");
        if (countShown) countShown.innerText = items.length;
        if (window.lucide) lucide.createIcons();
    }

    const logEntries = [
        "[08/09 14:32:01] [SYSTEM] AITA CodeDefend v2.4 AST Suite initialized (PID 8412)",
        "[08/09 14:32:01] [SECURITY] SHA-256 Signature Verification active (Standard RBL 4.4.2)",
        "[08/09 14:32:02] [REPO] Indexed 42 student assignments for PRJ301 - Fall 2026",
        "[08/09 14:32:02] [SANDBOX] Docker isolation worker active (Resource quota: 256MB RAM / 1 Core)",
        "[08/09 14:32:03] [AST-PARSER] JavaParser-3.25 building syntactic parse trees across 84 source files...",
        "[08/09 14:32:04] [NORMALIZE] Identifier renaming & statement order normalization completed.",
        "[08/09 14:32:05] [GEMINI-1.5] Google Gemini API connection established (Quota: 42/50 RPM, Latency: 118ms)",
        "[08/09 14:32:06] [AI-EMBED] Vectorizing structural AST trees into 768-dim semantic space...",
        "[08/09 14:32:07] [CRITICAL-FLAG] High risk cluster detected: OrderManager.java (88.5% Similarity)",
        "[08/09 14:32:07] [PAIR] Student SE1701 (Tran Van Long) vs Student SE1702 (Le Hoang Nam)",
        "[08/09 14:32:08] [AUDIT-LOCK] Forensic hash artifact computed: e3b0c44298fc1c149afbf4c8996fb924",
        "[08/09 14:32:09] [ECOLE-42] Correction point deducted (-1 Pt) for peer-review discrepancy",
        "[08/09 14:32:10] [MONITOR] System ready for interactive query & telemetry dispatch."
    ];

    function renderScanLogs() {
        const container = document.getElementById("scan-log-stream-content");
        if (!container) return;
        container.innerHTML = logEntries.map(log => {
            let color = "text-slate-300";
            if (log.includes("[CRITICAL-FLAG]")) color = "text-rose-400 font-bold bg-rose-950/30 px-1 py-0.5 rounded";
            else if (log.includes("[SECURITY]") || log.includes("[SANDBOX]")) color = "text-emerald-400 font-semibold";
            else if (log.includes("[GEMINI-1.5]") || log.includes("[AI-EMBED]")) color = "text-violet-300";
            else if (log.includes("[AST-PARSER]")) color = "text-cyan-300";
            return `<div class="${color}">${log}</div>`;
        }).join("");
    }

    window.openRepositoryModal = function () {
        ensureModalContainer();
        renderRepoTable(sampleSubmissions);
        const modal = document.getElementById("modal-repo-explorer");
        if (modal) {
            modal.classList.remove("hidden");
            modal.classList.add("active");
        }
        if (window.CyberAudio) CyberAudio.playTick();
    };

    window.openScanLogsModal = function () {
        ensureModalContainer();
        renderScanLogs();
        const modal = document.getElementById("modal-scan-logs");
        if (modal) {
            modal.classList.remove("hidden");
            modal.classList.add("active");
        }
        if (window.CyberAudio) CyberAudio.playLaser();
    };

    window.openThresholdModal = function () {
        ensureModalContainer();
        const modal = document.getElementById("modal-threshold-config");
        if (modal) {
            modal.classList.remove("hidden");
            modal.classList.add("active");
        }
        if (window.CyberAudio) CyberAudio.playTick();
    };

    window.closeSystemModal = function (id) {
        const modal = document.getElementById(id);
        if (modal) {
            modal.classList.remove("active");
            modal.classList.add("hidden");
        }
    };

    window.filterRepoTable = function (keyword) {
        const q = (keyword || "").toLowerCase();
        const filtered = sampleSubmissions.filter(s => 
            s.id.toLowerCase().includes(q) || 
            s.name.toLowerCase().includes(q) || 
            s.hash.toLowerCase().includes(q) || 
            s.file.toLowerCase().includes(q)
        );
        renderRepoTable(filtered);
    };

    window.filterRepoStatus = function (status) {
        if (status === "all") {
            renderRepoTable(sampleSubmissions);
        } else if (status === "flagged") {
            renderRepoTable(sampleSubmissions.filter(s => s.status === "flagged" || s.sim >= 80));
        } else if (status === "safe") {
            renderRepoTable(sampleSubmissions.filter(s => s.status === "safe" && s.sim < 40));
        }
    };

    window.updateWeights = function (val) {
        const astVal = parseInt(val);
        const aiVal = 100 - astVal;
        const d = document.getElementById("ast-weight-display");
        const s1 = document.getElementById("ast-sub-val");
        const s2 = document.getElementById("ai-sub-val");
        if (d) d.innerText = astVal + "%";
        if (s1) s1.innerText = astVal + "%";
        if (s2) s2.innerText = aiVal + "%";
    };

    window.saveThresholdConfig = function () {
        const astVal = document.getElementById("ast-weight-slider")?.value || "40";
        const redVal = document.getElementById("red-flag-slider")?.value || "85";
        const yellowVal = document.getElementById("yellow-flag-slider")?.value || "60";
        
        localStorage.setItem("aita_cfg_ast_weight", astVal);
        localStorage.setItem("aita_cfg_red_flag", redVal);
        localStorage.setItem("aita_cfg_yellow_flag", yellowVal);

        closeSystemModal('modal-threshold-config');
        if (window.CyberAudio) CyberAudio.playSuccess();
        if (window.showToast) {
            window.showToast(`Đã lưu cấu hình: AST ${astVal}%, Ngưỡng đỏ ${redVal}%, Ngưỡng vàng ${yellowVal}%!`, "success");
        } else {
            alert("Đã lưu cấu hình ngưỡng đạo văn thành công!");
        }
    };

    window.exportRepoList = function () {
        if (window.CyberAudio) CyberAudio.playSuccess();
        if (window.showToast) window.showToast("Đã trích xuất báo cáo SHA-256 của 42 bài nộp (PRJ301_Repo_Report.csv)!", "success");
    };

    window.exportScanLogs = function () {
        if (window.CyberAudio) CyberAudio.playSuccess();
        if (window.showToast) window.showToast("Đã xuất tệp Audit_Trail_Telemetry.log thành công!", "success");
    };

    window.clearScanLogs = function () {
        const c = document.getElementById("scan-log-stream-content");
        if (c) c.innerHTML = "<div class='text-slate-500 font-mono text-[11px]'>Console cleared by user.</div>";
    };

    document.addEventListener("DOMContentLoaded", () => {
        ensureModalContainer();
        bindSidebarLinks();
    });

    function bindSidebarLinks() {
        document.querySelectorAll('a[title="Kho Bài Nộp"]').forEach(a => {
            a.removeAttribute("href");
            a.style.cursor = "pointer";
            a.addEventListener("click", (e) => {
                e.preventDefault();
                openRepositoryModal();
            });
        });

        document.querySelectorAll('a[title="Nhật Ký Quét"]').forEach(a => {
            a.removeAttribute("href");
            a.style.cursor = "pointer";
            a.addEventListener("click", (e) => {
                e.preventDefault();
                openScanLogsModal();
            });
        });

        document.querySelectorAll('a[title="Ngưỡng Đạo Văn"]').forEach(a => {
            a.removeAttribute("href");
            a.style.cursor = "pointer";
            a.addEventListener("click", (e) => {
                e.preventDefault();
                openThresholdModal();
            });
        });
    }
})();
