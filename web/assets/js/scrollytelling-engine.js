/**
 * AITA CodeDefend - 3D Camera Scrollytelling Engine
 * PRJ301 - Research-Based Learning (Group 4)
 * High-performance Canvas Image-Sequence Scrollytelling with LERP Smoothing & Progressive Loading
 */
(function (global) {
  'use strict';

  const DEFAULT_OPTIONS = {
    canvasId: 'scrolly-canvas',
    containerId: 'scrolly-container',
    totalFrames: 240,
    framePathPattern: 'assets/frames/frame_{index}.webp',
    lerpFactor: 0.18,
    dprCap: 1.0,
    onProgress: null,
    onReady: null,
    onFrameChange: null
  };

  class ScrollytellingEngine {
    constructor(options = {}) {
      this.opts = Object.assign({}, DEFAULT_OPTIONS, options);
      this.canvas = document.getElementById(this.opts.canvasId);
      this.container = document.getElementById(this.opts.containerId);
      this.ctx = this.canvas ? this.canvas.getContext('2d', { alpha: false }) : null;

      this.totalFrames = this.opts.totalFrames;
      this.frames = new Array(this.totalFrames);
      this.loadedCount = 0;
      this.isReady = false;

      this.currentFrame = 0; // Float for LERP
      this.targetFrame = 0;
      this.lastRenderedFrame = -1;
      this.scrollProgress = 0;

      this.cachedContainerTop = 0;
      this.cachedScrollDist = 0;
      this.renderRect = null;

      this.isAutoFlying = false;
      this.autoFlySpeed = 0.0006; // Tốc độ bay tự động rất êm dịu (~25s toàn hành trình)
      this.autoFlyDirection = 1;

      this.rafId = null;
      this.listeners = {
        frameChange: [],
        progress: []
      };

      if (!this.canvas || !this.container) {
        console.warn('[ScrollytellingEngine] Canvas or Container not found. Waiting for DOM.');
        return;
      }

      this.init();
    }

    init() {
      this.handleResize = this.resize.bind(this);
      window.addEventListener('resize', this.handleResize, { passive: true });
      window.addEventListener('orientationchange', this.handleResize, { passive: true });

      this.resize();
      this.preloadFrames();
      this.bindScroll();
      this.startLoop();
    }

    formatIndex(idx) {
      // 1-based index with 3 zero-pads: 001, 002, ..., 150
      const n = idx + 1;
      if (n < 10) return '00' + n;
      if (n < 100) return '0' + n;
      return '' + n;
    }

    getFrameUrl(idx) {
      const pad = this.formatIndex(idx);
      return this.opts.framePathPattern.replace('{index}', pad);
    }

    preloadFrames() {
      // Priority pass: First load key anchors (frame 0, 15, 30, ... 149)
      const priorityIndices = [];
      for (let i = 0; i < this.totalFrames; i += 10) {
        priorityIndices.push(i);
      }
      if (!priorityIndices.includes(this.totalFrames - 1)) {
        priorityIndices.push(this.totalFrames - 1);
      }

      // Load all frames
      let loaded = 0;
      const total = this.totalFrames;

      const onLoadOne = (idx) => {
        loaded++;
        this.loadedCount = loaded;
        const percent = Math.min(100, Math.round((loaded / total) * 100));

        if (this.opts.onProgress) {
          this.opts.onProgress(loaded, total, percent);
        }
        this.emit('progress', { loaded, total, percent });

        // Trigger initial render as soon as frame 0 is ready
        if (idx === 0 && !this.isReady) {
          this.draw(0);
        }

        // Chỉ kích hoạt sẵn sàng khi ĐÃ NẠP ĐỦ 100% TẤT CẢ KHUNG HÌNH
        if (loaded >= total && !this.isReady) {
          this.isReady = true;
          if (this.opts.onReady) this.opts.onReady();
        }
      };

      const loadImg = async (i) => {
        const img = new Image();
        img.src = this.getFrameUrl(i);
        this.frames[i] = img;

        try {
          if (typeof img.decode === 'function') {
            await img.decode();
          } else {
            await new Promise((resolve) => {
              img.onload = resolve;
              img.onerror = resolve;
            });
          }
        } catch (err) {
          // Bỏ qua lỗi decode nếu trình duyệt fallback
        }
        onLoadOne(i);
      };

      for (let i = 0; i < total; i++) {
        loadImg(i);
      }
    }

    resize() {
      if (!this.canvas || !this.ctx) return;
      const dpr = Math.min(window.devicePixelRatio || 1, this.opts.dprCap);
      const width = window.innerWidth;
      const height = window.innerHeight;

      this.canvas.width = Math.round(width * dpr);
      this.canvas.height = Math.round(height * dpr);
      this.canvas.style.width = width + 'px';
      this.canvas.style.height = height + 'px';

      this.ctx.imageSmoothingEnabled = true;
      this.ctx.imageSmoothingQuality = 'medium';

      // Precompute render coordinates (video là 1280x720)
      const cw = this.canvas.width;
      const ch = this.canvas.height;
      const iRatio = 1280 / 720;
      const cRatio = cw / ch;

      if (cRatio > iRatio) {
        const dw = cw;
        const dh = cw / iRatio;
        this.renderRect = { dw, dh, dx: 0, dy: (ch - dh) / 2 };
      } else {
        const dh = ch;
        const dw = ch * iRatio;
        this.renderRect = { dw, dh, dx: (cw - dw) / 2, dy: 0 };
      }

      this.updateCachedMetrics();
      this.lastRenderedFrame = -1;
      this.draw(this.currentFrame);
    }

    updateCachedMetrics() {
      if (!this.container) return;
      const rect = this.container.getBoundingClientRect();
      this.cachedContainerTop = window.pageYOffset + rect.top;
      this.cachedScrollDist = this.container.offsetHeight - window.innerHeight;
    }

    bindScroll() {
      const onScroll = () => {
        if (this.isAutoFlying) return;
        this.updateScrollTarget();
      };
      window.addEventListener('scroll', onScroll, { passive: true });
      this.updateScrollTarget();
    }

    updateScrollTarget() {
      if (!this.cachedScrollDist || this.cachedScrollDist <= 0) {
        this.updateCachedMetrics();
        if (!this.cachedScrollDist || this.cachedScrollDist <= 0) return;
      }

      const scrolled = window.pageYOffset - this.cachedContainerTop;
      let progress = scrolled / this.cachedScrollDist;
      progress = Math.max(0, Math.min(1, progress));
      this.scrollProgress = progress;
      this.targetFrame = progress * (this.totalFrames - 1);
    }

    setScrollProgress(progress, syncScroll = true) {
      progress = Math.max(0, Math.min(1, progress));
      this.scrollProgress = progress;
      this.targetFrame = progress * (this.totalFrames - 1);

      if (syncScroll && this.container) {
        if (!this.cachedScrollDist || this.cachedScrollDist <= 0) {
          this.updateCachedMetrics();
        }
        const targetScrollY = this.cachedContainerTop + progress * this.cachedScrollDist;
        window.scrollTo({ top: targetScrollY, behavior: 'instant' });
      }
    }

    getNearestLoadedFrame(targetIdx) {
      const idx = Math.max(0, Math.min(this.totalFrames - 1, Math.round(targetIdx)));
      if (this.frames[idx] && this.frames[idx].complete && this.frames[idx].naturalWidth > 0) {
        return this.frames[idx];
      }
      for (let offset = 1; offset < 20; offset++) {
        const prev = idx - offset;
        if (prev >= 0 && this.frames[prev] && this.frames[prev].complete && this.frames[prev].naturalWidth > 0) {
          return this.frames[prev];
        }
        const next = idx + offset;
        if (next < this.totalFrames && this.frames[next] && this.frames[next].complete && this.frames[next].naturalWidth > 0) {
          return this.frames[next];
        }
      }
      return this.frames[0];
    }

    draw(frameIndex) {
      if (!this.ctx || !this.canvas || !this.renderRect) return;
      const img = this.getNearestLoadedFrame(frameIndex);
      if (!img || !img.complete || img.naturalWidth === 0) return;

      this.ctx.drawImage(img, this.renderRect.dx, this.renderRect.dy, this.renderRect.dw, this.renderRect.dh);
    }

    startLoop() {
      const loop = () => {
        if (this.isAutoFlying) {
          let nextProgress = this.scrollProgress + this.autoFlySpeed * this.autoFlyDirection;
          if (nextProgress >= 1.0) {
            nextProgress = 1.0;
            this.autoFlyDirection = -1; // Reverse or stop
          } else if (nextProgress <= 0.0) {
            nextProgress = 0.0;
            this.autoFlyDirection = 1;
          }
          this.setScrollProgress(nextProgress, true);
        }

        // LERP interpolation
        const diff = this.targetFrame - this.currentFrame;
        if (Math.abs(diff) > 0.001) {
          this.currentFrame += diff * this.opts.lerpFactor;
        } else {
          this.currentFrame = this.targetFrame;
        }

        const intFrame = Math.round(this.currentFrame);
        if (intFrame !== this.lastRenderedFrame) {
          this.draw(this.currentFrame);
          this.lastRenderedFrame = intFrame;

          const payload = {
            frame: intFrame + 1,
            total: this.totalFrames,
            progress: this.scrollProgress,
            exactFrame: this.currentFrame
          };
          if (this.opts.onFrameChange) this.opts.onFrameChange(payload);
          this.emit('frameChange', payload);
        }

        this.rafId = requestAnimationFrame(loop);
      };
      this.rafId = requestAnimationFrame(loop);
    }

    toggleAutoFly() {
      this.isAutoFlying = !this.isAutoFlying;
      return this.isAutoFlying;
    }

    stopAutoFly() {
      this.isAutoFlying = false;
    }

    on(event, cb) {
      if (!this.listeners[event]) this.listeners[event] = [];
      this.listeners[event].push(cb);
    }

    emit(event, data) {
      if (this.listeners[event]) {
        this.listeners[event].forEach(cb => {
          try { cb(data); } catch (e) { console.error(e); }
        });
      }
    }

    destroy() {
      if (this.rafId) cancelAnimationFrame(this.rafId);
      window.removeEventListener('resize', this.handleResize);
      this.listeners = {};
    }
  }

  global.ScrollytellingEngine = ScrollytellingEngine;
})(window);
