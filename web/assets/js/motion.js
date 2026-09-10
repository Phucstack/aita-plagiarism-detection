/**
 * AITA Interactive Motion & DOM Micro-interactions
 * Supporting Group 4 - AI Plagiarism & Code Similarity
 */

document.addEventListener("DOMContentLoaded", () => {
    // Khởi chạy các bộ đếm số động khi nạp trang
    const counterElements = document.querySelectorAll("[data-counter-target]");
    counterElements.forEach(el => {
        const target = parseFloat(el.getAttribute("data-counter-target"));
        const suffix = el.getAttribute("data-counter-suffix") || "";
        animateCounter(el, target, suffix, 1400);
    });

    // Đồng bộ cuộn giữa 2 khung soạn thảo mã nguồn (Synced Scrolling)
    setupSyncedScrolling("editor-student-a", "editor-student-b");

    // Khởi tạo trạng thái Sidebar từ localStorage
    initSidebarState();

    // Khởi chạy hiệu ứng chuột Spotlight (Aceternity UI)
    initSpotlightCards();

    // Lắng nghe phím tắt toàn cục: Ctrl+B / Cmd+B (Sidebar) và Escape (Đóng Modal)
    document.addEventListener("keydown", (e) => {
        if ((e.ctrlKey || e.metaKey) && (e.key === "b" || e.key === "B")) {
            e.preventDefault();
            toggleSidebar();
            return;
        }
        if (e.key === "Escape") {
            const openModals = document.querySelectorAll("#ast-modal, #appeal-modal, .aita-modal");
            openModals.forEach(m => {
                m.classList.add("hidden");
                m.classList.remove("active");
            });
        }
    });
});

/**
 * Hiệu ứng nhảy số động mượt mà bằng requestAnimationFrame (Cubic Ease-Out)
 */
function animateCounter(element, targetValue, suffix = "", duration = 1200) {
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        const easeOut = 1 - Math.pow(1 - progress, 3);
        const current = (easeOut * targetValue).toFixed(targetValue % 1 === 0 ? 0 : 1);
        element.innerText = current + suffix;
        if (progress < 1) {
            window.requestAnimationFrame(step);
        }
    };
    window.requestAnimationFrame(step);
}

/**
 * Đồng bộ cuộn giữa 2 cột mã nguồn đối chứng
 */
function setupSyncedScrolling(idA, idB) {
    const elA = document.getElementById(idA);
    const elB = document.getElementById(idB);
    if (!elA || !elB) return;

    let isSyncingA = false;
    let isSyncingB = false;

    elA.addEventListener("scroll", () => {
        if (!isSyncingA) {
            isSyncingB = true;
            elB.scrollTop = elA.scrollTop;
            elB.scrollLeft = elA.scrollLeft;
        }
        isSyncingA = false;
    });

    elB.addEventListener("scroll", () => {
        if (!isSyncingB) {
            isSyncingA = true;
            elA.scrollTop = elB.scrollTop;
            elA.scrollLeft = elB.scrollLeft;
        }
        isSyncingB = false;
    });
}

/**
 * Làm nổi bật đoạn mã tương ứng khi hover (Đồng bộ SV A & SV B)
 */
function highlightBlock(blockIndex) {
    document.querySelectorAll(`[data-match-block="${blockIndex}"], [data-block="${blockIndex}"]`).forEach(el => {
        el.classList.add("ring-2", "ring-cyan-400", "bg-cyan-950/40");
    });
}

function unhighlightBlock(blockIndex) {
    document.querySelectorAll(`[data-match-block="${blockIndex}"], [data-block="${blockIndex}"]`).forEach(el => {
        el.classList.remove("ring-2", "ring-cyan-400", "bg-cyan-950/40");
    });
}

/**
 * Hệ thống Toast Notification nổi
 */
