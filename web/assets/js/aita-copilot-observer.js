/**
 * AITA CodeDefend - Realtime Web Session Observer
 * Logic Module (< 200 lines) - Observes user interactions, navigation & idle states to offer proactive AI guidance
 */
(function (global) {
  let isObserverEnabled = true;
  let idleTimer = null;
  let lastActionTime = Date.now();
  const IDLE_TIMEOUT_MS = 45000; // 45 giây không thao tác

  function observeUserEvents() {
    document.addEventListener('click', handleUserClick, { passive: true });
    document.addEventListener('change', handleUserChange, { passive: true });
    ['mousemove', 'keydown', 'scroll'].forEach(evt => {
      document.addEventListener(evt, resetIdleTimer, { passive: true });
    });
    resetIdleTimer();
  }

  function resetIdleTimer() {
    lastActionTime = Date.now();
    if (idleTimer) clearTimeout(idleTimer);
    if (!isObserverEnabled) return;

    idleTimer = setTimeout(() => {
      handleUserIdle();
    }, IDLE_TIMEOUT_MS);
  }

  function handleUserIdle() {
    if (!isObserverEnabled || !window.AITACopilot) return;
    const path = window.location.pathname.toLowerCase();
    let prompt = "";
    if (path.includes('diff')) {
      prompt = "Thầy đang dừng ở màn hình Soi Vi Phạm. Thầy có muốn tôi phân tích cụ thể các khối lệnh nghi ngờ sao chép giữa 2 sinh viên không?";
    } else if (path.includes('batch')) {
      prompt = "Thầy có thể tải lên file nén ZIP để quét AST hàng loạt, hoặc bấm vào nút Quét mẫu để xem thử quy trình đối soát.";
    } else if (path.includes('student')) {
      prompt = "Bạn có cần hướng dẫn cách băm SHA-256 hoặc cách soạn đơn giải trình khiếu nại cờ đỏ không?";
    }
    if (prompt) {
      window.AITACopilot.speak(prompt);
    }
  }

  function handleUserClick(e) {
    if (!isObserverEnabled || !window.AITACopilot) return;
    const target = e.target;
    if (!target) return;

    // Bỏ qua tương tác bên trong Copilot UI
    if (target.closest('#aita-copilot-root')) return;

    // 1. Click bài nộp sinh viên
    const studentCard = target.closest('[data-student], .student-row, .dashboard-data-row, [data-match], tr');
    if (studentCard && (studentCard.innerText.includes('SE1') || studentCard.innerText.includes('PRJ301') || studentCard.innerText.includes('Long') || studentCard.innerText.includes('Huy'))) {
      const name = studentCard.querySelector('.font-bold, strong, .student-name')?.innerText || "sinh viên";
      const hint = `Đang theo dõi bài nộp của ${name}. Cây cú pháp AST đang phân tích mức độ tương đồng logic.`;
      dispatchObservation(hint);
      return;
    }

    // 2. Click nút Quét mới hoặc Bắt đầu quét
    const scanBtn = target.closest('button, a');
    if (scanBtn) {
      const txt = scanBtn.innerText.toLowerCase();
      if (txt.includes('quét') || txt.includes('scan') || txt.includes('bắt đầu')) {
        dispatchObservation("Đang khởi động đợt quét AST. Hệ thống sẽ băm SHA-256 và so khớp k-gram song song.");
        return;
      }
      if (txt.includes('soi') || txt.includes('diff') || txt.includes('đối chiếu')) {
        dispatchObservation("Chuyển sang giao diện Soi Vi Phạm. Các khối lệnh giống nhau được bôi màu song song hai cột.");
        return;
      }
      if (txt.includes('khiếu nại') || txt.includes('appeal') || txt.includes('giải trình')) {
        dispatchObservation("Mở cổng giải trình khiếu nại. Hãy chuẩn bị đường dẫn commit Git minh chứng lịch sử làm bài.");
        return;
      }
    }
  }

  function handleUserChange(e) {
    if (!isObserverEnabled || !window.AITACopilot) return;
    const target = e.target;
    if (target.type === 'range' || target.id?.includes('threshold')) {
      dispatchObservation(`Đã điều chỉnh ngưỡng cờ đỏ sang ${target.value}%. Các bài nộp vượt ngưỡng này sẽ bị cảnh báo.`);
    }
  }

  let observationDebounce = null;
  function dispatchObservation(text) {
    if (!text || !isObserverEnabled) return;
    if (observationDebounce) clearTimeout(observationDebounce);
    observationDebounce = setTimeout(() => {
      if (window.AITACopilot) {
        window.AITACopilot.speak(text);
      }
    }, 400);
  }

  const AITAObserver = {
    init: observeUserEvents,
    setEnabled: (val) => { isObserverEnabled = !!val; },
    isEnabled: () => isObserverEnabled
  };

  global.AITAObserver = AITAObserver;
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => AITAObserver.init());
  } else {
    AITAObserver.init();
  }
})(window);
