/**
 * AITA CodeDefend - Ambient Cyber VFX Engine (v2.3 Masterclass)
 * Đỉnh cao hiệu ứng chuyển động không gian mạng tại chỗ (In-Place Alive Matrix):
 * - Camera Drone Floating: Khung cảnh thở & lơ lửng 3D nhịp nhàng tại chỗ
 * - Precision PCB Circuit Surges: Hàng chục luồng xung năng lượng chạy dọc các rãnh vi mạch sàn & tường
 * - Volumetric Holographic Laser Scanner: Dải quét laser an ninh lướt lên xuống toàn không gian
 * - Quantum Code Stream Decode Rays: Các tia giải mã quét qua hai vách mã nguồn
 * - Core Shield Shockwaves & Breathing Glow: Chiếc khiên số trung tâm tỏa sóng xung kích và hào quang
 * - Floating Photons: Hạt bụi lượng tử ánh sáng bay bồng bềnh
 * - Mouse Parallax Response: Phản hồi nhạy bén theo tọa độ con trỏ chuột
 */
(function (global) {
  'use strict';

  class ScrollyVFXAmbient {
    constructor(canvasId = 'scrolly-vfx-canvas', engine = null) {
      this.canvas = document.getElementById(canvasId);
      this.engine = engine;
      this.ctx = this.canvas ? this.canvas.getContext('2d') : null;
      this.scrollyCanvas = document.getElementById('scrolly-canvas');

      this.width = 0;
      this.height = 0;
      this.dpr = Math.min(window.devicePixelRatio || 1, 1.5);

      // Điểm tụ phối cảnh trung tâm (Center Vanishing Point & Core Shield)
      this.vp = { x: 0.50, y: 0.532 };
      this.coreShield = { x: 0.50, y: 0.408 };

      // Tuyến đường vi mạch chuẩn xác theo phối cảnh ảnh 3D
      this.tracks = [
        // Sàn nhà - Trục giữa thẳng tắp
        [{ x: 0.50, y: 1.0 }, { x: 0.50, y: 0.535 }],
        // Sàn nhà - Mạch trong trái (Chạy song song trục giữa)
        [{ x: 0.45, y: 1.0 }, { x: 0.46, y: 0.78 }, { x: 0.485, y: 0.65 }, { x: 0.496, y: 0.535 }],
        // Sàn nhà - Mạch trong phải
        [{ x: 0.55, y: 1.0 }, { x: 0.54, y: 0.78 }, { x: 0.515, y: 0.65 }, { x: 0.504, y: 0.535 }],
        // Sàn nhà - Mạch giữa trái (Gấp khúc ziczac PCB đặc trưng)
        [{ x: 0.32, y: 1.0 }, { x: 0.36, y: 0.88 }, { x: 0.30, y: 0.82 }, { x: 0.38, y: 0.70 }, { x: 0.47, y: 0.60 }, { x: 0.493, y: 0.535 }],
        // Sàn nhà - Mạch giữa phải (Gấp khúc ziczac đối xứng)
        [{ x: 0.68, y: 1.0 }, { x: 0.64, y: 0.88 }, { x: 0.70, y: 0.82 }, { x: 0.62, y: 0.70 }, { x: 0.53, y: 0.60 }, { x: 0.507, y: 0.535 }],
        // Sàn nhà - Mạch ngoài trái
        [{ x: 0.15, y: 1.0 }, { x: 0.22, y: 0.90 }, { x: 0.20, y: 0.78 }, { x: 0.35, y: 0.66 }, { x: 0.46, y: 0.58 }, { x: 0.49, y: 0.535 }],
        // Sàn nhà - Mạch ngoài phải
        [{ x: 0.85, y: 1.0 }, { x: 0.78, y: 0.90 }, { x: 0.80, y: 0.78 }, { x: 0.65, y: 0.66 }, { x: 0.54, y: 0.58 }, { x: 0.51, y: 0.535 }],
        // Vách tường trái - Thanh ray neon ngang 1
        [{ x: 0.01, y: 0.38 }, { x: 0.18, y: 0.43 }, { x: 0.38, y: 0.49 }, { x: 0.45, y: 0.52 }],
        // Vách tường trái - Thanh ray neon ngang 2
        [{ x: 0.01, y: 0.68 }, { x: 0.16, y: 0.62 }, { x: 0.35, y: 0.56 }, { x: 0.46, y: 0.53 }],
        // Vách tường phải - Thanh ray neon ngang 1
        [{ x: 0.99, y: 0.38 }, { x: 0.82, y: 0.43 }, { x: 0.62, y: 0.49 }, { x: 0.55, y: 0.52 }],
        // Vách tường phải - Thanh ray neon ngang 2
        [{ x: 0.99, y: 0.68 }, { x: 0.84, y: 0.62 }, { x: 0.65, y: 0.56 }, { x: 0.54, y: 0.53 }],
        // Cổng vòm trần - Trái
        [{ x: 0.30, y: 0.0 }, { x: 0.40, y: 0.22 }, { x: 0.47, y: 0.44 }, { x: 0.495, y: 0.51 }],
        // Cổng vòm trần - Phải
        [{ x: 0.70, y: 0.0 }, { x: 0.60, y: 0.22 }, { x: 0.53, y: 0.44 }, { x: 0.505, y: 0.51 }]
      ];

      // Xung photon ánh sáng
      this.pulses = [];
      this.maxPulses = 42;

      // Dải quét laser an ninh holographic (quét lên xuống)
      this.scanner = {
        y: 0.25,
        direction: 1,
        speed: 0.0032,
        minY: 0.12,
        maxY: 0.88,
        pulseGlow: 0
      };

      // Vòng sóng xung kích từ khiên
      this.shockwaves = [];
      this.lastShockwaveTime = 0;

      // Hạt photon lượng tử
      this.particles = [];
      this.maxParticles = 50;

      // Tia quét giải mã mã nguồn 2 bên
      this.codeRays = [];
      this.initCodeRays();

      // Mouse Parallax & Gimbal Floating
      this.mouse = { x: 0.5, y: 0.5, targetX: 0.5, targetY: 0.5 };
      this.cameraFloat = { x: 0, y: 0, scale: 1.0 };

      this.rafId = null;
      this.time = 0;
      this.isRunning = false;

      if (!this.canvas) return;
      this.init();
    }

    init() {
      this.handleResize = this.resize.bind(this);
      window.addEventListener('resize', this.handleResize, { passive: true });

      window.addEventListener('mousemove', (e) => {
        this.mouse.targetX = e.clientX / window.innerWidth;
        this.mouse.targetY = e.clientY / window.innerHeight;
      }, { passive: true });

      this.resize();
      this.initParticles();
      this.initPulses();
      this.start();
    }

    resize() {
      if (!this.canvas) return;
      this.width = window.innerWidth;
      this.height = window.innerHeight;
      this.dpr = Math.min(window.devicePixelRatio || 1, 1.5);

      this.canvas.width = Math.round(this.width * this.dpr);
      this.canvas.height = Math.round(this.height * this.dpr);
      this.canvas.style.width = this.width + 'px';
      this.canvas.style.height = this.height + 'px';

      if (this.ctx) {
        this.ctx.scale(this.dpr, this.dpr);
      }
    }

    initParticles() {
      this.particles = [];
      for (let i = 0; i < this.maxParticles; i++) {
        this.particles.push({
          x: Math.random(),
          y: Math.random(),
          z: Math.random() * 0.8 + 0.2,
          vx: (Math.random() - 0.5) * 0.0005,
          vy: (Math.random() - 0.5) * 0.0007,
          radius: Math.random() * 1.8 + 0.5,
          color: Math.random() > 0.4 ? 'rgba(0, 240, 255, ' : (Math.random() > 0.5 ? 'rgba(192, 132, 252, ' : 'rgba(255, 255, 255, '),
          baseAlpha: Math.random() * 0.55 + 0.15,
          flickerSpeed: Math.random() * 3.5 + 1.2
        });
      }
    }

    initPulses() {
      this.pulses = [];
      for (let i = 0; i < this.maxPulses; i++) {
        this.spawnPulse(Math.random());
      }
    }

    initCodeRays() {
      this.codeRays = [
        { side: 'left', y: 0.35, speed: 0.002, length: 0.25, alpha: 0.4 },
        { side: 'left', y: 0.52, speed: 0.0035, length: 0.30, alpha: 0.5 },
        { side: 'right', y: 0.42, speed: 0.0025, length: 0.28, alpha: 0.45 },
        { side: 'right', y: 0.65, speed: 0.003, length: 0.22, alpha: 0.5 }
      ];
    }

    spawnPulse(initialProgress = 0) {
      const trackIdx = Math.floor(Math.random() * this.tracks.length);
      const isCyan = Math.random() > 0.32;
      this.pulses.push({
        trackIdx: trackIdx,
        t: initialProgress,
        speed: (Math.random() * 0.009 + 0.006),
        tailLength: Math.random() * 0.14 + 0.07,
        width: Math.random() * 2.4 + 1.2,
        color: isCyan ? '#00f0ff' : '#d946ef',
        glowColor: isCyan ? 'rgba(0, 240, 255, 0.9)' : 'rgba(217, 70, 239, 0.9)'
      });
    }

    interpolateTrack(track, t) {
      if (!track || track.length < 2) return { x: 0, y: 0 };
      if (t <= 0) return track[0];
      if (t >= 1) return track[track.length - 1];

      const segCount = track.length - 1;
      const totalT = t * segCount;
      const segIndex = Math.min(Math.floor(totalT), segCount - 1);
      const localT = totalT - segIndex;

      const p0 = track[segIndex];
      const p1 = track[segIndex + 1];

      return {
        x: p0.x + (p1.x - p0.x) * localT,
        y: p0.y + (p1.y - p0.y) * localT
      };
    }

    start() {
      if (this.isRunning) return;
      this.isRunning = true;

      const loop = (timestamp) => {
        this.time = timestamp * 0.001;
        this.update();
        this.render();
        this.rafId = requestAnimationFrame(loop);
      };
      this.rafId = requestAnimationFrame(loop);
    }

    stop() {
      this.isRunning = false;
      if (this.rafId) cancelAnimationFrame(this.rafId);
    }

    update() {
      // 1. Mouse Parallax LERP
      this.mouse.x += (this.mouse.targetX - this.mouse.x) * 0.05;
      this.mouse.y += (this.mouse.targetY - this.mouse.y) * 0.05;

      let scrollProg = 0;
      if (this.engine && typeof this.engine.scrollProgress === 'number') {
        scrollProg = this.engine.scrollProgress;
      }

      // 2. Camera Float & Breathing Motion (Áp dụng chuyển động tại chỗ cho toàn bộ không gian)
      if (scrollProg < 0.05) {
        // Drone breathing float nhịp nhàng
        const floatY = Math.sin(this.time * 1.4) * 4.5; // Dao động 4.5px lên xuống
        const floatX = Math.cos(this.time * 0.9) * 3.0; // Dao động 3.0px qua lại
        const floatScale = 1.0 + Math.sin(this.time * 0.8) * 0.008; // Phóng to/thu nhỏ 0.8%

        this.cameraFloat = { x: floatX, y: floatY, scale: floatScale };

        // Đồng bộ hiệu ứng lơ lửng lên cả canvas video chính
        if (this.scrollyCanvas) {
          const mx = (this.mouse.x - 0.5) * 8;
          const my = (this.mouse.y - 0.5) * 5;
          this.scrollyCanvas.style.transform = `translate3d(${floatX + mx}px, ${floatY + my}px, 0) scale(${floatScale})`;
        }
      } else {
        if (this.scrollyCanvas && this.scrollyCanvas.style.transform) {
          this.scrollyCanvas.style.transform = 'none';
        }
      }

      // 3. Tăng tốc độ vi mạch khi người dùng cuộn
      const speedMultiplier = 1 + scrollProg * 3.5;

      // 4. Cập nhật các xung vi mạch
      for (let i = this.pulses.length - 1; i >= 0; i--) {
        const p = this.pulses[i];
        p.t += p.speed * speedMultiplier;

        if (p.t >= 1.0) {
          if (Math.random() > 0.55) {
            this.spawnShockwave();
          }
          this.pulses.splice(i, 1);
          this.spawnPulse(0);
        }
      }

      // 5. Cập nhật Dải quét Laser an ninh lên xuống
      this.scanner.y += this.scanner.speed * this.scanner.direction;
      if (this.scanner.y >= this.scanner.maxY) {
        this.scanner.y = this.scanner.maxY;
        this.scanner.direction = -1;
      } else if (this.scanner.y <= this.scanner.minY) {
        this.scanner.y = this.scanner.minY;
        this.scanner.direction = 1;
      }

      // 6. Cập nhật Sóng xung kích
      for (let i = this.shockwaves.length - 1; i >= 0; i--) {
        const sw = this.shockwaves[i];
        sw.radius += sw.growSpeed;
        sw.alpha -= sw.fadeSpeed;
        if (sw.alpha <= 0 || sw.radius > sw.maxRadius) {
          this.shockwaves.splice(i, 1);
        }
      }

      if (this.time - this.lastShockwaveTime > 2.2) {
        this.spawnShockwave();
        this.lastShockwaveTime = this.time;
      }

      // 7. Cập nhật Hạt bụi lượng tử
      for (let i = 0; i < this.particles.length; i++) {
        const pt = this.particles[i];
        pt.x += pt.vx;
        pt.y += pt.vy;
        if (pt.x < 0) pt.x = 1;
        if (pt.x > 1) pt.x = 0;
        if (pt.y < 0) pt.y = 1;
        if (pt.y > 1) pt.y = 0;
      }

      // 8. Cập nhật Tia quét mã nguồn hai bên
      for (let i = 0; i < this.codeRays.length; i++) {
        const ray = this.codeRays[i];
        ray.y += ray.speed;
        if (ray.y > 0.8) ray.y = 0.25;
      }
    }

    spawnShockwave() {
      if (this.shockwaves.length > 5) return;
      this.shockwaves.push({
        x: this.coreShield.x,
        y: this.coreShield.y,
        radius: 6,
        maxRadius: Math.min(this.width, this.height) * 0.22,
        growSpeed: 2.2,
        alpha: 0.8,
        fadeSpeed: 0.018,
        color: Math.random() > 0.4 ? 'rgba(0, 240, 255, ' : 'rgba(217, 70, 239, '
      });
    }

    render() {
      if (!this.ctx || !this.width || !this.height) return;
      const ctx = this.ctx;
      const w = this.width;
      const h = this.height;

      ctx.clearRect(0, 0, w, h);

      const mx = (this.mouse.x - 0.5) * 10;
      const my = (this.mouse.y - 0.5) * 6;
      const totalX = mx + this.cameraFloat.x;
      const totalY = my + this.cameraFloat.y;

      ctx.save();
      ctx.translate(totalX, totalY);
      ctx.scale(this.cameraFloat.scale, this.cameraFloat.scale);

      ctx.globalCompositeOperation = 'lighter';

      // =========================================================================
      // 1. QUẦNG THỞ NĂNG LƯỢNG KHIÊN TRUNG TÂM & CỘT SÁNG TRỤC DỌC
      // =========================================================================
      const coreX = this.coreShield.x * w;
      const coreY = this.coreShield.y * h;
      const breath = 0.35 + Math.sin(this.time * 2.2) * 0.20;

      const coreGlow = ctx.createRadialGradient(coreX, coreY, 2, coreX, coreY, 80);
      coreGlow.addColorStop(0, `rgba(255, 255, 255, ${breath * 0.95})`);
      coreGlow.addColorStop(0.2, `rgba(0, 240, 255, ${breath * 0.75})`);
      coreGlow.addColorStop(0.6, `rgba(168, 85, 247, ${breath * 0.3})`);
      coreGlow.addColorStop(1, 'rgba(0, 0, 0, 0)');

      ctx.fillStyle = coreGlow;
      ctx.beginPath();
      ctx.arc(coreX, coreY, 80, 0, Math.PI * 2);
      ctx.fill();

      // Cột sáng trục dọc siêu thực
      const beamGrad = ctx.createLinearGradient(coreX, 0, coreX, h);
      beamGrad.addColorStop(0, 'rgba(0, 240, 255, 0)');
      beamGrad.addColorStop(0.35, `rgba(0, 240, 255, ${breath * 0.2})`);
      beamGrad.addColorStop(0.53, `rgba(255, 255, 255, ${breath * 0.55})`);
      beamGrad.addColorStop(0.70, `rgba(0, 240, 255, ${breath * 0.25})`);
      beamGrad.addColorStop(1, 'rgba(0, 240, 255, 0)');

      ctx.fillStyle = beamGrad;
      ctx.fillRect(coreX - 2.5, 0, 5, h);

      // =========================================================================
      // 2. SÓNG XUNG KÍCH TỎA TRÒN TỪ KHIÊN SỐ (Shield Shockwave Rings)
      // =========================================================================
      for (let i = 0; i < this.shockwaves.length; i++) {
        const sw = this.shockwaves[i];
        ctx.strokeStyle = sw.color + sw.alpha + ')';
        ctx.lineWidth = 2.2;
        ctx.beginPath();
        ctx.ellipse(sw.x * w, sw.y * h, sw.radius * 1.35, sw.radius * 0.9, 0, 0, Math.PI * 2);
        ctx.stroke();
      }

      // =========================================================================
      // 3. XUNG NĂNG LƯỢNG CHẠY DỌC ĐƯỜNG MẠCH BO MẠCH (PCB Circuit Pulses)
      // =========================================================================
      for (let i = 0; i < this.pulses.length; i++) {
        const p = this.pulses[i];
        const track = this.tracks[p.trackIdx];
        if (!track) continue;

        const headT = Math.min(1.0, p.t);
        const tailT = Math.max(0.0, p.t - p.tailLength);

        const headPos = this.interpolateTrack(track, headT);
        const tailPos = this.interpolateTrack(track, tailT);

        const hx = headPos.x * w;
        const hy = headPos.y * h;
        const tx = tailPos.x * w;
        const ty = tailPos.y * h;

        // Vệt sáng co giãn theo phối cảnh chiều sâu 3D
        const depthScale = Math.max(0.35, headPos.y);
        const strokeW = p.width * depthScale * 2.4;

        const pulseGrad = ctx.createLinearGradient(tx, ty, hx, hy);
        pulseGrad.addColorStop(0, 'rgba(0, 240, 255, 0)');
        pulseGrad.addColorStop(0.65, p.glowColor);
        pulseGrad.addColorStop(1, '#ffffff');

        ctx.strokeStyle = pulseGrad;
        ctx.lineWidth = strokeW;
        ctx.lineCap = 'round';

        ctx.beginPath();
        ctx.moveTo(tx, ty);

        const midT = (headT + tailT) * 0.5;
        const midPos = this.interpolateTrack(track, midT);
        ctx.quadraticCurveTo(midPos.x * w, midPos.y * h, hx, hy);
        ctx.stroke();

        // Đầu hạt photon sáng chói
        ctx.fillStyle = '#ffffff';
        ctx.beginPath();
        ctx.arc(hx, hy, strokeW * 0.95, 0, Math.PI * 2);
        ctx.fill();
      }

      // =========================================================================
      // 4. DẢI QUÉT LASER AN NINH LƯỚT LÊN XUỐNG (Holographic Laser Scanner)
      // =========================================================================
      const scanY = this.scanner.y * h;
      const scanSpanX = w * (0.35 + this.scanner.y * 0.55);
      const scanStartX = (w - scanSpanX) / 2;
      const scanEndX = scanStartX + scanSpanX;

      // Tia laser chính giữa với 2 đầu fade out êm dịu
      const laserGrad = ctx.createLinearGradient(scanStartX, scanY, scanEndX, scanY);
      laserGrad.addColorStop(0, 'rgba(0, 240, 255, 0)');
      laserGrad.addColorStop(0.2, 'rgba(0, 240, 255, 0.8)');
      laserGrad.addColorStop(0.5, 'rgba(255, 255, 255, 0.95)');
      laserGrad.addColorStop(0.8, 'rgba(0, 240, 255, 0.8)');
      laserGrad.addColorStop(1, 'rgba(0, 240, 255, 0)');

      ctx.strokeStyle = laserGrad;
      ctx.lineWidth = 2.0;
      ctx.beginPath();
      ctx.moveTo(scanStartX, scanY);
      ctx.lineTo(scanEndX, scanY);
      ctx.stroke();

      // Vệt sáng hologram quét sau tia laser
      const trailH = 26 * this.scanner.direction;
      const scanAreaGrad = ctx.createLinearGradient(0, scanY, 0, scanY - trailH);
      scanAreaGrad.addColorStop(0, 'rgba(0, 240, 255, 0.22)');
      scanAreaGrad.addColorStop(1, 'rgba(0, 240, 255, 0)');

      ctx.fillStyle = scanAreaGrad;
      ctx.beginPath();
      ctx.moveTo(scanStartX, scanY);
      ctx.lineTo(scanEndX, scanY);
      ctx.lineTo(scanEndX * 0.98, scanY - trailH);
      ctx.lineTo(scanStartX * 1.02, scanY - trailH);
      ctx.closePath();
      ctx.fill();

      // =========================================================================
      // 5. TIA QUÉT GIẢI MÃ MÃ NGUỒN 2 BÊN VÁCH (Code Stream Decode Shimmer)
      // =========================================================================
      for (let i = 0; i < this.codeRays.length; i++) {
        const ray = this.codeRays[i];
        const ry = ray.y * h;
        const isLeft = ray.side === 'left';
        const rx1 = isLeft ? w * 0.05 : w * 0.65;
        const rx2 = isLeft ? w * 0.35 : w * 0.95;

        const rayGrad = ctx.createLinearGradient(rx1, ry, rx2, ry);
        rayGrad.addColorStop(0, 'rgba(0, 240, 255, 0)');
        rayGrad.addColorStop(0.5, isLeft ? `rgba(0, 240, 255, ${ray.alpha * 0.7})` : `rgba(217, 70, 239, ${ray.alpha * 0.7})`);
        rayGrad.addColorStop(1, 'rgba(0, 240, 255, 0)');

        ctx.strokeStyle = rayGrad;
        ctx.lineWidth = 1.6;
        ctx.beginPath();
        ctx.moveTo(rx1, ry);
        ctx.lineTo(rx2, ry);
        ctx.stroke();
      }

      // =========================================================================
      // 6. HẠT BỤI LƯỢNG TỬ ÁNH SÁNG (Floating Photons)
      // =========================================================================
      for (let i = 0; i < this.particles.length; i++) {
        const pt = this.particles[i];
        const flicker = Math.sin(this.time * pt.flickerSpeed + i) * 0.35;
        const currentAlpha = Math.max(0.06, pt.baseAlpha + flicker);

        ctx.fillStyle = pt.color + currentAlpha + ')';
        ctx.beginPath();
        ctx.arc(pt.x * w, pt.y * h, pt.radius * pt.z, 0, Math.PI * 2);
        ctx.fill();
      }

      ctx.restore();
    }

    destroy() {
      this.stop();
      window.removeEventListener('resize', this.handleResize);
    }
  }

  global.ScrollyVFXAmbient = ScrollyVFXAmbient;
})(window);