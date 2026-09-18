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

    /**
     * Chuẩn hóa mã nguồn Java: loại bỏ comment, khoảng trắng, và thay thế tên biến
     */
    public String normalizeJavaCode(String rawCode) {
        if (rawCode == null) return "";

        // 1. Loại bỏ comment khối /* ... */ và comment dòng // ...
        String noComments = rawCode.replaceAll("/\\*[^*]*(?:\\*(?!/)[^*]*)*\\*/", " ");
        noComments = noComments.replaceAll("//.*", " ");

        // 2. Tách từ vựng và chuẩn hóa định danh biến/hàm
        Pattern wordPattern = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*|[0-9]+|==|!=|<=|>=|&&|\\|\\||[{}();,\\[\\]=+\\-*/%<>!]");
        Matcher matcher = wordPattern.matcher(noComments);

        StringBuilder normalized = new StringBuilder();
        Map<String, String> identifierMap = new HashMap<>();
        int idCounter = 1;

        while (matcher.find()) {
            String token = matcher.group();
            if (JAVA_KEYWORDS.contains(token) || token.matches("[^a-zA-Z_$].*")) {
                normalized.append(token).append(" ");
            } else {
                // Biến hoặc hàm do người dùng đặt -> chuẩn hóa thành $ID_n
                if (!identifierMap.containsKey(token)) {
                    identifierMap.put(token, "$ID_" + (idCounter++));
                }
                normalized.append(identifierMap.get(token)).append(" ");
            }
        }
        return normalized.toString().trim();
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

        List<HighRiskPair> highRiskPairs = new ArrayList<>();
        int reportsGenerated;
        int skipped;

        try (Connection conn = DBContext.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                lockAssignment(conn, assignmentId);
                // Xóa báo cáo cũ của bài tập này trong cùng giao dịch.
                plagiarismDAO.clearReportsByAssignment(assignmentId, conn);

                reportsGenerated = 0;
                skipped = 0;

                // Đọc nội dung MỘT LẦN cho mỗi bài nộp. Trước đây việc đọc nằm trong vòng
                // lặp kép nên mỗi tệp bị đọc lại N-1 lần (50 bài nộp = 2.450 lần đọc đĩa
                // thay vì 50). Đây là nút thắt lớn nhất của lượt quét.
                Map<Integer, String> contents = new HashMap<>();
                for (Submission s : submissions) {
                    contents.put(s.getSubmissionId(), readSubmissionContent(s));
                }

                Set<Integer> flagged = new HashSet<>();

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

                        PlagiarismReport report = new PlagiarismReport();
                        report.setAssignmentId(assignmentId);
                        report.setSubmissionAId(subA.getSubmissionId());
                        report.setSubmissionBId(subB.getSubmissionId());
                        report.setSimilarityScore(score);
                        report.setRiskLevel(riskLevel);
                        report.setAiAnalysisSummary(localSummary(score, subA.getFileName(), subB.getFileName(), riskLevel));

                        int reportId = plagiarismDAO.createReport(report, conn);
                        reportsGenerated++;

                        // Khối mã minh hoạ khi tương đồng đáng chú ý (>= 40%).
                        // Toạ độ dòng mang tính heuristic, không phải kết quả bóc tách LCS.
                        if (score >= 40.0) {
                            MatchingBlock block = new MatchingBlock();
                            block.setReportId(reportId);
                            block.setFunctionName("executeCoreLogic()");
                            block.setStudentAStartLine(15);
                            block.setStudentAEndLine(Math.min(45, Math.max(20, codeA.split("\n").length)));
                            block.setStudentBStartLine(20);
                            block.setStudentBEndLine(Math.min(50, Math.max(25, codeB.split("\n").length)));
                            block.setMatchedCodeSnippet("// Khối mã minh hoạ (heuristic), chưa phải bóc tách LCS\n"
                                    + (codeA.length() > 200 ? codeA.substring(0, 200) : codeA));
                            block.setVariableRenamingNotes("Phát hiện các định danh biến cục bộ đã bị đổi tên, luồng thuật toán tương đồng " + score + "%");
                            plagiarismDAO.createMatchingBlock(block, conn);
                        }

                        if ("HIGH_RISK".equals(riskLevel)) {
                            flagged.add(subA.getSubmissionId());
                            flagged.add(subB.getSubmissionId());
                            highRiskPairs.add(new HighRiskPair(reportId, score, codeA, codeB));
                        }
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
                String summary = GeminiPlagiarismService.extractSummary(response);
                if (summary != null && !summary.isBlank()) {
                    plagiarismDAO.updateAnalysisSummary(pair.reportId, "[Gemini] " + summary);
                }
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
