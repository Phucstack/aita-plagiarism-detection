package com.aita.plagiarism.service;

import com.aita.plagiarism.config.DBContext;
import com.aita.plagiarism.dao.AssignmentDAO;
import com.aita.plagiarism.dao.DataAccessException;
import com.aita.plagiarism.dao.PlagiarismDAO;
import com.aita.plagiarism.dao.SubmissionDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.MatchingBlock;
import com.aita.plagiarism.model.PlagiarismReport;
import com.aita.plagiarism.model.Submission;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lõi Engine tính toán độ tương đồng mã nguồn Java (Deterministic Java Core)
 * Kết hợp:
 * 1. Lexical Tokenization & Identifier Normalization (chuẩn hóa tên biến/hàm)
 * 2. Jaccard Similarity Index trên tập Token N-Gram
 * 3. Normalized Levenshtein Distance trên chuỗi Token
 * 4. Bóc tách vị trí dòng trùng lặp (Matching Blocks)
 */
public class PlagiarismEngineService {

    private final PlagiarismDAO plagiarismDAO = new PlagiarismDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final GeminiPlagiarismService geminiService;

    /** Số lần gọi Gemini tối đa trong một lượt quét. Cấu hình qua GEMINI_MAX_CALLS_PER_SCAN. */
    private static final int DEFAULT_MAX_CALLS_PER_SCAN = 3;

    private static final String LOCAL_ANALYSIS_PREFIX = "Phân tích cục bộ (rule-based):";

    public PlagiarismEngineService() {
        this(new GeminiPlagiarismService());
    }

    /** Constructor để kiểm thử: cho phép tiêm một Gemini service giả. */
    PlagiarismEngineService(GeminiPlagiarismService geminiService) {
        this.geminiService = geminiService;
    }

