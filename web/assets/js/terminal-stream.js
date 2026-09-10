/**
 * AITA Cyber Terminal Live Stream & Number Counter Animation
 * High-tech syntax colored log stream & live statistics counter
 */
(function () {
  const TERMINAL_LOGS = [
    { tag: 'AST-INIT', tagColor: 'bg-cyan-950/70 text-cyan-300 border-cyan-500/40', text: 'Initializing JavaParser 3.25 AST token stream...', type: 'info' },
    { tag: 'SHA-256', tagColor: 'bg-slate-800 text-slate-300 border-white/10', text: 'Bytecode hash verified: <span class="text-indigo-400">8f9a4c7e2b...</span> (OrderManager.java)', type: 'muted' },
    { tag: 'TOKEN-SCAN', tagColor: 'bg-cyan-950/70 text-cyan-300 border-cyan-500/40', text: 'Extracted 1,420 identifiers across 8 packages...', type: 'info' },
    { tag: 'GEMINI-1.5', tagColor: 'bg-violet-950/70 text-violet-300 border-violet-500/40', text: 'Cross-checking vector embeddings with Student Repository...', type: 'violet' },
    { tag: 'AST-DIFF', tagColor: 'bg-amber-950/70 text-amber-300 border-amber-500/40', text: 'Variable renaming detected: <span class="text-amber-400 font-mono">_cart -> _basket</span>', type: 'warning' },
    { tag: 'CRITICAL', tagColor: 'bg-rose-950 text-rose-300 border-rose-500/60 font-bold animate-pulse', text: 'Plagiarism Flag: <span class="px-1.5 py-0.5 rounded bg-rose-500/20 border border-rose-500/50 text-rose-400 font-bold">88% Semantic Similarity</span> (Student A vs B)', type: 'danger' }
  ];

  let currentLineIndex = 0;

  function typeLogLine(logObj, container, onComplete) {
    const p = document.createElement('div');
    p.className = 'font-mono text-[11px] leading-relaxed flex items-center gap-2 opacity-0 transition-opacity duration-300 py-0.5';
    
    const tagBadge = `<span class="px-1.5 py-0.2 rounded text-[9px] border font-mono tracking-wider ${logObj.tagColor}">[${logObj.tag}]</span>`;
    p.innerHTML = `<span class="text-cyan-500 select-none">></span>${tagBadge}<span class="text-slate-200">${logObj.text}</span>`;
    container.appendChild(p);
    
    requestAnimationFrame(() => {
      p.style.opacity = '1';
      container.scrollTop = container.scrollHeight;
      if (onComplete) setTimeout(onComplete, 850);
    });
  }

  function runTerminalCycle() {
    const container = document.getElementById('terminal-stream-body');
    if (!container) return;

    if (currentLineIndex < TERMINAL_LOGS.length) {
      typeLogLine(TERMINAL_LOGS[currentLineIndex], container, () => {
        currentLineIndex++;
        runTerminalCycle();
      });
    } else {
      setTimeout(() => {
        container.innerHTML = '';
        currentLineIndex = 0;
        runTerminalCycle();
      }, 4500);
    }
  }

  // Live Number Counter Animation
  function animateCounters() {
    const counters = document.querySelectorAll('[data-counter-target]');
    counters.forEach((el) => {
      const target = parseFloat(el.getAttribute('data-counter-target'));
      const prefix = el.getAttribute('data-counter-prefix') || '';
      const suffix = el.getAttribute('data-counter-suffix') || '';
      const decimals = parseInt(el.getAttribute('data-counter-decimals') || '0', 10);
      const duration = 1200;
      const start = performance.now();

      function update(now) {
        const elapsed = now - start;
        const progress = Math.min(elapsed / duration, 1);
        const easeOut = 1 - Math.pow(1 - progress, 3);
        const currentVal = (target * easeOut).toFixed(decimals);
        el.textContent = `${prefix}${currentVal}${suffix}`;
        if (progress < 1) requestAnimationFrame(update);
      }
      requestAnimationFrame(update);
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    runTerminalCycle();
    animateCounters();
  });
})();
