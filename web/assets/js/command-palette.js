/**
 * AITA Global Command Palette (Ctrl+K / Cmd+K)
 * Inspired by Linear & Raycast Spotlight Interface
 */
(function () {
  function isStudentContext() {
    if (window.AITA_USER_ROLE === 'STUDENT') return true;
    const path = (window.location.pathname || '').toLowerCase();
    if (path.includes('student-portal')) return true;
    const params = new URLSearchParams(window.location.search);
    if (params.get('role') === 'student') return true;
    return false;
  }

  function getCommands() {
    if (isStudentContext()) {
      return [
        { id: 'cmd-student-portal', title: 'Cổng Sinh Viên - Tra Cứu Bài Nộp & Kết Quả', group: 'Điều Hướng Sinh Viên', icon: 'graduation-cap', action: () => navigateTo('student-portal') },
        { id: 'cmd-student-inspect', title: 'Xem Đối Soát AST (OrderManager_LongTV.java - 88.5%)', group: 'Bài Nộp Của Bạn', icon: 'code-2', action: () => (typeof window.openStudentInspectModal === 'function' ? window.openStudentInspectModal() : navigateTo('diff-inspector?reportId=1&role=student')) },
        { id: 'cmd-student-appeal', title: 'Gửi Đơn Giải Trình Khiếu Nại Liêm Chính (#DP-2026)', group: 'Hành Động Khảo Thí', icon: 'message-square-warning', action: () => (typeof window.openAppealModal === 'function' ? window.openAppealModal() : navigateTo('student-portal')) },
        { id: 'cmd-ast-modal', title: 'Xem Giải Thuật Cây Cú Pháp AST & Gemini 1.5 Pro', group: 'Hệ Thống & AI', icon: 'cpu', action: () => { if (window.openAstModal) window.openAstModal(); } },
        { id: 'cmd-sidebar', title: 'Thu Gọn / Mở Rộng Sidebar (Ctrl+B)', group: 'Giao Diện', icon: 'panel-left-close', action: () => { if (window.toggleSidebar) window.toggleSidebar(); } },
        { id: 'cmd-logout', title: 'Đăng Xuất Tài Khoản Sinh Viên', group: 'Tài Khoản', icon: 'log-out', action: () => navigateTo('login') }
      ];
    }
    return [
      { id: 'nav-dash', title: 'Dashboard Giám Sát', group: 'Điều Hướng', icon: 'layout-grid', action: () => navigateTo('dashboard') },
      { id: 'nav-batch', title: 'Batch Scanner - Quét Hàng Loạt', group: 'Điều Hướng', icon: 'scan-line', action: () => navigateTo('batch-scanner') },
      { id: 'nav-diff', title: 'Diff Inspector - Soi Đối Chiếu Mã Nguồn', group: 'Điều Hướng', icon: 'split-square-vertical', action: () => navigateTo('diff-inspector?reportId=1') },
      { id: 'nav-student', title: 'Student Portal - Cổng Tra Cứu Sinh Viên', group: 'Điều Hướng', icon: 'graduation-cap', action: () => navigateTo('student-portal') },
      { id: 'action-ast', title: 'Xem Giải Thuật Cây Cú Pháp AST & Gemini 1.5 Pro', group: 'Hệ Thống & AI', icon: 'cpu', action: () => { if (window.openAstModal) window.openAstModal(); } },
      { id: 'action-sidebar', title: 'Thu Gọn / Mở Rộng Sidebar (Ctrl+B)', group: 'Giao Diện', icon: 'panel-left-close', action: () => { if (window.toggleSidebar) window.toggleSidebar(); } },
      { id: 'sub-ordermanager', title: 'OrderManager.java — Báo cáo đạo văn #882 (88% Trùng lặp)', group: 'Bài Nộp Cần Xử Lý', icon: 'alert-triangle', action: () => navigateTo('diff-inspector?reportId=1') },
      { id: 'sub-cartservice', title: 'CartService.java — Báo cáo #883 (12% Ngưỡng an toàn)', group: 'Bài Nộp Cần Xử Lý', icon: 'check-circle-2', action: () => navigateTo('diff-inspector?reportId=1') },
      { id: 'nav-logout', title: 'Đăng Xuất Giảng Viên', group: 'Tài Khoản', icon: 'log-out', action: () => navigateTo('login') }
    ];
  }

  function getBasePath() {
    const isJsp = window.location.pathname.endsWith('.jsp') || (!window.location.pathname.endsWith('.html') && window.location.pathname.includes('/d%E1%BB%B1%20%C3%A1n/'));
    const isHtmlPreview = window.location.pathname.includes('/preview/') || window.location.pathname.endsWith('.html');
    return { isJsp, isHtmlPreview };
  }

  function navigateTo(target) {
    const { isHtmlPreview } = getBasePath();
    if (isHtmlPreview) {
      const parts = target.split('?');
      const baseTarget = parts[0];
      const query = parts.length > 1 ? '?' + parts[1] : '';
      window.location.href = baseTarget + '.html' + query;
    } else {
      const contextPath = window.AITA_CONTEXT_PATH || '';
      window.location.href = contextPath + '/' + target;
    }
  }

  let paletteEl = null;
  let inputEl = null;
  let listEl = null;
  let selectedIndex = 0;
  let currentCommands = [];
  let filteredCommands = [];

  function createPaletteDOM() {
    if (document.getElementById('aita-command-palette')) return;

    const wrapper = document.createElement('div');
    wrapper.id = 'aita-command-palette';
    wrapper.className = 'command-palette-backdrop';
    wrapper.innerHTML = `
      <div class="command-palette-modal" onclick="event.stopPropagation()">
        <div class="command-palette-header">
          <i data-lucide="search" class="w-4 h-4 text-cyan-400 shrink-0"></i>
          <input type="text" id="cmd-palette-input" placeholder="Tìm kiếm trang, bài nộp, giải thuật AST hoặc lệnh... (Gõ để lọc)" autocomplete="off">
          <kbd class="cmd-kbd">ESC</kbd>
        </div>
        <div class="command-palette-list" id="cmd-palette-list"></div>
        <div class="command-palette-footer">
          <div class="flex items-center gap-3">
            <span><kbd class="cmd-kbd-sm">↑</kbd> <kbd class="cmd-kbd-sm">↓</kbd> Di chuyển</span>
            <span><kbd class="cmd-kbd-sm">↵</kbd> Chọn</span>
          </div>
          <span class="text-cyan-400/80 font-mono">AITA Spotlight Search</span>
        </div>
      </div>
    `;

    wrapper.addEventListener('click', closePalette);
    document.body.appendChild(wrapper);

    paletteEl = wrapper;
    inputEl = document.getElementById('cmd-palette-input');
    listEl = document.getElementById('cmd-palette-list');

    inputEl.addEventListener('input', onSearchInput);
    inputEl.addEventListener('keydown', onKeyDown);

    if (window.lucide) lucide.createIcons();
  }

  function renderList() {
    if (!listEl) return;
    listEl.innerHTML = '';

    if (filteredCommands.length === 0) {
      listEl.innerHTML = `
        <div class="p-6 text-center text-xs text-slate-500 font-mono">
          Không tìm thấy lệnh hoặc bài nộp phù hợp với từ khóa.
        </div>
      `;
      return;
    }

    let currentGroup = '';
    filteredCommands.forEach((cmd, idx) => {
      if (cmd.group !== currentGroup) {
        currentGroup = cmd.group;
        const groupEl = document.createElement('div');
        groupEl.className = 'cmd-group-label';
        groupEl.innerText = currentGroup;
        listEl.appendChild(groupEl);
      }

      const itemEl = document.createElement('div');
      itemEl.className = 'cmd-item ' + (idx === selectedIndex ? 'selected' : '');
      itemEl.innerHTML = `
        <div class="flex items-center gap-2.5">
          <i data-lucide="${cmd.icon}" class="w-4 h-4 text-cyan-400 shrink-0"></i>
          <span class="cmd-title">${cmd.title}</span>
        </div>
        <kbd class="cmd-enter-tag">↵</kbd>
      `;
      itemEl.addEventListener('click', () => {
        executeCommand(cmd);
      });
      itemEl.addEventListener('mouseenter', () => {
        selectedIndex = idx;
        updateSelectionHighlight();
      });
      listEl.appendChild(itemEl);
    });

    if (window.lucide) lucide.createIcons();
  }

  function updateSelectionHighlight() {
    const items = listEl.querySelectorAll('.cmd-item');
    items.forEach((item, idx) => {
      if (idx === selectedIndex) {
        item.classList.add('selected');
        item.scrollIntoView({ block: 'nearest' });
      } else {
        item.classList.remove('selected');
      }
    });
  }

  function onSearchInput(e) {
    const query = e.target.value.toLowerCase().trim();
    if (!query) {
      filteredCommands = [...currentCommands];
    } else {
      filteredCommands = currentCommands.filter(c => 
        c.title.toLowerCase().includes(query) || 
        c.group.toLowerCase().includes(query)
      );
    }
    selectedIndex = 0;
    renderList();
  }

  function onKeyDown(e) {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (filteredCommands.length > 0) {
        selectedIndex = (selectedIndex + 1) % filteredCommands.length;
        updateSelectionHighlight();
      }
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      if (filteredCommands.length > 0) {
        selectedIndex = (selectedIndex - 1 + filteredCommands.length) % filteredCommands.length;
        updateSelectionHighlight();
      }
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (filteredCommands[selectedIndex]) {
        executeCommand(filteredCommands[selectedIndex]);
      }
    } else if (e.key === 'Escape') {
      closePalette();
    }
  }

  function executeCommand(cmd) {
    closePalette();
    cmd.action();
  }

  function openPalette() {
    createPaletteDOM();
    currentCommands = getCommands();
    filteredCommands = [...currentCommands];
    paletteEl.classList.add('active');
    inputEl.value = '';
    selectedIndex = 0;
    renderList();
    setTimeout(() => inputEl.focus(), 50);
  }

  function closePalette() {
    if (paletteEl) {
      paletteEl.classList.remove('active');
    }
  }

  // Keyboard shortcut listener: Ctrl+K, Cmd+K, or /
  window.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
      e.preventDefault();
      if (paletteEl && paletteEl.classList.contains('active')) {
        closePalette();
      } else {
        openPalette();
      }
    } else if (e.key === 'Escape' && paletteEl && paletteEl.classList.contains('active')) {
      closePalette();
    }
  });

  window.openCommandPalette = openPalette;
  window.closeCommandPalette = closePalette;
  window.commandPalette = {
    open: openPalette,
    close: closePalette,
    isOpen: () => !!(paletteEl && paletteEl.classList.contains('active'))
  };
})();