    private static int maxCallsPerScan() {
        String raw = System.getProperty("GEMINI_MAX_CALLS_PER_SCAN");
        if (raw == null || raw.isBlank()) raw = System.getenv("GEMINI_MAX_CALLS_PER_SCAN");
        if (raw == null || raw.isBlank()) return DEFAULT_MAX_CALLS_PER_SCAN;
        try {
            int value = Integer.parseInt(raw.trim());
            return Math.max(0, Math.min(value, 50));
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_CALLS_PER_SCAN;
        }
    }

    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "true", "false", "null", "String", "List", "ArrayList", "Map", "HashMap"
    ));

    /** Một dòng mã sau chuẩn hóa, kèm số dòng gốc (1-based) trong tệp nguồn. */
    private static final class NormalizedLine {
        String normalized;
        int originalLine;
    }

    /** Pattern tách từ vựng dùng chung cho mọi pha chuẩn hóa. */
    private static final Pattern WORD_PATTERN = Pattern.compile(
            "[a-zA-Z_$][a-zA-Z0-9_$]*|[0-9]+|==|!=|<=|>=|&&|\\|\\||[{}();,\\[\\]=+\\-*/%<>!]");

    /** Các từ khóa điều khiển luồng — loại khỏi nhận diện tên hàm best-effort. */
    private static final Set<String> CONTROL_FLOW_KEYWORDS = new HashSet<>(Arrays.asList(
            "if", "for", "while", "switch", "catch", "do", "try", "else",
            "synchronized", "return", "new", "throw"
    ));

    /** Pattern best-effort nhận diện dòng khai báo method Java (không dựng AST). */
    private static final Pattern METHOD_DECL_PATTERN = Pattern.compile(
            "^\\s*(?:[\\w\\[\\]<>?,]+\\s+)+([a-zA-Z_$][\\w$]*)\\s*\\([^;]*\\)\\s*(?:throws[\\w\\s,.]+)?\\{?\\s*$");

    /**
     * Chuẩn hóa mã nguồn Java: loại bỏ comment, khoảng trắng, và thay thế tên biến
     */
    public String normalizeJavaCode(String rawCode) {
        if (rawCode == null) return "";

        // 1. Loại bỏ comment khối /* ... */ và comment dòng // ...
        String noComments = rawCode.replaceAll("/\\*[^*]*(?:\\*(?!/)[^*]*)*\\*/", " ");
        noComments = noComments.replaceAll("//.*", " ");

        // 2. Tách từ vựng và chuẩn hóa định danh biến/hàm
        return normalizeFragment(noComments, new HashMap<>());
    }

    /**
     * Tách từ vựng một đoạn văn bản và chuẩn hóa định danh thành {@code $ID_n}.
     * Bản đồ định danh được truyền từ ngoài để nhiều đoạn (ví dụ từng dòng của
     * cùng một tệp) dùng chung một cách đánh số nhất quán.
     */
    private static String normalizeFragment(String text, Map<String, String> identifierMap) {
        Matcher matcher = WORD_PATTERN.matcher(text);
        StringBuilder normalized = new StringBuilder();
        while (matcher.find()) {
            String token = matcher.group();
            if (JAVA_KEYWORDS.contains(token) || token.matches("[^a-zA-Z_$].*")) {
                normalized.append(token).append(" ");
            } else {
                // Biến hoặc hàm do người dùng đặt -> chuẩn hóa thành $ID_n
                if (!identifierMap.containsKey(token)) {
                    identifierMap.put(token, "$ID_" + (identifierMap.size() + 1));
                }
                normalized.append(identifierMap.get(token)).append(" ");
            }
        }
        return normalized.toString().trim();
    }

    /**
     * Chuẩn hóa mã nguồn theo từng dòng, giữ ánh xạ về số dòng gốc (1-based).
     * Dùng chung logic bỏ comment/token hóa/đổi tên định danh với {@link #normalizeJavaCode},
     * nhưng áp dụng trên từng dòng để phục vụ bóc tách Matching Block theo tọa độ dòng.
     * Dòng sau chuẩn hóa mà rỗng (dòng trắng hoặc chỉ chứa comment) bị loại khỏi kết quả.
     */
    List<NormalizedLine> normalizeJavaCodeToLines(String rawCode) {
        List<NormalizedLine> result = new ArrayList<>();
        if (rawCode == null) return result;

        // Bỏ comment khối trước vì nó có thể trải dài nhiều dòng; comment dòng xử lý theo từng dòng.
        String noBlockComments = rawCode.replaceAll("/\\*[^*]*(?:\\*(?!/)[^*]*)*\\*/", " ");

        Map<String, String> identifierMap = new HashMap<>();
        String[] lines = noBlockComments.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].replaceAll("//.*", " ");
            String normalized = normalizeFragment(line, identifierMap);
            if (normalized.isEmpty()) continue;
            NormalizedLine nl = new NormalizedLine();
            nl.normalized = normalized;
            nl.originalLine = i + 1;
            result.add(nl);
        }
        return result;
    }

    /**
     * Bóc tách các khối mã trùng lặp (Matching Block) giữa hai tệp bằng thuật toán
     * LCS trên bảng quy hoạch động, tính trên các dòng ĐÃ chuẩn hóa (đổi tên định
     * danh vẫn khớp nhau). Tọa độ block trả về là số dòng GỐC trong từng tệp.
     * Các đoạn khớp liên tiếp trên đường đi LCS được gom thành một block;
     * block ngắn hơn 3 dòng bị loại vì quá nhiễu (một cặp ngoặc, một lệnh return...).
     */
    List<MatchingBlock> computeMatchingBlocks(String codeA, String codeB) {
        List<NormalizedLine> linesA = normalizeJavaCodeToLines(codeA);
        List<NormalizedLine> linesB = normalizeJavaCodeToLines(codeB);
        List<MatchingBlock> blocks = new ArrayList<>();
        if (linesA.isEmpty() || linesB.isEmpty()) return blocks;

        int n = linesA.size();
        int m = linesB.size();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                if (linesA.get(i).normalized.equals(linesB.get(j).normalized)) {
                    dp[i][j] = dp[i + 1][j + 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }

        String[] rawLinesA = codeA.split("\\R", -1);
        int i = 0, j = 0;
        while (i < n && j < m) {
            if (linesA.get(i).normalized.equals(linesB.get(j).normalized)) {
                int startI = i, startJ = j;
                while (i < n && j < m
                        && linesA.get(i).normalized.equals(linesB.get(j).normalized)) {
                    i++;
                    j++;
                }
                int blockLen = i - startI;
                if (blockLen >= 3) {
                    MatchingBlock block = new MatchingBlock();
                    block.setFunctionName(findEnclosingFunctionName(
                            rawLinesA, linesA.get(startI).originalLine));
                    block.setStudentAStartLine(linesA.get(startI).originalLine);
                    block.setStudentAEndLine(linesA.get(i - 1).originalLine);
                    block.setStudentBStartLine(linesB.get(startJ).originalLine);
                    block.setStudentBEndLine(linesB.get(j - 1).originalLine);
                    block.setMatchedCodeSnippet(extractOriginalSnippet(
                            rawLinesA, linesA.get(startI).originalLine, linesA.get(i - 1).originalLine));
                    blocks.add(block);
                }
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        return blocks;
    }

    /**
     * Cắt đoạn mã GỐC phía A tương ứng với một block (từ dòng start đến dòng end,
     * 1-based, cả hai đầu đều lấy), giới hạn tối đa 500 ký tự.
     */
    private static String extractOriginalSnippet(String[] rawLines, int startLine, int endLine) {
        StringBuilder sb = new StringBuilder();
        for (int line = startLine; line <= endLine && line <= rawLines.length; line++) {
            sb.append(rawLines[line - 1]);
            if (line < endLine) sb.append('\n');
            if (sb.length() > 500) break;
        }
        String snippet = sb.toString();
        return snippet.length() > 500 ? snippet.substring(0, 500) : snippet;
    }

    /**
     * Best-effort tìm tên hàm bao chứa một dòng: quét ngược từ dòng đó lên trên,
     * lấy dòng khai báo method gần nhất khớp {@link #METHOD_DECL_PATTERN}.
     * Đây KHÔNG phải parser AST — khai báo method trải trên nhiều dòng hoặc lồng
     * trong class nặc danh có thể nhận diện sai; khi đó trả về "unknown".
     */
    private static String findEnclosingFunctionName(String[] rawLines, int lineNumber) {
        for (int k = Math.min(lineNumber, rawLines.length) - 1; k >= 0; k--) {
            String line = rawLines[k].trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("*")
                    || line.startsWith("/*")) {
                continue;
            }
            Matcher matcher = METHOD_DECL_PATTERN.matcher(line);
            if (matcher.matches()) {
                String name = matcher.group(1);
                if (!CONTROL_FLOW_KEYWORDS.contains(name) && !"class".equals(name)
                        && !"interface".equals(name) && !"enum".equals(name)) {
                    return name;
                }
            }
        }
        return "unknown";
    }

    /**
     * Tính toán chỉ số Jaccard Similarity trên tập N-Gram (k = 3)
     */
    public double calculateJaccardIndex(String normCodeA, String normCodeB) {
        if (normCodeA.isEmpty() && normCodeB.isEmpty()) return 100.0;
        if (normCodeA.isEmpty() || normCodeB.isEmpty()) return 0.0;

        String[] tokensA = normCodeA.split("\\s+");
        String[] tokensB = normCodeB.split("\\s+");

        Set<String> ngramsA = extractNgrams(tokensA, 3);
        Set<String> ngramsB = extractNgrams(tokensB, 3);

        if (ngramsA.isEmpty() && ngramsB.isEmpty()) return 100.0;
        if (ngramsA.isEmpty() || ngramsB.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(ngramsA);
        intersection.retainAll(ngramsB);

        Set<String> union = new HashSet<>(ngramsA);
        union.addAll(ngramsB);

        return (double) intersection.size() / union.size() * 100.0;
    }

    /**
     * Tính Normalized Levenshtein Distance
     */
    /**
     * Khoảng cách Levenshtein chuẩn hóa, tính trên <b>chuỗi token</b> (đúng như SRS
     * FE-04.1 mô tả), không phải trên từng ký tự.
     *
     * Lý do: với hai tệp ~500 ký tự, Levenshtein theo ký tự tốn ~250.000 ô bảng cho
     * mỗi cặp; theo token (~100 token) chỉ tốn ~10.000 ô — nhanh hơn khoảng 25 lần mà
     * ý nghĩa so sánh vẫn giữ nguyên (đều là khoảng cách chỉnh sửa trên dãy đã chuẩn hóa).
     */
    public double calculateNormalizedLevenshtein(String strA, String strB) {
        if (strA.equals(strB)) return 100.0;
        String[] a = strA.isBlank() ? new String[0] : strA.trim().split("\\s+");
        String[] b = strB.isBlank() ? new String[0] : strB.trim().split("\\s+");
        int maxLen = Math.max(a.length, b.length);
        if (maxLen == 0) return 100.0;

        int distance = computeLevenshteinDistance(a, b);
        double similarity = (1.0 - (double) distance / maxLen) * 100.0;
        return Math.max(0.0, similarity);
    }

    /**
     * Điểm số tương đồng tổng hợp: 60% Jaccard + 40% Levenshtein
     */
    public double calculateOverallSimilarity(String rawCodeA, String rawCodeB) {
        String normA = normalizeJavaCode(rawCodeA);
        String normB = normalizeJavaCode(rawCodeB);

        // Tệp rỗng hoặc chỉ chứa comment không có gì để so sánh. Nếu không chặn,
        // hai tệp rỗng sẽ cho Jaccard = Levenshtein = 100% và bị gắn HIGH_RISK oan.
        if (normA.isBlank() || normB.isBlank()) return 0.0;

        double jaccard = calculateJaccardIndex(normA, normB);
        double levenshtein = calculateNormalizedLevenshtein(normA, normB);

        double overall = 0.6 * jaccard + 0.4 * levenshtein;
        return Math.round(overall * 100.0) / 100.0;
    }

    /** Kết quả của một lượt quét. */
    public static final class ScanResult {
        private final int reportsCreated;
        private final int skippedPairs;

        public ScanResult(int reportsCreated, int skippedPairs) {
            this.reportsCreated = reportsCreated;
            this.skippedPairs = skippedPairs;
        }

        public int getReportsCreated() { return reportsCreated; }

        /** Số cặp bị bỏ qua vì không đọc được nội dung file của một trong hai bài nộp. */
        public int getSkippedPairs() { return skippedPairs; }
    }

    /**
     * Quét toàn bộ bài nộp của Assignment và lưu báo cáo vào CSDL.
     *
     * Toàn bộ phần ghi được bọc trong một giao dịch duy nhất và giữ khóa
     * UPDLOCK/HOLDLOCK trên bản ghi Assignments để hai lượt quét không chạy xen kẽ.
     * Cặp nào không đọc được nội dung file sẽ bị bỏ qua (không tạo báo cáo) thay vì
     * được tính điểm trên nội dung giả.
     */
    public ScanResult scanAssignment(int assignmentId) {
        List<Submission> submissions = submissionDAO.getSubmissionsByAssignment(assignmentId);
        if (submissions.size() < 2) {
            return new ScanResult(0, 0);
        }

        Assignment assignment = assignmentDAO.getAssignmentById(assignmentId);
        double threshold = (assignment != null && assignment.getSimilarityThreshold() > 0)
                ? assignment.getSimilarityThreshold() : 75.0;

        // ------------------------------------------------------------------
        // PHA 1 — TÍNH TOÁN, KHÔNG GIỮ KẾT NỐI CSDL
        // Đọc tệp và chạy thuật toán chiếm phần lớn thời gian quét. Nếu làm điều này
        // trong khi đang mượn một kết nối, kết nối đó bị chiếm dụng vô ích: đo thực tế
        // 16 lượt quét đồng thời với pool 10 kết nối có request chờ quá 10 giây và
        // nhận HTTP 503. Tách pha giúp giảm mạnh thời gian giữ kết nối.
        // ------------------------------------------------------------------
        Map<Integer, String> contents = new HashMap<>();
        for (Submission s : submissions) {
            contents.put(s.getSubmissionId(), readSubmissionContent(s));
        }

        List<PairOutcome> outcomes = new ArrayList<>();
        Set<Integer> flagged = new HashSet<>();
        int skipped = 0;

        for (int i = 0; i < submissions.size(); i++) {
            for (int j = i + 1; j < submissions.size(); j++) {
                Submission subA = submissions.get(i);
                Submission subB = submissions.get(j);

                String codeA = contents.get(subA.getSubmissionId());
                String codeB = contents.get(subB.getSubmissionId());
                if (codeA == null || codeB == null || codeA.isBlank() || codeB.isBlank()) {
                    // Không đọc được nội dung thật, hoặc tệp rỗng/chỉ có comment:
                    // bỏ qua cặp này, tuyệt đối không thay bằng nội dung giả
                    // cũng như không tạo báo cáo từ nội dung không có gì để so sánh.
                    skipped++;
                    continue;
                }

                double score = calculateOverallSimilarity(codeA, codeB);
                String riskLevel = determineRiskLevel(score, threshold);
                outcomes.add(new PairOutcome(subA, subB, codeA, codeB, score, riskLevel));
                if ("HIGH_RISK".equals(riskLevel)) {
                    flagged.add(subA.getSubmissionId());
                    flagged.add(subB.getSubmissionId());
                }
            }
        }

        // ------------------------------------------------------------------
        // PHA 2 — GHI DỮ LIỆU trong một giao dịch duy nhất
        // ------------------------------------------------------------------
        List<HighRiskPair> highRiskPairs = new ArrayList<>();
        int reportsGenerated;

        try (Connection conn = DBContext.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                lockAssignment(conn, assignmentId);
                // Xóa báo cáo cũ của bài tập này trong cùng giao dịch.
                plagiarismDAO.clearReportsByAssignment(assignmentId, conn);

                reportsGenerated = 0;

                for (PairOutcome outcome : outcomes) {
                    PlagiarismReport report = new PlagiarismReport();
                    report.setAssignmentId(assignmentId);
                    report.setSubmissionAId(outcome.subA.getSubmissionId());
                    report.setSubmissionBId(outcome.subB.getSubmissionId());
                    report.setSimilarityScore(outcome.score);
                    report.setRiskLevel(outcome.riskLevel);
                    report.setAiAnalysisSummary(localSummary(outcome.score,
                            outcome.subA.getFileName(), outcome.subB.getFileName(), outcome.riskLevel));

                    int reportId = plagiarismDAO.createReport(report, conn);
                    reportsGenerated++;

                    // Bóc tách LCS thật khi tương đồng đáng chú ý (>= 40%).
                    // Tọa độ dòng là số dòng GỐC trong từng tệp; tối đa 5 block/cặp
                    // để tránh phình bảng MatchingBlocks với các cặp giống hệt nhau.
                    if (outcome.score >= 40.0) {
                        List<MatchingBlock> blocks = computeMatchingBlocks(outcome.codeA, outcome.codeB);
                        for (MatchingBlock block : blocks.subList(0, Math.min(5, blocks.size()))) {
                            block.setReportId(reportId);
                            block.setVariableRenamingNotes("Khối mã khớp sau chuẩn hóa định danh (đổi tên biến/hàm vẫn khớp), độ tương đồng cặp " + outcome.score + "%");
                            plagiarismDAO.createMatchingBlock(block, conn);
                        }
                    }

                    if ("HIGH_RISK".equals(outcome.riskLevel)) {
                        highRiskPairs.add(new HighRiskPair(reportId, outcome.score, outcome.codeA, outcome.codeB));
                    }
                }

                // Cập nhật trạng thái HAI câu lệnh cho toàn bộ bài tập, thay vì 2 câu
                // cho mỗi cặp (1.225 cặp = 2.450 câu lệnh).
                submissionDAO.markSubmissionsByOutcome(assignmentId, flagged, conn);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DataAccessException(e);
            } finally {
                try {
                    conn.setAutoCommit(autoCommit);
                } catch (Exception ignored) {
                    // Không làm thay đổi kết quả chính.
                }
            }
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }

        // Làm giàu bằng Gemini SAU khi đã commit: không giữ khóa CSDL trong lúc chờ mạng.
        enrichWithGemini(highRiskPairs);

        return new ScanResult(reportsGenerated, skipped);
    }

    /** Giữ khóa hàng Assignments để ngăn hai lượt quét đồng thời trên cùng bài tập. */
    private void lockAssignment(Connection conn, int assignmentId) {
        String sql = "SELECT assignment_id FROM Assignments WITH (UPDLOCK, HOLDLOCK) WHERE assignment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    /** Kết quả tính toán của một cặp bài nộp, trước khi ghi vào CSDL. */
    private static final class PairOutcome {
        final Submission subA;
        final Submission subB;
        final String codeA;
        final String codeB;
        final double score;
        final String riskLevel;

        PairOutcome(Submission subA, Submission subB, String codeA, String codeB,
                    double score, String riskLevel) {
            this.subA = subA;
            this.subB = subB;
            this.codeA = codeA;
            this.codeB = codeB;
            this.score = score;
            this.riskLevel = riskLevel;
        }
    }

    private static final class HighRiskPair {
        final int reportId;
        final double score;
        final String codeA;
        final String codeB;

        HighRiskPair(int reportId, double score, String codeA, String codeB) {
            this.reportId = reportId;
            this.score = score;
            this.codeA = codeA;
            this.codeB = codeB;
        }
    }

    /**
     * Gọi Gemini cho tối đa {@code GEMINI_MAX_CALLS_PER_SCAN} cặp HIGH_RISK có điểm cao nhất.
     * Khi thiếu cấu hình hoặc lỗi, bản ghi giữ nguyên nhận định cục bộ đã gắn nhãn.
     */
    private void enrichWithGemini(List<HighRiskPair> pairs) {
        if (pairs.isEmpty() || geminiService == null || !geminiService.isConfigured()) {
            return;
        }
        int cap = maxCallsPerScan();
        if (cap <= 0) return;

        pairs.sort((a, b) -> Double.compare(b.score, a.score));
        int limit = Math.min(cap, pairs.size());
        for (int i = 0; i < limit; i++) {
            HighRiskPair pair = pairs.get(i);
            try {
                String response = geminiService.analyzeCodeSimilarity(pair.codeA, pair.codeB);
                GeminiPlagiarismService.GeminiAnalysis analysis = GeminiPlagiarismService.parseAnalysis(response);
                if (analysis != null) {
                    plagiarismDAO.updateAnalysisSummary(pair.reportId,
                            "[Gemini] " + GeminiPlagiarismService.renderAnalysis(analysis));
                }
                // JSON malformed hoặc fallback gắn nhãn: giữ nguyên nhận định cục bộ (rule-based).
            } catch (Exception e) {
                // Mất kết nối/quota: giữ nguyên nhận định cục bộ, không làm gãy lượt quét.
            }
        }
    }

    /**
     * Đọc nội dung thật của bài nộp.
     *
     * @return nội dung tệp, hoặc {@code null} nếu không đọc được — người gọi phải bỏ qua cặp này.
     */
    private String readSubmissionContent(Submission sub) {
        File file = com.aita.plagiarism.config.StorageConfig.resolve(sub.getFilePath());
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private String determineRiskLevel(double score, double threshold) {
        if (score >= threshold) return "HIGH_RISK";
        if (score >= 50.0) return "MEDIUM";
        if (score >= 30.0) return "LOW";
        return "SAFE";
    }

    /**
     * Nhận định cục bộ (rule-based), được gắn nhãn rõ ràng.
     *
     * Nguyên tắc: không một văn bản nào được mang chữ "AI"/"Gemini" nếu nó không thực sự
     * do mô hình sinh ra. Khi Gemini được gọi thành công, bản ghi sẽ được cập nhật với
     * tiền tố {@code [Gemini]}.
     */
    private String localSummary(double score, String fileA, String fileB, String riskLevel) {
        String body;
        if ("HIGH_RISK".equals(riskLevel)) {
            body = String.format("mức độ tương đồng báo động đỏ (%.1f%%) giữa %s và %s. Dấu hiệu đổi tên định danh biến/hàm và tái cấu trúc khối lệnh.",
                    score, fileA, fileB);
        } else if ("MEDIUM".equals(riskLevel)) {
            body = String.format("tương đồng mức trung bình (%.1f%%). Một số hàm tiện ích và cấu trúc vòng lặp có mẫu hình tương tự.", score);
        } else {
            body = String.format("độ tương đồng an toàn (%.1f%%). Logic thuật toán mang tính độc lập cao.", score);
        }
        return LOCAL_ANALYSIS_PREFIX + " " + body
                + " Đây không phải kết quả từ mô hình ngôn ngữ lớn.";
    }

    private Set<String> extractNgrams(String[] tokens, int n) {
        Set<String> ngrams = new HashSet<>();
        if (tokens.length < n) {
            ngrams.add(String.join(" ", tokens));
            return ngrams;
        }
        for (int i = 0; i <= tokens.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                sb.append(tokens[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }
        return ngrams;
    }

    /** Khoảng cách Levenshtein trên hai dãy token, dùng một hàng chi phí (O(n) bộ nhớ). */
    private int computeLevenshteinDistance(String[] a, String[] b) {
        if (a.length < b.length) {
            String[] swap = a;
            a = b;
            b = swap;
        }
        int[] costs = new int[b.length + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;

        for (int i = 1; i <= a.length; i++) {
            costs[0] = i;
            int nw = i - 1;
            String ai = a[i - 1];
            for (int j = 1; j <= b.length; j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        ai.equals(b[j - 1]) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length];
    }
}
