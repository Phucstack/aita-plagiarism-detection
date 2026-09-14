/**
 * AITA CodeDefend - Multimodal AI Copilot Chat Controller
 * Logic Module (< 200 lines) - Handles text input, clipboard image paste, file attachment & API chat
 */
(function (global) {
  const CHAT_ENDPOINT = 'http://127.0.0.1:8008/api/chat';
  let attachedFile = null, attachedImage = null;

  function initChat() {
    const input = document.getElementById('aita-chat-input'), fileInput = document.getElementById('aita-file-input');
    const sendBtn = document.getElementById('aita-btn-send');
    if (!input || !sendBtn) return;
    input.addEventListener('keydown', (e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); } });
    sendBtn.addEventListener('click', () => sendMessage());
    input.addEventListener('paste', (e) => {
      const items = (e.clipboardData || window.clipboardData).items || [];
      for (let i = 0; i < items.length; i++) {
        if (items[i].type.indexOf('image') !== -1) { e.preventDefault(); handleImageAttachment(items[i].getAsFile()); break; }
      }
    });
    if (fileInput) {
      fileInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) { if (file.type.startsWith('image/')) handleImageAttachment(file); else handleFileAttachment(file); }
        fileInput.value = '';
      });
    }
  }

  function handleImageAttachment(file) {
    const r = new FileReader();
    r.onload = (ev) => { attachedImage = { name: file.name || 'clipboard.png', base64: ev.target.result }; renderAttachmentBar(); };
    r.readAsDataURL(file);
  }
  function handleFileAttachment(file) {
    const r = new FileReader();
    r.onload = (ev) => { attachedFile = { name: file.name, size: (file.size / 1024).toFixed(1) + ' KB', content: ev.target.result }; renderAttachmentBar(); };
    r.readAsText(file);
  }
  function renderAttachmentBar() {
    const bar = document.getElementById('aita-attachment-bar');
    if (!bar) return;
    bar.innerHTML = '';
    bar.style.display = (attachedFile || attachedImage) ? 'flex' : 'none';
    if (attachedImage) {
      bar.innerHTML += `<div class="aita-attach-chip"><img src="${attachedImage.base64}" class="aita-attach-thumb" /><span class="aita-attach-name">${attachedImage.name}</span><button class="aita-attach-remove" onclick="AITAChat.removeImg()">&times;</button></div>`;
    }
    if (attachedFile) {
      bar.innerHTML += `<div class="aita-attach-chip"><i data-lucide="file-code" class="w-3.5 h-3.5 text-cyan-400"></i><span class="aita-attach-name">${attachedFile.name}</span><button class="aita-attach-remove" onclick="AITAChat.removeFile()">&times;</button></div>`;
    }
    if (window.lucide) lucide.createIcons();
  }

  function formatMarkdown(md) {
    if (!md) return '';
    return md.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>').replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/^- (.*)$/gm, '<li>$1</li>').replace(/\n\n/g, '<br/><br/>').replace(/\n/g, '<br/>');
  }

  const CHAT_STREAM_ENDPOINT = 'http://127.0.0.1:8008/api/chat/stream';

  function createStreamingBubble() {
    const thread = document.getElementById('aita-chat-thread');
    if (!thread) return null;
    const div = document.createElement('div');
    div.className = 'aita-msg-row aita-msg-ai';
    div.innerHTML = `<div class="aita-msg-avatar"><i data-lucide="bot" class="w-4 h-4 text-cyan-400"></i></div><div class="aita-msg-bubble ai"><div class="aita-msg-content"></div></div>`;
    thread.appendChild(div);
    if (window.lucide) lucide.createIcons();
    return div;
  }

  async function callChatApi(text, file, img, onDone) {
    const role = (window.AITAContext && typeof window.AITAContext.getRole === 'function') ? window.AITAContext.getRole() : 'student';
    const payload = { message: text, role, file_name: file ? file.name : null, file_content: file ? file.content : null, image_base64: img ? img.base64 : null };
    if (window.AITAVoice) window.AITAVoice.stop();

    try {
      const res = await fetch(CHAT_STREAM_ENDPOINT, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok || !res.body) throw new Error('Stream failed');

      if (onDone) onDone();
      const reader = res.body.getReader();
      const decoder = new TextDecoder('utf-8');
      let buffer = '', accumulated = '', aiRow = null;

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const parts = buffer.split('\n\n');
        buffer = parts.pop();

        for (const block of parts) {
          const trimmed = block.trim();
          if (!trimmed.startsWith('data:')) continue;
          try {
            const data = JSON.parse(trimmed.substring(5).trim());
            if (data.type === 'text') {
              accumulated += data.delta;
              if (!aiRow) aiRow = createStreamingBubble();
              const c = aiRow.querySelector('.aita-msg-content');
              if (c) c.innerHTML = formatMarkdown(accumulated);
              updateBubbleSubtitle(accumulated);
              const thread = document.getElementById('aita-chat-thread');
              if (thread) thread.scrollTop = thread.scrollHeight;
            } else if (data.type === 'audio' && data.url) {
              if (window.AITAVoice) window.AITAVoice.enqueueAudio(data.url);
            }
          } catch (e) {}
        }
      }
    } catch (err) {
      fallbackSyncChat(payload, onDone);
    }
  }

  async function fallbackSyncChat(payload, onDone) {
    try {
      const res = await fetch(CHAT_ENDPOINT, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      updateBubbleSubtitle(data.voice_summary || data.reply);
      appendAIBubble(data.reply, data.voice_summary);
      if (window.AITAVoice && data.voice_summary) window.AITAVoice.speak(data.voice_summary);
    } catch (e) {
      appendAIBubble("⚠️ Chưa kết nối được ZeroTTS (Port 8008).", null);
      updateBubbleSubtitle("Chưa kết nối được ZeroTTS (Port 8008).");
    } finally {
      if (onDone) onDone();
    }
  }

  async function sendMessage() {
    const input = document.getElementById('aita-chat-input');
    const text = input ? input.value.trim() : '';
    if (!text && !attachedFile && !attachedImage) return;
    appendUserBubble(text, attachedFile, attachedImage);
    const curFile = attachedFile, curImg = attachedImage;
    if (input) input.value = '';
    attachedFile = null; attachedImage = null;
    renderAttachmentBar();
    const bText = document.getElementById('aita-bubble-text');
    if (bText) bText.innerHTML = '<div class="aita-thinking-dots"><span></span><span></span><span></span></div>';
    await callChatApi(text, curFile, curImg);
  }

  function updateBubbleSubtitle(text) {
    const bText = document.getElementById('aita-bubble-text');
    if (bText && text) bText.textContent = text;
  }

  async function sendFromBubble(text) {
    if (!text || !text.trim()) return;
    const bText = document.getElementById('aita-bubble-text');
    if (bText) bText.innerHTML = '<div class="aita-thinking-dots"><span></span><span></span><span></span></div>';
    appendUserBubble(text, null, null);
    await callChatApi(text, null, null);
  }

  function appendUserBubble(text, file, img) {
    const thread = document.getElementById('aita-chat-thread');
    if (!thread) return;
    const div = document.createElement('div');
    div.className = 'aita-msg-row aita-msg-user';
    let extra = img ? `<img src="${img.base64}" class="aita-msg-img-preview" />` : '';
    if (file) extra += `<div class="aita-msg-file-badge"><i data-lucide="file-text" class="w-3.5 h-3.5"></i> ${file.name}</div>`;
    div.innerHTML = `<div class="aita-msg-bubble user">${extra}${text ? `<div>${text}</div>` : ''}</div>`;
    thread.appendChild(div);
    thread.scrollTop = thread.scrollHeight;
    if (window.lucide) lucide.createIcons();
  }

  function appendAIBubble(reply, voiceSummary) {
    const thread = document.getElementById('aita-chat-thread');
    if (!thread) return;
    const div = document.createElement('div');
    div.className = 'aita-msg-row aita-msg-ai';
    div.innerHTML = `<div class="aita-msg-avatar"><i data-lucide="bot" class="w-4 h-4 text-cyan-400"></i></div><div class="aita-msg-bubble ai"><div>${formatMarkdown(reply)}</div></div>`;
    thread.appendChild(div);
    thread.scrollTop = thread.scrollHeight;
    if (window.lucide) lucide.createIcons();
  }

  global.AITAChat = {
    init: initChat, send: sendMessage, sendFromBubble, appendAI: appendAIBubble,
    removeImg: () => { attachedImage = null; renderAttachmentBar(); },
    removeFile: () => { attachedFile = null; renderAttachmentBar(); }
  };
})(window);
