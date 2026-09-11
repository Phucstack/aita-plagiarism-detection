/**
 * ==========================================================================
 * AITA CodeDefend 2026 - Visual Effects Manager (CyberEffects)
 * Quản lý bật/tắt toàn bộ hiệu ứng đồ họa (3D Canvas, Aurora Glows, Keyframes, Shimmers)
 * Yêu cầu: MẶC ĐỊNH LÀ TẮT SAU KHI ĐĂNG NHẬP để tối ưu hiệu năng & trải nghiệm
 * ==========================================================================
 */
(function (global) {
  'use strict';

  // Khóa lưu trạng thái trong localStorage
  const STORAGE_KEY = 'aita_effects_enabled';

  // MẶC ĐỊNH LÀ TẮT (false): Nếu chưa từng lưu trong localStorage thì coi như là false
  let isEnabled = localStorage.getItem(STORAGE_KEY) === 'true';

  const CyberEffects = {
    /**
     * Kiểm tra hiệu ứng có đang bật không
     * @returns {boolean}
     */
    isEnabled: function () {
      return isEnabled;
    },

    /**
     * Bật / Tắt hiệu ứng
     * @returns {boolean} trạng thái mới
     */
    toggle: function () {
      isEnabled = !isEnabled;
      try {
        localStorage.setItem(STORAGE_KEY, isEnabled ? 'true' : 'false');
      } catch (e) {
        console.warn('[CyberEffects] Không thể lưu vào localStorage:', e);
      }

      this.apply();

      // Hiển thị thông báo Toast nếu có
      if (typeof window.showToast === 'function') {
        if (isEnabled) {
          window.showToast('✨ Đã bật toàn bộ hiệu ứng đồ họa', 'success');
        } else {
          window.showToast('⚡ Đã tắt toàn bộ hiệu ứng (Chế độ hiệu năng cao)', 'info');
        }
      }

      // Âm thanh phản hồi nếu CyberAudio đang bật
      if (window.CyberAudio && typeof window.CyberAudio.playTick === 'function') {
        window.CyberAudio.playTick();
      }

      return isEnabled;
    },

    /**
     * Áp dụng trạng thái lên DOM và các module đồ họa liên quan
     */
    apply: function () {
      const doc = document.documentElement;
      const body = document.body;

      if (isEnabled) {
        doc.classList.remove('effects-disabled');
        doc.classList.add('effects-enabled');
        if (body) {
          body.classList.remove('effects-disabled');
          body.classList.add('effects-enabled');
        }
      } else {
        doc.classList.remove('effects-enabled');
        doc.classList.add('effects-disabled');
        if (body) {
          body.classList.remove('effects-enabled');
          body.classList.add('effects-disabled');
        }
      }

      // Cập nhật giao diện của các nút toggle trên trang
      this.syncToggleButtons();

      // Đồng bộ tới hạt 3D AST Matrix (CyberParticles)
      if (window.CyberParticles) {
        if (isEnabled && typeof window.CyberParticles.resume === 'function') {
          window.CyberParticles.resume();
        } else if (!isEnabled && typeof window.CyberParticles.pause === 'function') {
          window.CyberParticles.pause();
        }
      }

      // Đồng bộ tới CyberShield 3D nếu có
      if (window.CyberShield3D) {
        if (isEnabled && typeof window.CyberShield3D.resume === 'function') {
          window.CyberShield3D.resume();
        } else if (!isEnabled && typeof window.CyberShield3D.pause === 'function') {
          window.CyberShield3D.pause();
        }
      }

      // Đồng bộ tới Diff Connector SVG nếu có
      if (typeof window.refreshDiffConnectors === 'function') {
        window.refreshDiffConnectors();
      }
    },

    /**
     * Đồng bộ trạng thái icon, màu sắc và chữ hiển thị trên nút Toggle
     */
    syncToggleButtons: function () {
      const buttons = document.querySelectorAll('.effects-hud-toggle');
      buttons.forEach(btn => {
        let icon = btn.querySelector('[data-lucide]') || btn.querySelector('svg');
        const statusSpan = btn.querySelector('.effects-status-val');

        if (isEnabled) {
          // Trạng thái BẬT
          btn.classList.add('bg-cyan-500/15', 'border-cyan-500/40', 'text-cyan-300');
          btn.classList.remove('bg-white/5', 'border-white/10', 'text-slate-400');
          if (icon) {
            const i = document.createElement('i');
            i.setAttribute('data-lucide', 'sparkles');
            i.className = 'w-3.5 h-3.5 text-cyan-400';
            icon.parentNode.replaceChild(i, icon);
          }
          if (statusSpan) {
            statusSpan.textContent = 'BẬT';
            statusSpan.className = 'effects-status-val font-bold text-cyan-400';
          }
          btn.setAttribute('title', 'Hiệu ứng đang BẬT. Nhấp để tắt (Chế độ hiệu năng cao)');
        } else {
          // Trạng thái TẮT (Mặc định)
          btn.classList.remove('bg-cyan-500/15', 'border-cyan-500/40', 'text-cyan-300');
          btn.classList.add('bg-white/5', 'border-white/10', 'text-slate-400');
          if (icon) {
            const i = document.createElement('i');
            i.setAttribute('data-lucide', 'zap-off');
            i.className = 'w-3.5 h-3.5 text-amber-400';
            icon.parentNode.replaceChild(i, icon);
          }
          if (statusSpan) {
            statusSpan.textContent = 'TẮT';
            statusSpan.className = 'effects-status-val font-bold text-amber-400';
          }
          btn.setAttribute('title', 'Hiệu ứng đang TẮT (mặc định). Nhấp để bật lại hiệu ứng đồ họa');
        }
      });

      if (window.lucide && typeof window.lucide.createIcons === 'function') {
        window.lucide.createIcons();
      }
    },

    /**
     * Khởi tạo hiệu ứng
     */
    init: function () {
      this.apply();
    }
  };

  global.CyberEffects = CyberEffects;

  // Tự động khởi tạo ngay khi tải xong DOM
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      CyberEffects.init();
    });
  } else {
    CyberEffects.init();
  }
})(typeof window !== 'undefined' ? window : this);
