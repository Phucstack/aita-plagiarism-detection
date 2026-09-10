/**
 * AITA CodeDefend - Scrollytelling HUD & Telemetry Controller
 * PRJ301 - Research-Based Learning (Group 4)
 * Coordinates HUD Telemetry, Timeline Range Scrubber, Auto-Fly, and Audio Integration
 */
(function (global) {
  'use strict';

  class ScrollytellingHUD {
    constructor(engine, options = {}) {
      this.engine = engine;
      this.options = Object.assign({
        telemetryFrameId: 'hud-telemetry-frame',
        telemetryDistId: 'hud-telemetry-dist',
        telemetryStageId: 'hud-telemetry-stage',
        telemetryStatusId: 'hud-telemetry-status',
        stageNameId: 'hud-stage-name',
        capsuleId: 'hud-capsule',
        progressLineId: 'scrolly-progress-line',
        scrubberId: 'timeline-scrubber',
        scrubberProgressFillId: 'timeline-progress-fill',
        autoFlyBtnId: 'auto-fly-btn',
        preloaderId: 'scrolly-preloader',
        preloaderBarId: 'preloader-bar',
        preloaderPercentId: 'preloader-percent',
        cardsClass: '.waypoint-card',
        maxDistance: 1200 // meters
      }, options);

      this.currentStage = 0;
      this.lastPlayedStage = -1;
      this.isScrubbing = false;

      this.init();
    }

    init() {
      this.cacheDom();
      this.bindEngine();
      this.bindScrubber();
      this.bindAutoFly();
      this.bindUserInterruption();

      // Safety fallback: Sau 12s nếu có sự cố mạng vẫn mở trang
      setTimeout(() => {
        if (this.engine && !this.engine.isReady) {
          this.engine.isReady = true;
          this.hidePreloader();
        }
      }, 12000);
    }

    cacheDom() {
      this.dom = {
        frame: document.getElementById(this.options.telemetryFrameId),
        dist: document.getElementById(this.options.telemetryDistId),
        stage: document.getElementById(this.options.telemetryStageId),
        status: document.getElementById(this.options.telemetryStatusId),
        stageName: document.getElementById(this.options.stageNameId),
        capsule: document.getElementById(this.options.capsuleId),
        progressLine: document.getElementById(this.options.progressLineId),
        scrubber: document.getElementById(this.options.scrubberId),
        progressFill: document.getElementById(this.options.scrubberProgressFillId),
        autoFlyBtn: document.getElementById(this.options.autoFlyBtnId),
        preloader: document.getElementById(this.options.preloaderId),
        preloaderBar: document.getElementById(this.options.preloaderBarId),
        preloaderPercent: document.getElementById(this.options.preloaderPercentId),
        cards: Array.from(document.querySelectorAll(this.options.cardsClass))
      };
    }

    bindEngine() {
      if (!this.engine) return;

      // Loading progress
      this.engine.on('progress', (data) => {
        if (this.dom.preloaderBar) {
          this.dom.preloaderBar.style.width = data.percent + '%';
        }
        if (this.dom.preloaderPercent) {
          this.dom.preloaderPercent.textContent = data.percent + '%';
        }
        if (data.percent >= 100) {
          if (this.dom.preloaderPercent) {
            this.dom.preloaderPercent.textContent = '100%';
          }
          if (this.dom.preloaderBar) {
            this.dom.preloaderBar.style.width = '100%';
          }
          // Chờ 300ms để mắt kịp nhìn thấy 100% trọn vẹn trước khi fade-out
          setTimeout(() => {
            this.hidePreloader();
          }, 300);
        }
      });

      // Frame & Scroll progress updates
      this.engine.on('frameChange', (data) => {
        this.updateTelemetry(data);
        this.updateScrubberUI(data.progress);
        this.updateWaypointCards(data.progress);
      });
    }

    hidePreloader() {
      if (this.dom.preloader && !this.dom.preloader.classList.contains('hidden-preloader')) {
        this.dom.preloader.classList.add('opacity-0', 'pointer-events-none');
        setTimeout(() => {
          this.dom.preloader.classList.add('hidden-preloader');
          this.dom.preloader.style.display = 'none';
        }, 500);
      }
    }

    updateTelemetry(data) {
      const frameStr = String(data.frame).padStart(3, '0');
      const totalStr = String(data.total).padStart(3, '0');
      if (this.dom.frame) {
        this.dom.frame.textContent = `${frameStr} / ${totalStr}`;
      }

      const distVal = Math.round(data.progress * this.options.maxDistance);
      if (this.dom.dist) {
        this.dom.dist.textContent = `${distVal}m`;
      }

      // Determine stage
      const p = data.progress;
      let stageNum = 1;
      let stageName = 'Artifact Ingestion & SHA-256';
      let statusText = 'TIẾP NHẬN BÀI NỘP';
      let statusColor = 'text-cyan-400';

      let stageShort = '01 • Tiếp Nhận & SHA-256';

      if (p < 0.28) {
        stageNum = 1;
        stageName = 'Chặng 01: Ingestion & Băm SHA-256';
        stageShort = '01 • Tiếp Nhận & SHA-256';
        statusText = 'KIỂM TOÀN VẸN MÃ BĂM';
        statusColor = 'text-cyan-400';
      } else if (p < 0.58) {
        stageNum = 2;
        stageName = 'Chặng 02: Chuẩn Hóa Cây Cú Pháp AST';
        stageShort = '02 • Bóc Tách AST';
        statusText = 'BÓC TÁCH KHỬ NGỤY TRANG';
        statusColor = 'text-violet-400';
      } else if (p < 0.84) {
        stageNum = 3;
        stageName = 'Chặng 03: Thẩm Định Ngữ Nghĩa Gemini AI';
        stageShort = '03 • Lõi Gemini AI';
        statusText = 'QUÉT NƠ-RON NGỮ NGHĨA';
        statusColor = 'text-amber-400';
      } else {
        stageNum = 4;
        stageName = 'Chặng 04: Phán Quyết Liêm Chính Học Thuật';
        stageShort = '04 • Phán Quyết Liêm Chính';
        statusText = 'TRUNG TÂM PHÂN TÍCH';
        statusColor = 'text-emerald-400';
      }

      if (this.dom.stage) {
        this.dom.stage.textContent = `GIAI ĐOẠN 0${stageNum} / 04`;
      }
      if (this.dom.stageName) {
        this.dom.stageName.textContent = stageShort;
      }
      if (this.dom.progressLine) {
        this.dom.progressLine.style.width = (p * 100).toFixed(1) + '%';
      }
      if (this.dom.status) {
        this.dom.status.textContent = statusText;
        this.dom.status.className = `font-mono text-xs tracking-wider ${statusColor}`;
      }

      // Tự động ẩn Floating Capsule khi cuộn xuống dưới xem Kiến Trúc Kỹ Thuật (Bento Grid)
      if (this.dom.capsule) {
        if (p >= 0.99) {
          this.dom.capsule.classList.add('opacity-0', 'pointer-events-none', 'translate-y-4');
        } else {
          this.dom.capsule.classList.remove('opacity-0', 'pointer-events-none', 'translate-y-4');
        }
      }

      // Audio cues on stage transition
      if (stageNum !== this.currentStage) {
        this.currentStage = stageNum;
        if (this.currentStage !== this.lastPlayedStage) {
          this.lastPlayedStage = this.currentStage;
          if (window.CyberAudio && window.CyberAudio.isEnabled()) {
            if (stageNum === 1) window.CyberAudio.playTick();
            else if (stageNum === 2 || stageNum === 3) window.CyberAudio.playLaser();
            else if (stageNum === 4) window.CyberAudio.playSuccess();
          }
        }
      }
    }

    updateScrubberUI(progress) {
      if (this.isScrubbing) return;
      const pct = Math.round(progress * 100);
      if (this.dom.scrubber) {
        this.dom.scrubber.value = pct;
      }
      if (this.dom.progressFill) {
        this.dom.progressFill.style.width = pct + '%';
      }
    }

    updateWaypointCards(progress) {
      // 4 Milestone ranges:
      // Card 0: 0.08 -> 0.28
      // Card 1: 0.33 -> 0.58
      // Card 2: 0.63 -> 0.84
      // Card 3: 0.88 -> 1.0
      const ranges = [
        { min: 0.06, max: 0.28 },
        { min: 0.32, max: 0.58 },
        { min: 0.62, max: 0.84 },
        { min: 0.87, max: 1.00 }
      ];

      this.dom.cards.forEach((card, idx) => {
        const range = ranges[idx];
        if (!range) return;
        const isActive = progress >= range.min && progress <= range.max;
        if (isActive) {
          if (!card.classList.contains('card-active')) {
            card.classList.add('card-active');
            card.classList.remove('card-inactive');
          }
        } else {
          if (card.classList.contains('card-active')) {
            card.classList.remove('card-active');
            card.classList.add('card-inactive');
          }
        }
      });
    }

    bindScrubber() {
      if (!this.dom.scrubber) return;

      const handleInput = (e) => {
        this.isScrubbing = true;
        this.engine.stopAutoFly();
        this.updateAutoFlyButtonUI(false);

        const val = parseFloat(e.target.value);
        const progress = val / 100;
        this.engine.setScrollProgress(progress, true);

        if (this.dom.progressFill) {
          this.dom.progressFill.style.width = val + '%';
        }

        if (window.CyberAudio && window.CyberAudio.isEnabled()) {
          window.CyberAudio.playTick();
        }
      };

      const handleRelease = () => {
        this.isScrubbing = false;
      };

      this.dom.scrubber.addEventListener('input', handleInput);
      this.dom.scrubber.addEventListener('change', handleRelease);
      this.dom.scrubber.addEventListener('mouseup', handleRelease);
      this.dom.scrubber.addEventListener('touchend', handleRelease);
    }

    bindAutoFly() {
      if (!this.dom.autoFlyBtn) return;

      this.dom.autoFlyBtn.addEventListener('click', () => {
        const isFlying = this.engine.toggleAutoFly();
        this.updateAutoFlyButtonUI(isFlying);
        if (window.CyberAudio && window.CyberAudio.isEnabled()) {
          isFlying ? window.CyberAudio.playLaser() : window.CyberAudio.playTick();
        }
      });
    }

    updateAutoFlyButtonUI(isFlying) {
      if (!this.dom.autoFlyBtn) return;
      const text = this.dom.autoFlyBtn.querySelector('.auto-fly-text');
      const icon = this.dom.autoFlyBtn.querySelector('[data-lucide]');

      if (isFlying) {
        this.dom.autoFlyBtn.classList.add('bg-cyan-500/20', 'border-cyan-400', 'text-cyan-300', 'shadow-[0_0_15px_rgba(6,182,212,0.4)]');
        if (text) text.textContent = 'Đang Tự Động Bay...';
        if (icon) icon.setAttribute('data-lucide', 'pause');
      } else {
        this.dom.autoFlyBtn.classList.remove('bg-cyan-500/20', 'border-cyan-400', 'text-cyan-300', 'shadow-[0_0_15px_rgba(6,182,212,0.4)]');
        if (text) text.textContent = 'Tự Động Bay (Auto-Fly)';
        if (icon) icon.setAttribute('data-lucide', 'play');
      }
      if (window.lucide) lucide.createIcons();
    }

    bindUserInterruption() {
      // If user scrolls manually while flying, pause auto-fly
      const interrupt = () => {
        if (this.engine.isAutoFlying && !this.isScrubbing) {
          this.engine.stopAutoFly();
          this.updateAutoFlyButtonUI(false);
        }
      };

      window.addEventListener('wheel', interrupt, { passive: true });
      window.addEventListener('touchmove', interrupt, { passive: true });
      window.addEventListener('keydown', (e) => {
        if (['ArrowUp', 'ArrowDown', 'PageUp', 'PageDown', 'Home', 'End', ' '].includes(e.key)) {
          interrupt();
        }
      });
    }
  }

  global.ScrollytellingHUD = ScrollytellingHUD;
})(window);
