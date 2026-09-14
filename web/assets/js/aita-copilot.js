/**
 * AITA CodeDefend - AI Copilot & Voice Assistant Coordinator
 * UI Module (< 250 lines) - Connects Floating Orb, Subtitles, Quick Drawer & Multimodal Chat
 */
(function (global) {
  let typeTimer = null;

  function typewriter(el, text) {
    if (typeTimer) clearInterval(typeTimer);
    if (!el) return;
    el.innerHTML = '';
    let i = 0;
    typeTimer = setInterval(() => {
      if (i < text.length) { el.textContent += text.charAt(i++); }
      else { clearInterval(typeTimer); typeTimer = null; }
    }, 20);
  }

  function mountUI() {
    if (document.getElementById('aita-copilot-root')) return;
    const isVoice = window.AITAVoice ? window.AITAVoice.isEnabled() : true;
    const root = document.createElement('div');
    root.id = 'aita-copilot-root';
    root.className = 'aita-copilot-root';
    root.innerHTML = `
      <!-- Speech Subtitles Bubble (Codex Pet Style Transparent HUD Balloon) -->
      <div id="aita-speech-bubble" class="aita-bubble-container">
        <div class="aita-bubble-topbar">
          <span class="aita-bubble-badge"><span class="aita-badge-dot"></span>Pet Assistant</span>
          <div class="aita-bubble-mini-tools">
            <button id="aita-btn-voice" class="aita-mini-tool ${isVoice ? 'active' : ''}" title="Bật/Tắt Giọng Nói"><i data-lucide="${isVoice ? 'volume-2' : 'volume-x'}" class="w-3 h-3"></i></button>
            <button id="aita-btn-replay" class="aita-mini-tool" title="Nghe Lại"><i data-lucide="rotate-ccw" class="w-3 h-3"></i></button>
            <button id="aita-btn-open-drawer" class="aita-mini-tool" title="Mở Khung Chat Lớn"><i data-lucide="maximize-2" class="w-3 h-3"></i></button>
            <button id="aita-btn-close" class="aita-mini-tool" title="Đóng"><i data-lucide="x" class="w-3 h-3"></i></button>
          </div>
        </div>
        <div id="aita-bubble-text" class="aita-bubble-text"></div>
        <div class="aita-bubble-input-row">
          <input type="text" id="aita-bubble-input" class="aita-bubble-input" placeholder="Hỏi nhanh AI (Enter để gửi)..." />
          <button id="aita-bubble-mic-btn" class="aita-bubble-mic-btn" title="Luôn luôn lắng nghe (Bật/Tắt Mic)"><i data-lucide="mic" class="w-3.5 h-3.5"></i></button>
          <button id="aita-bubble-send-btn" class="aita-bubble-send-btn" title="Gửi"><i data-lucide="send" class="w-3 h-3"></i></button>
        </div>
        <div class="aita-bubble-tail"></div>
      </div>

      <!-- Floating Orb Button -->
      <button id="aita-copilot-orb" class="aita-copilot-orb-btn aita-copilot-interactive" title="AITA AI Assistant">
        <div class="aita-copilot-ring"></div>
        <div class="aita-copilot-aura"></div>
        <div class="aita-copilot-badge"></div>
        <i data-lucide="sparkles" class="aita-copilot-icon"></i>
        <div class="aita-wave-bars">
          <span class="aita-wave-bar"></span><span class="aita-wave-bar"></span>
          <span class="aita-wave-bar"></span><span class="aita-wave-bar"></span>
        </div>
      </button>

      <!-- Backdrop & Multimodal AI Drawer -->
      <div id="aita-drawer-backdrop" class="aita-drawer-backdrop"></div>
      <div id="aita-drawer" class="aita-drawer">
        <div class="aita-drawer-header">
          <div class="aita-drawer-title">
            <i data-lucide="bot" class="w-5 h-5 text-cyan-400"></i>
            <span>Trợ Lý AITA CodeDefend</span>
          </div>
          <button id="aita-btn-close-drawer" class="aita-ctrl-btn"><i data-lucide="x" class="w-4 h-4"></i></button>
        </div>

        <!-- Quick Suggestions Horizontal Scroll -->
        <div class="aita-suggestions-bar" id="aita-suggestions-bar"></div>

        <!-- Multimodal Chat Message Thread -->
        <div class="aita-chat-thread" id="aita-chat-thread"></div>

        <!-- Attachment Preview Bar -->
        <div class="aita-attachment-bar" id="aita-attachment-bar" style="display: none;"></div>

        <!-- Multimodal Input Bar -->
        <div class="aita-input-container">
          <input type="file" id="aita-file-input" style="display: none;" accept=".java,.txt,.zip,.py,.json,.sql,.png,.jpg,.jpeg" />
          <button id="aita-btn-attach" class="aita-icon-btn" title="Đính kèm mã nguồn hoặc hình ảnh">
            <i data-lucide="paperclip" class="w-4 h-4"></i>
          </button>
          <textarea id="aita-chat-input" rows="1" class="aita-chat-textarea" placeholder="Gõ tin nhắn, dán ảnh (Ctrl+V) hoặc gửi tệp code..."></textarea>
          <button id="aita-btn-send" class="aita-send-btn" title="Gửi tin nhắn">
            <i data-lucide="send" class="w-4 h-4"></i>
          </button>
        </div>
        <div class="aita-input-hint">Hỗ trợ dán ảnh màn hình clipboard (Ctrl+V), tệp .java, .zip</div>
      </div>
    `;
    document.body.appendChild(root);

    // Setup FAQs suggestions
    const suggBar = document.getElementById('aita-suggestions-bar');
    if (window.AITAContext && window.AITAContext.FAQS && suggBar) {
      window.AITAContext.FAQS.forEach(item => {
        const btn = document.createElement('button');
        btn.className = 'aita-sugg-chip';
        btn.innerHTML = `<i data-lucide="${item.icon}" class="w-3 h-3 text-cyan-400"></i><span>${item.q}</span>`;
        btn.onclick = () => {
          if (window.AITAChat) window.AITAChat.appendAI(item.a, item.a);
          if (window.AITAVoice) window.AITAVoice.speak(item.a);
        };
        suggBar.appendChild(btn);
      });
    }

    // Connect voice visualizer
    if (window.AITAVoice) {
      window.AITAVoice.onStateChange((speaking) => {
        const orb = document.getElementById('aita-copilot-orb');
        if (orb) {
          if (speaking) orb.classList.add('is-speaking');
          else orb.classList.remove('is-speaking');
        }
      });
    }

    // Initialize Chat module
    if (window.AITAChat) window.AITAChat.init();

    // Bind UI actions
    const orb = document.getElementById('aita-copilot-orb'), bubble = document.getElementById('aita-speech-bubble');
    const drawer = document.getElementById('aita-drawer'), backdrop = document.getElementById('aita-drawer-backdrop');
    const attachBtn = document.getElementById('aita-btn-attach'), fileInput = document.getElementById('aita-file-input');
    if (attachBtn && fileInput) attachBtn.onclick = () => fileInput.click();

    const openDrawer = () => {
      bubble.classList.remove('is-visible'); drawer.classList.add('is-open');
      setTimeout(() => { const inp = document.getElementById('aita-chat-input'); if (inp) inp.focus(); }, 150);
    };
    const closeDrawer = () => { drawer.classList.remove('is-open'); bubble.classList.add('is-visible'); };

    const bInp = document.getElementById('aita-bubble-input');
    const bBtn = document.getElementById('aita-bubble-send-btn');
    const micBtn = document.getElementById('aita-bubble-mic-btn');
    const sendBubbleMsg = () => {
      const val = bInp ? bInp.value.trim() : '';
      if (!val) return;
      bInp.value = '';
      if (window.AITAChat) window.AITAChat.sendFromBubble(val);
    };
    if (bInp) bInp.addEventListener('keydown', (e) => { if (e.key === 'Enter') sendBubbleMsg(); });
    if (bBtn) bBtn.onclick = sendBubbleMsg;
    if (micBtn && window.AITAVoice) {
      micBtn.onclick = () => window.AITAVoice.toggleListening();
      window.AITAVoice.onListenState((listening) => {
        micBtn.classList.toggle('is-listening', listening);
        const icon = micBtn.querySelector('[data-lucide]');
        if (icon) icon.setAttribute('data-lucide', listening ? 'mic' : 'mic-off');
        if (window.lucide) lucide.createIcons();
      });
      window.AITAVoice.onRecognized((transcript) => {
        if (bInp) bInp.value = transcript;
        if (window.AITAChat) window.AITAChat.sendFromBubble(transcript);
      });
    }

    orb.onclick = () => {
      bubble.classList.toggle('is-visible');
      if (bubble.classList.contains('is-visible')) AITACopilot.sayContext();
    };

    document.getElementById('aita-btn-voice').onclick = () => {
      if (!window.AITAVoice) return;
      const nextState = !window.AITAVoice.isEnabled();
      window.AITAVoice.setEnabled(nextState);
      const btn = document.getElementById('aita-btn-voice');
      btn.classList.toggle('active', nextState);
      const icon = btn.querySelector('[data-lucide]');
      if (icon) icon.setAttribute('data-lucide', nextState ? 'volume-2' : 'volume-x');
      if (window.lucide) lucide.createIcons();
      if (nextState) AITACopilot.sayContext();
    };

    document.getElementById('aita-btn-replay').onclick = () => AITACopilot.sayContext();
    document.getElementById('aita-btn-close').onclick = () => bubble.classList.remove('is-visible');
    document.getElementById('aita-btn-open-drawer').onclick = openDrawer;
    document.getElementById('aita-btn-close-drawer').onclick = closeDrawer;
    if (backdrop) backdrop.onclick = closeDrawer;
    if (window.lucide) lucide.createIcons();
  }

  function ensureDependencies(cb) {
    if (window.AITAVoice && window.AITAChat && window.AITAObserver) { cb(); return; }
    const scripts = document.getElementsByTagName('script');
    let basePath = '../assets/js/';
    for (let i = 0; i < scripts.length; i++) {
      if (scripts[i].src && scripts[i].src.indexOf('aita-copilot.js') !== -1) {
        basePath = scripts[i].src.substring(0, scripts[i].src.lastIndexOf('/') + 1);
        break;
      }
    }
    const queue = [];
    if (!window.AITAVoice) queue.push(basePath + 'aita-copilot-voice.js?v=2.0');
    if (!window.AITAChat) queue.push(basePath + 'aita-copilot-chat.js?v=2.0');
    if (!window.AITAObserver) queue.push(basePath + 'aita-copilot-observer.js?v=2.0');
    if (queue.length === 0) { cb(); return; }
    let done = 0;
    queue.forEach(src => {
      const s = document.createElement('script');
      s.src = src;
      s.onload = s.onerror = () => { if (++done === queue.length) cb(); };
      document.head.appendChild(s);
    });
  }

  const AITACopilot = {
    typewriter,
    init: function () {
      ensureDependencies(() => {
        mountUI();
        setTimeout(() => {
          if (window.AITAChat) window.AITAChat.appendAI(`👋 **Chào bạn! Tôi là Trợ lý AI AITA CodeDefend.**\n\nTôi sẵn sàng hỗ trợ giải đáp thắc mắc, bóc tách tệp mã nguồn Java hoặc đối chiếu hình ảnh. Bạn có thể gõ chat tay, dán ảnh (Ctrl+V) hoặc bấm micro nghe giọng nói trực tiếp!`, null);
          this.sayContext();
        }, 600);
      });
    },
    sayContext: function () {
      const msg = window.AITAContext ? window.AITAContext.getMessage() : "Xin chào! Tôi là trợ lý AI.";
      this.speak(msg);
    },
    speak: function (text) {
      const bubble = document.getElementById('aita-speech-bubble');
      const textEl = document.getElementById('aita-bubble-text');
      if (bubble && textEl) { bubble.classList.add('is-visible'); typewriter(textEl, text); }
      if (window.AITAVoice) window.AITAVoice.speak(text);
    },
    stop: function () { if (window.AITAVoice) window.AITAVoice.stop(); }
  };

  global.AITACopilot = AITACopilot;
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', () => AITACopilot.init());
  else AITACopilot.init();
})(window);
