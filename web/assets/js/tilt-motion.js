/**
 * AITA 3D Perspective Tilt & Morphing Tab Indicator
 * Inspired by Aceternity 3D Pin & Apple Dynamic Island / Linear Tabs
 */
(function () {
  // 1. 3D Card Perspective Tilt
  function initCardTilt() {
    const tiltCards = document.querySelectorAll('[data-tilt]');
    tiltCards.forEach((card) => {
      card.style.transition = 'transform 0.15s ease-out';
      card.style.transformStyle = 'preserve-3d';

      card.addEventListener('mousemove', (e) => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const rotateX = ((y / rect.height) - 0.5) * -12;
        const rotateY = ((x / rect.width) - 0.5) * 12;
        card.style.transform = `perspective(800px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg) scale3d(1.02, 1.02, 1.02)`;
      });

      card.addEventListener('mouseleave', () => {
        card.style.transition = 'transform 0.4s cubic-bezier(0.22, 1, 0.36, 1)';
        card.style.transform = 'perspective(800px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
      });
    });
  }

  // 2. Sliding Morphing Tab Indicator
  window.updateTabPill = function (btnElement) {
    const indicator = document.getElementById('role-pill-indicator');
    const container = document.getElementById('role-tab-container');
    if (!indicator || !container || !btnElement) return;

    const containerRect = container.getBoundingClientRect();
    const btnRect = btnElement.getBoundingClientRect();

    const left = btnRect.left - containerRect.left;
    const width = btnRect.width;

    indicator.style.transform = `translateX(${left}px)`;
    indicator.style.width = `${width}px`;
    indicator.style.opacity = '1';
  };

  document.addEventListener('DOMContentLoaded', () => {
    initCardTilt();
    const initialActive = document.querySelector('.role-tab-btn.active');
    if (initialActive) {
      setTimeout(() => window.updateTabPill(initialActive), 80);
    }
  });

  window.addEventListener('resize', () => {
    const active = document.querySelector('.role-tab-btn.active');
    if (active) window.updateTabPill(active);
  });
})();
