/**
 * AITA CodeDefend - Copilot Voice & Speech Engine (ZeroTTS & Always-Listening STT)
 * Logic Module (< 200 lines) - Supports Sub-second Audio, Interruption, and Hands-free Continuous Recognition
 */
(function (global) {
  const ZEROTTS_BASE = 'http://127.0.0.1:8008';
  let isVoiceEnabled = localStorage.getItem('aita_voice_enabled') !== 'false';
  let activeVoiceId = localStorage.getItem('aita_voice_id') || 'maichi';
  let audioInstance = null, activeSessionId = 0, onSpeechStateChanged = null;
  let isListening = false, recognition = null, onRecognizedCb = null, onListenStateCb = null;
  let audioQueue = [], isPlayingQueue = false, preloadedAudio = null;

  function clearQueue() {
    audioQueue = [];
    isPlayingQueue = false;
    preloadedAudio = null;
  }

  function stopSpeech() {
    activeSessionId++;
    clearQueue();
    if (audioInstance) {
      try {
        audioInstance.pause(); audioInstance.currentTime = 0;
        audioInstance.removeAttribute('src'); audioInstance.load();
      } catch (e) {}
      audioInstance = null;
    }
    if (window.speechSynthesis && window.speechSynthesis.speaking) window.speechSynthesis.cancel();
    notifyState(false);
  }

  function preloadNext() {
    if (audioQueue.length > 0 && !preloadedAudio) {
      preloadedAudio = new Audio(audioQueue[0]);
      preloadedAudio.preload = 'auto';
    }
  }

  function enqueueAudio(urlOrSentence) {
    if (!isVoiceEnabled || !urlOrSentence) return;
    const url = urlOrSentence.startsWith('http') || urlOrSentence.startsWith('/')
      ? (urlOrSentence.startsWith('http') ? urlOrSentence : ZEROTTS_BASE + urlOrSentence)
      : `${ZEROTTS_BASE}/tts/stream?text=${encodeURIComponent(urlOrSentence)}&voice=${encodeURIComponent(activeVoiceId)}`;
    audioQueue.push(url);
    if (!isPlayingQueue) playNextInQueue();
    else preloadNext();
  }

  function playNextInQueue() {
    if (audioQueue.length === 0) {
      isPlayingQueue = false;
      notifyState(false);
      return;
    }
    isPlayingQueue = true;
    const nextUrl = audioQueue.shift();
    const sessionId = ++activeSessionId;
    if (audioInstance) {
      try { audioInstance.pause(); audioInstance.currentTime = 0; } catch (e) {}
      audioInstance = null;
    }
    const audio = (preloadedAudio && preloadedAudio.src === nextUrl) ? preloadedAudio : new Audio(nextUrl);
    preloadedAudio = null;
    audioInstance = audio;
    audio.onplay = () => {
      if (sessionId === activeSessionId) {
        notifyState(true);
        preloadNext();
      }
    };
    audio.onended = () => { if (sessionId === activeSessionId) playNextInQueue(); };
    audio.onerror = () => { if (sessionId === activeSessionId) playNextInQueue(); };
    audio.play().catch(() => { if (sessionId === activeSessionId) playNextInQueue(); });
  }

  function speak(text, onStart, onEnd) {
    if (!isVoiceEnabled || !text || !text.trim()) { if (onEnd) onEnd(); return; }
    const sessionId = ++activeSessionId;
    if (audioInstance) {
      try { audioInstance.pause(); audioInstance.currentTime = 0; audioInstance.removeAttribute('src'); audioInstance.load(); } catch (e) {}
      audioInstance = null;
    }
    if (window.speechSynthesis && window.speechSynthesis.speaking) window.speechSynthesis.cancel();

    const cleanText = text.replace(/[*_#`~[\]]/g, ' ').replace(/\s+/g, ' ').trim();
    const streamUrl = `${ZEROTTS_BASE}/tts/stream?text=${encodeURIComponent(cleanText)}&voice=${encodeURIComponent(activeVoiceId)}`;
    const audio = new Audio(streamUrl);
    audioInstance = audio;

    audio.onplay = () => { if (sessionId === activeSessionId) { notifyState(true); if (onStart) onStart(); } };
    audio.onended = () => { if (sessionId === activeSessionId) { notifyState(false); if (onEnd) onEnd(); } };
    audio.onerror = () => { if (sessionId === activeSessionId) fallbackSpeech(cleanText, sessionId, onStart, onEnd); };

    const playPromise = audio.play();
    if (playPromise !== undefined) {
      playPromise.catch((err) => {
        if (sessionId !== activeSessionId) return;
        if (err.name === 'NotAllowedError') {
          notifyState(false);
          const unlock = () => {
            if (sessionId === activeSessionId && audio) audio.play().then(() => notifyState(true)).catch(() => {});
            document.removeEventListener('click', unlock);
          };
          document.addEventListener('click', unlock, { once: true });
        } else { fallbackSpeech(cleanText, sessionId, onStart, onEnd); }
      });
    }
  }

  function fallbackSpeech(text, sessionId, onStart, onEnd) {
    if (!('speechSynthesis' in window) || sessionId !== activeSessionId) { notifyState(false); if (onEnd) onEnd(); return; }
    const utter = new SpeechSynthesisUtterance(text);
    utter.lang = 'vi-VN'; utter.rate = 1.05;
    utter.onstart = () => { if (sessionId === activeSessionId) { notifyState(true); if (onStart) onStart(); } };
    utter.onend = () => { if (sessionId === activeSessionId) { notifyState(false); if (onEnd) onEnd(); } };
    utter.onerror = () => { if (sessionId === activeSessionId) { notifyState(false); if (onEnd) onEnd(); } };
    window.speechSynthesis.speak(utter);
  }

  function notifyState(speaking) {
    if (typeof onSpeechStateChanged === 'function') onSpeechStateChanged(speaking);
    // Echo guard: tạm ngắt nghe khi AI đang phát âm qua loa để tránh lặp âm
    if (recognition && isListening) {
      try {
        if (speaking) recognition.stop();
        else setTimeout(() => { if (isListening) try { recognition.start(); } catch (e) {} }, 300);
      } catch (e) {}
    }
  }

  // Chế độ Luôn luôn lắng nghe (Hands-free Continuous Voice Recognition)
  function initRecognition() {
    const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SR) return;
    recognition = new SR();
    recognition.continuous = true;
    recognition.interimResults = false;
    recognition.lang = 'vi-VN';

    recognition.onresult = (e) => {
      if (audioInstance && !audioInstance.paused) return; // Bảo vệ chống dội âm
      const last = e.results.length - 1;
      const transcript = e.results[last][0].transcript.trim();
      if (transcript && onRecognizedCb) onRecognizedCb(transcript);
    };
    recognition.onend = () => {
      if (isListening && (!audioInstance || audioInstance.paused)) {
        try { recognition.start(); } catch (e) {}
      }
    };
  }

  function startListening() {
    if (!recognition) initRecognition();
    if (!recognition) return false;
    isListening = true;
    try { recognition.start(); } catch (e) {}
    if (onListenStateCb) onListenStateCb(true);
    return true;
  }

  function stopListening() {
    isListening = false;
    if (recognition) { try { recognition.stop(); } catch (e) {} }
    if (onListenStateCb) onListenStateCb(false);
  }

  const AITAVoice = {
    speak, stop: stopSpeech,
    enqueueAudio, clearQueue,
    isEnabled: () => isVoiceEnabled,
    setEnabled: (val) => { isVoiceEnabled = !!val; localStorage.setItem('aita_voice_enabled', isVoiceEnabled); if (!isVoiceEnabled) stopSpeech(); },
    getVoice: () => activeVoiceId,
    setVoice: (vid) => { activeVoiceId = vid; localStorage.setItem('aita_voice_id', vid); },
    onStateChange: (cb) => { onSpeechStateChanged = cb; },
    startListening, stopListening,
    toggleListening: () => { if (isListening) stopListening(); else startListening(); },
    isListening: () => isListening,
    onRecognized: (cb) => { onRecognizedCb = cb; },
    onListenState: (cb) => { onListenStateCb = cb; },
    getSessionId: () => activeSessionId
  };

  global.AITAVoice = AITAVoice;
})(window);
