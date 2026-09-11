/**
 * AITA CodeDefend 2026 Motion & Micro-Interaction Engine
 * Handles Count-up Animation, Staggered Cascades, Spring Physics, and Tactile Haptics.
 */
(function() {
    'use strict';

    // 1. Smooth RequestAnimationFrame Count-up Engine
    function initCounters() {
        const counters = document.querySelectorAll('[data-counter-target]');
        if (!counters.length) return;

        const observer = new IntersectionObserver((entries, obs) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    animateCounter(entry.target);
                    obs.unobserve(entry.target);
                }
            });
        }, { threshold: 0.15 });

        counters.forEach(el => observer.observe(el));
    }

    function animateCounter(el) {
        const target = parseFloat(el.getAttribute('data-counter-target')) || 0;
        const prefix = el.getAttribute('data-counter-prefix') || '';
        const suffix = el.getAttribute('data-counter-suffix') || '';
        const decimals = parseInt(el.getAttribute('data-counter-decimals')) || 0;
        const duration = 850; // ms
        const startTime = performance.now();

        // Hiển thị ngay giá trị cuối cùng nếu chế độ tắt hiệu ứng đang kích hoạt
        if (window.CyberEffects && !window.CyberEffects.isEnabled()) {
            if (decimals > 0) {
                el.innerText = prefix + target.toFixed(decimals) + suffix;
            } else {
                el.innerText = prefix + Math.round(target).toLocaleString() + suffix;
            }
            return;
        }

        function update(now) {
            const elapsed = now - startTime;
            const progress = Math.min(elapsed / duration, 1);
            // Cubic ease-out
            const easeOut = 1 - Math.pow(1 - progress, 3);
            const currentVal = target * easeOut;

            if (decimals > 0) {
                el.innerText = prefix + currentVal.toFixed(decimals) + suffix;
            } else {
                el.innerText = prefix + Math.round(currentVal).toLocaleString() + suffix;
            }

            if (progress < 1) {
                requestAnimationFrame(update);
            } else {
                if (decimals > 0) {
                    el.innerText = prefix + target.toFixed(decimals) + suffix;
                } else {
                    el.innerText = prefix + Math.round(target).toLocaleString() + suffix;
                }
            }
        }
        requestAnimationFrame(update);
    }

    // 2. Staggered Row Cascade Engine
    function applyStagger(container) {
        if (!container) return;
        const items = container.querySelectorAll('.stagger-item, tr, .divide-y > div');
        items.forEach((item, idx) => {
            item.classList.add('stagger-row');
            item.style.animationDelay = (idx * 28) + 'ms';
        });
    }

    // 3. Tactile Button Click Sound & Ripple Injection
    function initTactileInteractions() {
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('button, a.btn-shimmer, .role-tab-btn, .radar-dot');
            if (!btn) return;

            // Trigger Audio feedback if element specifies data-sound or is an action
            if (window.CyberAudio && !btn.getAttribute('data-no-sound')) {
                const sound = btn.getAttribute('data-sound');
                if (sound === 'danger') CyberAudio.playDanger();
                else if (sound === 'laser') CyberAudio.playLaser();
                else if (sound === 'success') CyberAudio.playSuccess();
                else if (!btn.classList.contains('audio-hud-toggle')) CyberAudio.playTick();
            }
        });
    }

    // 4. Global System Notification & Help Action Handlers
    window.showSystemHelp = function() {
        if (window.CyberAudio) CyberAudio.playTick();
        if (window.showToast) {
            window.showToast("AITA CodeDefend: Kiểm soát mã nguồn bằng JavaParser AST + Gemini 1.5 Pro + Khóa băm SHA-256 (Mục 4.4.2).", "info");
        }
    };

    window.showSystemNotifications = function() {
        if (window.CyberAudio) CyberAudio.playLaser();
        if (window.showToast) {
            window.showToast("Cảnh báo mới: Phát hiện tương đồng 88.5% giữa SV A (SE1701) và SV B (SE1702) tại OrderManager.java!", "danger");
        }
    };

    // Auto-init on DOMContentLoaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            initCounters();
            initTactileInteractions();
        });
    } else {
        initCounters();
        initTactileInteractions();
    }

    // Expose helpers globally
    window.AitaMotion = {
        initCounters,
        applyStagger,
        animateCounter
    };
})();
