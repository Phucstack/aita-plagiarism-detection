/**
 * AITA Interactive 3D Cyber Synapse Engine (Perspective AST Matrix)
 * High-performance 3D Projection with Atmospheric Depth & Depth-of-Field Easing
 * Inspired by Three.js & Awwwards 2026 Interactive WebGL Trends
 */
(function () {
  const canvas = document.getElementById('cyber-canvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  let width = (canvas.width = window.innerWidth);
  let height = (canvas.height = window.innerHeight);

  const PARTICLE_COUNT = 64;
  const FOCAL_LENGTH = 480;
  const MAX_Z = 350;
  const MIN_Z = -300;
  const CONNECT_DIST_3D = 170;

  let mouseX = 0;
  let mouseY = 0;
  let rotX = 0;
  let rotY = 0;
  let targetRotX = 0;
  let targetRotY = 0;
  let isTabActive = true;

  const AST_TAGS = [
    'AST:Class', 'AST:Method', 'SHA-256', 'Gemini-1.5', 'Token:Match', 
    'Vector:Embed', 'Diff:88%', 'Risk:High', 'PRJ301', 'Synapse'
  ];

  class Particle3D {
    constructor(index) {
      this.reset(true);
      this.tag = (index % 7 === 0) ? AST_TAGS[(index / 7) % AST_TAGS.length] : null;
    }

    reset(initial = false) {
      this.x = (Math.random() - 0.5) * width * 1.3;
      this.y = (Math.random() - 0.5) * height * 1.3;
      this.z = initial ? (Math.random() * (MAX_Z - MIN_Z) + MIN_Z) : MAX_Z;
      this.vx = (Math.random() - 0.5) * 0.45;
      this.vy = (Math.random() - 0.5) * 0.45;
      this.vz = -(Math.random() * 0.6 + 0.25);
      this.baseRadius = Math.random() * 2.2 + 1.4;
      this.isCyan = Math.random() > 0.35;
      this.pulsePhase = Math.random() * Math.PI * 2;
    }

    update() {
      this.x += this.vx;
      this.y += this.vy;
      this.z += this.vz;
      this.pulsePhase += 0.03;

      if (this.z < MIN_Z) this.reset(false);
      if (Math.abs(this.x) > width * 0.85) this.vx *= -1;
      if (Math.abs(this.y) > height * 0.85) this.vy *= -1;

      // 3D Rotation Matrix projection
      const cosY = Math.cos(rotY);
      const sinY = Math.sin(rotY);
      const cosX = Math.cos(rotX);
      const sinX = Math.sin(rotX);

      // Rotate around Y
      let rx = this.x * cosY - this.z * sinY;
      let rz = this.z * cosY + this.x * sinY;

      // Rotate around X
      let ry = this.y * cosX - rz * sinX;
      rz = rz * cosX + this.y * sinX;

      this.projZ = rz;
      const scale = FOCAL_LENGTH / (FOCAL_LENGTH + rz + 400);

      this.projX = rx * scale + width / 2;
      this.projY = ry * scale + height / 2;
      this.scale = scale;
      this.alpha = Math.max(0.1, Math.min(0.9, (1 - (rz - MIN_Z) / (MAX_Z - MIN_Z)) * scale * 1.4));
    }

    draw() {
      if (this.scale <= 0) return;
      const r = this.baseRadius * this.scale * (1 + 0.2 * Math.sin(this.pulsePhase));
      ctx.beginPath();
      ctx.arc(this.projX, this.projY, Math.max(0.6, r), 0, Math.PI * 2);

      const colorPrefix = this.isCyan ? '6, 182, 212' : '139, 92, 246';
      ctx.fillStyle = `rgba(${colorPrefix}, ${this.alpha})`;
      ctx.shadowBlur = this.scale > 0.9 ? 12 : 4;
      ctx.shadowColor = `rgba(${colorPrefix}, ${this.alpha * 0.8})`;
      ctx.fill();

      // Draw floating miniature holographic tags for selected nodes
      if (this.tag && this.scale > 0.85) {
        ctx.font = '9px "JetBrains Mono", monospace';
        ctx.fillStyle = `rgba(165, 180, 252, ${this.alpha * 0.75})`;
        ctx.fillText(this.tag, this.projX + 8, this.projY - 4);
      }
    }
  }

  const particles = [];
  for (let i = 0; i < PARTICLE_COUNT; i++) {
    particles.push(new Particle3D(i));
  }

  function render() {
    if (!isTabActive) return;
    ctx.clearRect(0, 0, width, height);

    // Smooth camera rotation damping (lerp)
    rotX += (targetRotX - rotX) * 0.04;
    rotY += (targetRotY - rotY) * 0.04;

    // Update all 3D positions
    for (let i = 0; i < PARTICLE_COUNT; i++) {
      particles[i].update();
    }

    // Sort by projected Z for true depth rendering (Painter's algorithm)
    particles.sort((a, b) => b.projZ - a.projZ);

    // Draw 3D Synapse connection lines between close neighbors
    for (let i = 0; i < PARTICLE_COUNT; i++) {
      const p1 = particles[i];
      p1.draw();

      for (let j = i + 1; j < PARTICLE_COUNT; j++) {
        const p2 = particles[j];
        const dx = p1.x - p2.x;
        const dy = p1.y - p2.y;
        const dz = p1.z - p2.z;
        const dist3D = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist3D < CONNECT_DIST_3D) {
          const depthFactor = (p1.alpha + p2.alpha) * 0.5;
          const lineAlpha = (1 - dist3D / CONNECT_DIST_3D) * 0.35 * depthFactor;
          
          if (lineAlpha > 0.02) {
            ctx.beginPath();
            ctx.moveTo(p1.projX, p1.projY);
            ctx.lineTo(p2.projX, p2.projY);
            ctx.strokeStyle = `rgba(6, 182, 212, ${lineAlpha})`;
            ctx.lineWidth = Math.max(0.4, 1.1 * ((p1.scale + p2.scale) * 0.5));
            ctx.stroke();
          }
        }
      }
    }

    requestAnimationFrame(render);
  }

  // Window Resize
  window.addEventListener('resize', () => {
    width = canvas.width = window.innerWidth;
    height = canvas.height = window.innerHeight;
  });

  // Mouse camera tilt in 3D
  window.addEventListener('mousemove', (e) => {
    mouseX = (e.clientX - width / 2) / (width / 2);
    mouseY = (e.clientY - height / 2) / (height / 2);
    targetRotY = mouseX * 0.22; // subtle +/- 12 degrees
    targetRotX = -mouseY * 0.18;
  });

  // Page visibility awareness (saves battery & GPU when tab is inactive)
  document.addEventListener('visibilitychange', () => {
    isTabActive = !document.hidden;
    if (isTabActive) render();
  });

  render();
})();