function showToast(message, type = "info") {
    let container = document.getElementById("toast-container");
    if (!container) {
        container = document.createElement("div");
        container.id = "toast-container";
        container.className = "toast-container";
        document.body.appendChild(container);
    }

    const toast = document.createElement("div");
    toast.className = "toast-item";
    
    let icon = "info";
    let iconColor = "text-cyan-400";
    if (type === "success") { icon = "check-circle"; iconColor = "text-emerald-400"; }
    else if (type === "warning") { icon = "alert-triangle"; iconColor = "text-amber-400"; }
    else if (type === "danger") { icon = "alert-octagon"; iconColor = "text-rose-400"; }

    toast.innerHTML = `
        <div class="w-6 h-6 rounded-lg bg-white/5 flex items-center justify-center shrink-0 ${iconColor}">
            <i data-lucide="${icon}" class="w-4 h-4"></i>
        </div>
        <div class="flex-1 font-mono">${message}</div>
        <button onclick="this.parentElement.remove()" class="text-slate-500 hover:text-white">&times;</button>
    `;

    container.appendChild(toast);
    if (window.lucide) lucide.createIcons();

    setTimeout(() => {
        toast.style.animation = "fadeOutRight 0.3s cubic-bezier(0.22, 1, 0.36, 1) forwards";
        setTimeout(() => toast.remove(), 300);
    }, 8000);
}

/**
 * Quản lý Modal Thuật toán Gemini AST
 */
function openAstModal() {
    const modal = document.getElementById("ast-modal");
    if (modal) {
        modal.classList.add("active");
    }
}

function closeAstModal() {
    const modal = document.getElementById("ast-modal");
    if (modal) {
        modal.classList.remove("active");
    }
}

/**
 * Quản lý Trạng thái Ẩn / Mở rộng Sidebar với localStorage
 */
function initSidebarState() {
    const isCollapsed = localStorage.getItem("aita_sidebar_collapsed") === "true";
    const sidebar = document.getElementById("master-sidebar");
    if (sidebar && isCollapsed) {
        sidebar.classList.add("collapsed");
    }
    updateSidebarToggleIcon(isCollapsed);
}

function toggleSidebar() {
    const sidebar = document.getElementById("master-sidebar");
    if (!sidebar) return;
    sidebar.classList.toggle("collapsed");
    const isCollapsed = sidebar.classList.contains("collapsed");
    localStorage.setItem("aita_sidebar_collapsed", isCollapsed ? "true" : "false");
    updateSidebarToggleIcon(isCollapsed);
}

function updateSidebarToggleIcon(isCollapsed) {
    const toggleBtn = document.getElementById("sidebar-toggle-btn");
    if (toggleBtn) {
        toggleBtn.setAttribute("title", isCollapsed ? "Mở rộng Sidebar (Phím tắt: [ )" : "Thu gọn Sidebar (Phím tắt: [ )");
        toggleBtn.innerHTML = isCollapsed 
            ? '<i data-lucide="panel-left-open" class="w-4 h-4 text-cyan-400"></i>'
            : '<i data-lucide="panel-left-close" class="w-4 h-4 text-slate-400"></i>';
        if (window.lucide) lucide.createIcons();
    }
}

/**
 * Hiệu ứng Chuột Rọi Spotlight Card (Aceternity UI)
 */
function initSpotlightCards() {
    document.addEventListener("mousemove", (e) => {
        const cards = document.querySelectorAll(".spotlight-card, .specular-rim-border");
        cards.forEach(card => {
            const rect = card.getBoundingClientRect();
            // Tối ưu chỉ tính toán khi con trỏ ở lân cận card
            if (
                e.clientX >= rect.left - 80 &&
                e.clientX <= rect.right + 80 &&
                e.clientY >= rect.top - 80 &&
                e.clientY <= rect.bottom + 80
            ) {
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;
                card.style.setProperty("--mouse-x", `${x}px`);
                card.style.setProperty("--mouse-y", `${y}px`);
            }
        });
    });
}

/**
 * Hiệu ứng Rung Haptic Shake khi Input Validation lỗi
 */
function triggerHapticShake(el) {
    if (!el) return;
    el.classList.remove('haptic-shake');
    void el.offsetWidth; // trigger reflow
    el.classList.add('haptic-shake');
    if (window.CyberAudio) window.CyberAudio.playDanger();
}
window.triggerHapticShake = triggerHapticShake;


