/**
 * AITA CodeDefend - Copilot Context & Knowledge Base
 * Defines speech scripts and role/page detection matrix
 */
(function (global) {
  const CONTEXT_SCRIPTS = {
    landing: {
      guest: "Xin chào! Tôi là Trợ lý AI của Hệ thống AITA CodeDefend. Tôi có thể hỗ trợ bạn khám phá công nghệ bóc tách cây cú pháp AST và giám sát liêm chính mã nguồn Java. Hãy bấm Đăng nhập để bắt đầu trải nghiệm!",
      lecturer: "Kính chào Thầy Nguyễn Hoàng Hà! Thầy có thể truy cập ngay Bảng điều khiển Giảng viên để quản lý các lớp học và xem đợt đối soát đạo văn mới nhất.",
      student: "Chào bạn Quốc Huy! Hãy truy cập Cổng Sinh Viên để kiểm tra hạn nộp bài tập và trạng thái bài nộp của bạn.",
      admin: "Kính chào Ban Khảo Thí FPT University! Hệ thống AITA CodeDefend đang hoạt động ổn định và sẵn sàng phục vụ công tác thanh tra học thuật."
    },
    login: {
      guest: "Chào mừng bạn đến với Cổng đăng nhập AITA! Bạn có thể sử dụng tài khoản FPT Edu, Google Mail, hoặc các nút chọn nhanh vai trò Giảng viên, Sinh viên và Khảo thí ở cột bên phải để thử nghiệm hệ thống ngay.",
      lecturer: "Thầy đang ở trang Đăng nhập. Thầy có thể bấm Đăng nhập nhanh Giảng viên để tiếp tục phiên làm việc.",
      student: "Chào bạn sinh viên! Hãy đăng nhập bằng email trường để vào cổng nộp bài tập cá nhân nhé.",
      admin: "Chào Ban Khảo thí! Vui lòng xác thực tài khoản để truy cập hệ thống quản trị liêm chính học thuật."
    },
    dashboard: {
      lecturer: "Kính chào Thầy Nguyễn Hoàng Hà! Hệ thống ghi nhận lớp PRJ301 hiện có 3 bài nộp vượt ngưỡng cờ đỏ cảnh báo đạo văn trên 70%. Thầy có thể mở tính năng Quét hàng loạt hoặc Soi vi phạm để xem đối chiếu chi tiết.",
      admin: "Kính chào Ban Khảo Thí FPT University! Báo cáo liêm chính học thuật toàn khóa PRJ301 đã được tổng hợp. Các chỉ số rủi ro và đơn khiếu nại đang chờ quý ban phê duyệt.",
      default: "Chào mừng bạn đến với Bảng Điều Khiển Trung Tâm AITA! Tại đây bạn có thể theo dõi radar quét mã thời gian thực và phân tích xu hướng tương đồng mã nguồn."
    },
    student: {
      student: "Chào bạn Quốc Huy! Bài tập Assignment 1 của bạn đã kiểm định an toàn với độ trùng lặp chỉ 12.4%. Bài tập Assignment 2 đang trong hàng đợi chấm. Đừng quên hạn nộp trước 23 giờ 59 phút Chủ Nhật nhé!",
      default: "Chào bạn sinh viên! Tại Cổng Thông Tin này, bạn có thể tra cứu lịch sử bài nộp, mã băm SHA-256 và gửi đơn giải trình khiếu nại nếu phát hiện sai lệch."
    },
    batch: {
      lecturer: "Đây là Trung tâm Quét AST Hàng loạt. Thầy có thể tải lên tệp nén ZIP chứa toàn bộ bài nộp của lớp. Hệ thống sẽ tự động băm SHA-256 và dựng cây cú pháp để phân tích mức độ tương đồng.",
      default: "Chào mừng đến với Trình Quét Hàng Loạt. Hệ thống phân tích song song đa bài nộp với thuật toán chuẩn hóa token AST và AI Semantic."
    },
    diff: {
      lecturer: "Chào Thầy! Tại giao diện Soi vi phạm AST, các khối lệnh nghi ngờ sao chép giữa hai sinh viên được đánh dấu song song hai cột kèm mã tỷ lệ tương đồng và phân tích ngữ nghĩa từ Gemini AI.",
      default: "Giao diện Soi Vi Phạm AST cho phép đối chiếu mã nguồn song song từng dòng, bóc tách cấu trúc hàm và nhận diện kỹ thuật đổi tên biến tinh vi."
    }
  };

  const FAQS = [
    {
      id: "about",
      icon: "info",
      q: "Hệ thống AITA CodeDefend là gì?",
      a: "Nền tảng kiểm định liêm chính học thuật bằng Cây Cú Pháp AST Java kết hợp mô hình AI Google Gemini để đối soát đạo văn."
    },
    {
      id: "ast",
      icon: "git-merge",
      q: "Công nghệ Chuẩn hóa AST chống gian lận thế nào?",
      a: "Bóc tách cấu trúc cú pháp loại bỏ biến đổi hình thức như đổi tên biến, đảo hàm, chèn khoảng trắng, phát hiện sao chép logic tinh vi."
    },
    {
      id: "sha",
      icon: "hash",
      q: "Tại sao bài nộp bắt buộc có mã băm SHA-256?",
      a: "Đảm bảo tính toàn vẹn của tệp ZIP bài nộp, chống chối bỏ trách nhiệm và ngăn chặn mọi can thiệp sửa đổi bài sau hạn chót."
    },
    {
      id: "appeal",
      icon: "file-text",
      q: "Sinh viên bị cờ đỏ cấm thi thì khiếu nại ở đâu?",
      a: "Truy cập Cổng Sinh Viên, chọn bài nộp bị nghi ngờ và bấm nút Khiếu Nại để nạp bản giải trình kèm minh chứng commit Git."
    }
  ];

  function detectContext() {
    let role = localStorage.getItem('auth_role') || 'guest';
    try {
      const stored = sessionStorage.getItem('google_user');
      if (stored) {
        const u = JSON.parse(stored);
        if (u.role) role = u.role;
      }
    } catch (e) {}

    const path = window.location.pathname.toLowerCase();
    let pageKey = 'landing';
    if (path.includes('login')) pageKey = 'login';
    else if (path.includes('dashboard')) pageKey = 'dashboard';
    else if (path.includes('student-portal') || path.includes('student')) pageKey = 'student';
    else if (path.includes('batch-scanner') || path.includes('batch')) pageKey = 'batch';
    else if (path.includes('diff-inspector') || path.includes('diff')) pageKey = 'diff';

    return { role, pageKey };
  }

  function getMessage() {
    const { role, pageKey } = detectContext();
    const pageObj = CONTEXT_SCRIPTS[pageKey] || CONTEXT_SCRIPTS.landing;
    return pageObj[role] || pageObj.default || pageObj.guest || CONTEXT_SCRIPTS.landing.guest;
  }

  function getRole() {
    return detectContext().role;
  }

  global.AITAContext = { detectContext, getMessage, getRole, FAQS };
})(window);
