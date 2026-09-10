/**
 * AITA Cybernetic Web Audio Soundscape Engine
 * 100% Procedural Oscillator Synthesis - Zero External Media Dependencies
 * Designed for PRJ301 Evaluation & Defense Demo
 */
(function (global) {
  let ctx = null;
  let isEnabled = localStorage.getItem('aita_cyber_audio') === 'true';

  function initContext() {
    if (!ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) ctx = new AudioCtx();
    }
    if (ctx && ctx.state === 'suspended') {
      ctx.resume();
    }
  }

  const CyberAudio = {
    isEnabled: () => isEnabled,

    toggle: function () {
      isEnabled = !isEnabled;
      localStorage.setItem('aita_cyber_audio', isEnabled);
      if (isEnabled) {
        initContext();
        this.playSuccess();
      }
      this.syncToggleButtons();
      if (window.showToast) {
        window.showToast(isEnabled ? '🔊 Đã bật âm thanh Cyber HUD' : '🔇 Đã tắt âm thanh HUD', 'info');
      }
      return isEnabled;
    },

    syncToggleButtons: function () {
      document.querySelectorAll('.audio-hud-toggle').forEach(btn => {
        if (isEnabled) {
          btn.classList.add('active');
          const icon = btn.querySelector('[data-lucide]');
          if (icon) icon.setAttribute('data-lucide', 'volume-2');
        } else {
          btn.classList.remove('active');
          const icon = btn.querySelector('[data-lucide]');
          if (icon) icon.setAttribute('data-lucide', 'volume-x');
        }
      });
      if (window.lucide) lucide.createIcons();
    },

    // 1. Click Haptic Tick (880Hz -> 1320Hz, 35ms)
    playTick: function () {
      if (!isEnabled) return;
      initContext();
      if (!ctx) return;
      try {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(880, ctx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(1400, ctx.currentTime + 0.035);

        gain.gain.setValueAtTime(0.04, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.035);

        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start();
        osc.stop(ctx.currentTime + 0.04);
      } catch (e) {}
    },

    // 2. Laser Sonar Sweep (1700Hz -> 350Hz, 120ms)
    playLaser: function () {
      if (!isEnabled) return;
      initContext();
      if (!ctx) return;
      try {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(1700, ctx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(320, ctx.currentTime + 0.11);

        gain.gain.setValueAtTime(0.05, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.11);

        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start();
        osc.stop(ctx.currentTime + 0.12);
      } catch (e) {}
    },

    // 3. Success Harmonic Chord (C5 - E5 - G5, 240ms)
    playSuccess: function () {
      if (!isEnabled) return;
      initContext();
      if (!ctx) return;
      try {
        [523.25, 659.25, 783.99].forEach((freq, idx) => {
          const osc = ctx.createOscillator();
          const gain = ctx.createGain();
          osc.type = 'sine';
          osc.frequency.setValueAtTime(freq, ctx.currentTime + idx * 0.05);

          gain.gain.setValueAtTime(0.04, ctx.currentTime + idx * 0.05);
          gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + idx * 0.05 + 0.22);

          osc.connect(gain);
          gain.connect(ctx.destination);
          osc.start(ctx.currentTime + idx * 0.05);
          osc.stop(ctx.currentTime + idx * 0.05 + 0.23);
        });
      } catch (e) {}
    },

    // 4. Plagiarism Danger Chord (Low Minor Triad 220Hz + 261Hz, 300ms)
    playDanger: function () {
      if (!isEnabled) return;
      initContext();
      if (!ctx) return;
      try {
        [220, 261.63].forEach((freq) => {
          const osc = ctx.createOscillator();
          const gain = ctx.createGain();
          osc.type = 'sawtooth';
          osc.frequency.setValueAtTime(freq, ctx.currentTime);

          gain.gain.setValueAtTime(0.05, ctx.currentTime);
          gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.28);

          osc.connect(gain);
          gain.connect(ctx.destination);
          osc.start();
          osc.stop(ctx.currentTime + 0.3);
        });
      } catch (e) {}
    }
  };

  // Auto bind to interactive elements with [data-sound]
  document.addEventListener('DOMContentLoaded', () => {
    CyberAudio.syncToggleButtons();

    document.addEventListener('click', (e) => {
      const target = e.target.closest('[data-sound]');
      if (!target) return;
      const soundType = target.getAttribute('data-sound');
      if (soundType === 'tick') CyberAudio.playTick();
      else if (soundType === 'laser') CyberAudio.playLaser();
      else if (soundType === 'success') CyberAudio.playSuccess();
      else if (soundType === 'danger') CyberAudio.playDanger();
    });

    // Attach click sound to role buttons & nav tabs by default
    document.querySelectorAll('.role-tab-btn, .sidebar-nav-item, nav a, button.btn-shimmer').forEach(el => {
      el.addEventListener('click', () => CyberAudio.playTick());
    });
  });

  global.CyberAudio = CyberAudio;
})(window);
