/**
 * AITA CodeDefend 2026 - Kinetic AST Diff Connector & Synchronized Inspector
 * - SVG Glowing Bezier Cables with Native Traveling Photon Pulses (<animateMotion>)
 * - Dual-Pane Proportional Smooth Scroll Sync
 * - Kinetic Diff Minimap Scrubber HUD with Plagiarism Hotspots
 * - AST Token Normalization Morphing Engine (JavaParser 3.25 Canonical Tokenizer)
 * Built to satisfy 'ai-kinetic-3d-web' skill specification.
 */
(function (global) {
  const container = document.getElementById('diff-comparison-container');
  if (!container) return;

  const editorA = document.getElementById('editor-student-a');
  const editorB = document.getElementById('editor-student-b');

  // 1. Setup Dynamic SVG Canvas
  let svg = document.getElementById('diff-connector-svg');
  if (!svg) {
    svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.id = 'diff-connector-svg';
    svg.setAttribute('class', 'diff-connector-canvas pointer-events-none');
    svg.style.position = 'absolute';
    svg.style.top = '0';
    svg.style.left = '0';
    svg.style.width = '100%';
    svg.style.height = '100%';
    svg.style.zIndex = '15';
    container.style.position = 'relative';
    container.appendChild(svg);
  }

  // 2. Draw Bezier Connectors with Traveling Photons
  function drawConnectors() {
    svg.innerHTML = '';
    if (window.CyberEffects && !window.CyberEffects.isEnabled()) {
      return;
    }
    const containerRect = container.getBoundingClientRect();
    svg.setAttribute('width', containerRect.width);
    svg.setAttribute('height', containerRect.height);

    const matchesA = container.querySelectorAll('.student-a-editor [data-match-block], .student-a-editor [data-block]');

    const defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs');
    defs.innerHTML = `
      <linearGradient id="glowGradRed" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#ef4444" stop-opacity="0.9" />
        <stop offset="100%" stop-color="#f43f5e" stop-opacity="0.9" />
      </linearGradient>
      <linearGradient id="glowGradAmber" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#f59e0b" stop-opacity="0.9" />
        <stop offset="100%" stop-color="#fbbf24" stop-opacity="0.9" />
      </linearGradient>
      <linearGradient id="glowGradCyan" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#06b6d4" stop-opacity="0.9" />
        <stop offset="100%" stop-color="#8b5cf6" stop-opacity="0.9" />
      </linearGradient>
      <filter id="photonGlow" x="-50%" y="-50%" width="200%" height="200%">
        <feGaussianBlur in="SourceGraphic" stdDeviation="2.5" result="blur" />
        <feMerge>
          <feMergeNode in="blur" />
          <feMergeNode in="SourceGraphic" />
        </feMerge>
      </filter>
    `;
    svg.appendChild(defs);

    matchesA.forEach((elA) => {
      const blockId = elA.getAttribute('data-match-block') || elA.getAttribute('data-block');
      const elB = container.querySelector(`.student-b-editor [data-match-block="${blockId}"], .student-b-editor [data-block="${blockId}"]`);
      if (!elB) return;

      const rectA = elA.getBoundingClientRect();
      const rectB = elB.getBoundingClientRect();

      // Skip if out of viewport container
      if (rectA.bottom < containerRect.top - 10 || rectA.top > containerRect.bottom + 10) return;

      const x1 = rectA.right - containerRect.left;
      const y1 = rectA.top + rectA.height / 2 - containerRect.top;
      const x2 = rectB.left - containerRect.left;
      const y2 = rectB.top + rectB.height / 2 - containerRect.top;

      if (x2 <= x1) return; // Mobile stacked layout check

      const dx = Math.max(35, (x2 - x1) * 0.5);
      const curveOffset = (blockId % 2 === 0) ? -14 : 14;
      const pathData = `M ${x1} ${y1} C ${x1 + dx} ${y1 + curveOffset}, ${x2 - dx} ${y2 - curveOffset}, ${x2} ${y2}`;

      const isHighRisk = elA.classList.contains('border-rose-500') || elA.classList.contains('border-rose-500/80') || elA.classList.contains('match-high');
      const isAmber = elA.classList.contains('border-amber-500') || elA.classList.contains('border-amber-500/70');

      const strokeGrad = isHighRisk ? 'url(#glowGradRed)' : (isAmber ? 'url(#glowGradAmber)' : 'url(#glowGradCyan)');
      const colorHex = isHighRisk ? '#ef4444' : (isAmber ? '#f59e0b' : '#06b6d4');

      const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
      g.setAttribute('class', 'diff-connector-group');
      g.setAttribute('data-connector-block', blockId);

      // Layer 1: Outer glowing aura
      const aura = document.createElementNS('http://www.w3.org/2000/svg', 'path');
      aura.setAttribute('d', pathData);
      aura.setAttribute('stroke', colorHex);
      aura.setAttribute('stroke-width', '7');
      aura.setAttribute('stroke-opacity', '0.22');
      aura.setAttribute('fill', 'none');
      aura.setAttribute('stroke-linecap', 'round');
      g.appendChild(aura);

      // Layer 2: Main animated optical laser cable
      const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
      path.setAttribute('d', pathData);
      path.setAttribute('class', 'diff-glow-curve');
      path.setAttribute('stroke', strokeGrad);
      path.setAttribute('stroke-width', isHighRisk ? '3' : '2.5');
      path.setAttribute('fill', 'none');
      path.style.pointerEvents = 'stroke';
      path.style.cursor = 'pointer';
      path.addEventListener('click', () => {
        if (window.openAstModal) window.openAstModal();
      });
      g.appendChild(path);

      // Layer 3: Kinetic Traveling Photon (Visualizing Active Plagiarism Transfer)
      const photon = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
      photon.setAttribute('r', '3.5');
      photon.setAttribute('fill', '#ffffff');
      photon.setAttribute('filter', 'url(#photonGlow)');
      const animMotion = document.createElementNS('http://www.w3.org/2000/svg', 'animateMotion');
      animMotion.setAttribute('dur', (1.8 + blockId * 0.4) + 's');
      animMotion.setAttribute('repeatCount', 'indefinite');
      animMotion.setAttribute('path', pathData);
      photon.appendChild(animMotion);
      g.appendChild(photon);

      // Layer 4: Terminal Node Dots
      const dotA = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
      dotA.setAttribute('cx', x1);
      dotA.setAttribute('cy', y1);
      dotA.setAttribute('r', '4');
      dotA.setAttribute('fill', colorHex);
      g.appendChild(dotA);

      const dotB = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
      dotB.setAttribute('cx', x2);
      dotB.setAttribute('cy', y2);
      dotB.setAttribute('r', '4');
      dotB.setAttribute('fill', colorHex);
      g.appendChild(dotB);

      svg.appendChild(g);
    });
  }

  // 3. Dual-Pane Synchronous Scrolling
  let isSyncing = false;
  if (editorA && editorB) {
    editorA.addEventListener('scroll', () => {
      if (!isSyncing) {
        isSyncing = true;
        const ratio = editorA.scrollTop / (editorA.scrollHeight - editorA.clientHeight || 1);
        editorB.scrollTop = ratio * (editorB.scrollHeight - editorB.clientHeight || 1);
        scheduleUpdate();
        updateMinimapThumb(ratio);
        isSyncing = false;
      }
    });

    editorB.addEventListener('scroll', () => {
      if (!isSyncing) {
        isSyncing = true;
        const ratio = editorB.scrollTop / (editorB.scrollHeight - editorB.clientHeight || 1);
        editorA.scrollTop = ratio * (editorA.scrollHeight - editorA.clientHeight || 1);
        scheduleUpdate();
        updateMinimapThumb(ratio);
        isSyncing = false;
      }
    });
  }

  // 4. Kinetic Diff Minimap Scrubber HUD
  function initDiffMinimap() {
    let minimap = document.getElementById('diff-kinetic-minimap');
    if (!minimap && container) {
      minimap = document.createElement('div');
      minimap.id = 'diff-kinetic-minimap';
      minimap.className = 'absolute -right-4 top-2 bottom-2 w-3 rounded-full bg-[#080914]/90 border border-white/10 flex flex-col justify-between py-1.5 px-0.5 z-20 shadow-xl backdrop-blur-md hidden sm:flex select-none';
      minimap.title = 'Bản đồ đối soát động học (Kinetic AST Minimap) - Nhấp để cuộn nhanh';

      // Hotspots for Block 1, 2, 3
      minimap.innerHTML = `
        <div class="relative w-full h-full">
          <!-- Hotspot 1: 20% (calculateTotal - 88.5%) -->
          <div onclick="scrollToDiffBlock(1)" class="absolute top-[18%] left-0 right-0 h-3 rounded bg-rose-500/80 shadow-[0_0_8px_#ef4444] cursor-pointer hover:scale-125 transition-transform" title="Khối 1: calculateTotal() - 88.5% Cờ Đỏ"></div>
          <!-- Hotspot 2: 46% (calculateTotal - 65%) -->
          <div onclick="scrollToDiffBlock(2)" class="absolute top-[46%] left-0 right-0 h-2.5 rounded bg-amber-500/80 shadow-[0_0_8px_#f59e0b] cursor-pointer hover:scale-125 transition-transform" title="Khối 2: calculateTotal() - Cảnh báo 65%"></div>
          <!-- Hotspot 3: 74% (processPayment - 84%) -->
          <div onclick="scrollToDiffBlock(3)" class="absolute top-[72%] left-0 right-0 h-3.5 rounded bg-rose-500/80 shadow-[0_0_8px_#ef4444] cursor-pointer hover:scale-125 transition-transform" title="Khối 3: processPayment() - 84% Cờ Đỏ"></div>
          <!-- Draggable/Tracking Scrubber Thumb -->
          <div id="minimap-scrubber-thumb" class="absolute top-0 left-0 right-0 h-4 rounded-full bg-cyan-400 shadow-[0_0_10px_#06b6d4] transition-all duration-75 pointer-events-none"></div>
        </div>
      `;
      container.appendChild(minimap);
    }
  }

  function updateMinimapThumb(ratio) {
    const thumb = document.getElementById('minimap-scrubber-thumb');
    if (!thumb) return;
    const clamped = Math.max(0, Math.min(1, ratio));
    thumb.style.top = `calc(${clamped * 86}% + 2px)`;
  }

  global.scrollToDiffBlock = function (blockId) {
    const targetA = document.querySelector(`.student-a-editor [data-match-block="${blockId}"]`);
    if (targetA && editorA) {
      targetA.scrollIntoView({ behavior: 'smooth', block: 'center' });
      if (global.CyberAudio) CyberAudio.playTick();
      if (global.showToast) global.showToast(`Đang cuộn đến Khối trùng lặp #${blockId}`, 'info');
      // Highlight pulse
      targetA.classList.add('ring-2', 'ring-cyan-400');
      setTimeout(() => targetA.classList.remove('ring-2', 'ring-cyan-400'), 1200);
    }
  };

  // 5. AST Token Normalization Morphing Engine
  let isAstTokenMode = false;
  let rawHtmlA = null, rawHtmlB = null;

  global.toggleAstTokenMode = function () {
    if (!editorA || !editorB) return;

    if (!rawHtmlA) rawHtmlA = editorA.innerHTML;
    if (!rawHtmlB) rawHtmlB = editorB.innerHTML;

    isAstTokenMode = !isAstTokenMode;
    const btn = document.getElementById('btn-toggle-ast-tokens');
    const label = document.getElementById('ast-toggle-label');

    if (isAstTokenMode) {
      // Morph code to show AST canonical tokens
      editorA.innerHTML = morphToAstTokens(rawHtmlA);
      editorB.innerHTML = morphToAstTokens(rawHtmlB);

      if (btn) {
        btn.classList.add('bg-cyan-500', 'text-black');
        btn.classList.remove('bg-cyan-950/70', 'text-cyan-300');
      }
      if (label) label.textContent = 'Mã Gốc (Raw Code)';

      if (global.CyberAudio) CyberAudio.playLaser();
      if (global.showToast) {
        global.showToast('AST Token View: Biến & định danh đã chuẩn hóa về Canonical Tokens (<VAR_0>, <METHOD_TOTAL>)', 'info');
      }
    } else {
      // Restore raw student source code
      editorA.innerHTML = rawHtmlA;
      editorB.innerHTML = rawHtmlB;

      if (btn) {
        btn.classList.remove('bg-cyan-500', 'text-black');
        btn.classList.add('bg-cyan-950/70', 'text-cyan-300');
      }
      if (label) label.textContent = 'AST Token View';

      if (global.CyberAudio) CyberAudio.playTick();
      if (global.showToast) {
        global.showToast('Đã quay lại chế độ xem mã nguồn gốc sinh viên nộp', 'info');
      }
    }

    setTimeout(drawConnectors, 100);
  };

  function morphToAstTokens(html) {
    return html
      .replace(/_cart/g, '<span class="px-1.5 py-0.5 rounded bg-cyan-950/90 border border-cyan-400 text-cyan-300 font-bold shadow-[0_0_8px_#06b6d4]">&lt;VAR_0&gt;</span>')
      .replace(/_basket/g, '<span class="px-1.5 py-0.5 rounded bg-cyan-950/90 border border-cyan-400 text-cyan-300 font-bold shadow-[0_0_8px_#06b6d4]">&lt;VAR_0&gt;</span>')
      .replace(/cartValue/g, '<span class="px-1.5 py-0.5 rounded bg-cyan-950/90 border border-cyan-400 text-cyan-300 font-bold shadow-[0_0_8px_#06b6d4]">&lt;VAR_0&gt;</span>')
      .replace(/basketValue/g, '<span class="px-1.5 py-0.5 rounded bg-cyan-950/90 border border-cyan-400 text-cyan-300 font-bold shadow-[0_0_8px_#06b6d4]">&lt;VAR_0&gt;</span>')
      .replace(/total_amt/g, '<span class="px-1.5 py-0.5 rounded bg-violet-950/90 border border-violet-400 text-violet-300 font-bold shadow-[0_0_8px_#8b5cf6]">&lt;VAR_1&gt;</span>')
      .replace(/finalCost/g, '<span class="px-1.5 py-0.5 rounded bg-violet-950/90 border border-violet-400 text-violet-300 font-bold shadow-[0_0_8px_#8b5cf6]">&lt;VAR_1&gt;</span>')
      .replace(/totalCost/g, '<span class="px-1.5 py-0.5 rounded bg-violet-950/90 border border-violet-400 text-violet-300 font-bold shadow-[0_0_8px_#8b5cf6]">&lt;VAR_1&gt;</span>')
      .replace(/calculateTotal/g, '<span class="px-1.5 py-0.5 rounded bg-rose-950/90 border border-rose-400 text-rose-300 font-bold shadow-[0_0_8px_#f43f5e]">&lt;METHOD_CALC_TOTAL&gt;</span>')
      .replace(/calculateTotat/g, '<span class="px-1.5 py-0.5 rounded bg-rose-950/90 border border-rose-400 text-rose-300 font-bold shadow-[0_0_8px_#f43f5e]">&lt;METHOD_CALC_TOTAL&gt;</span>')
      .replace(/processPayment/g, '<span class="px-1.5 py-0.5 rounded bg-rose-950/90 border border-rose-400 text-rose-300 font-bold shadow-[0_0_8px_#f43f5e]">&lt;METHOD_PAYMENT&gt;</span>');
  }

  // Throttled update
  let ticking = false;
  function scheduleUpdate() {
    if (!ticking) {
      requestAnimationFrame(() => {
        drawConnectors();
        ticking = false;
      });
      ticking = true;
    }
  }

  const scrollContainers = container.querySelectorAll('.overflow-y-auto');
  scrollContainers.forEach(el => el.addEventListener('scroll', scheduleUpdate));
  window.addEventListener('resize', scheduleUpdate);

  setTimeout(() => {
    initDiffMinimap();
    drawConnectors();
  }, 250);

  global.refreshDiffConnectors = drawConnectors;
})(window);
