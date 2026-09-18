<%@ page pageEncoding="UTF-8" %>
    </main>
    <footer class="border-t border-white/5 bg-[#08080e] py-6 text-center text-xs text-slate-500">
        <div class="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
            <div>&copy; 2026 AITA Ecosystem - SE20C Group 7: AI Plagiarism & Code Similarity Detection.</div>
            <div class="flex items-center gap-4 text-slate-400">
                <span>PRJ301 Capstone</span>
                <span>•</span>
                <span>Deterministic similarity engine + optional Gemini analysis</span>
            </div>
        </div>
    </footer>
    <!-- Initialize Lucide Icons & Custom Scripts -->
    <script>
        lucide.createIcons();
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/motion.js"></script>
    <!-- AITA Realtime AI Copilot (ZeroTTS Voice Engine) -->
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot-context.js?v=2.0"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot-voice.js?v=2.0"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot-chat.js?v=2.0"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot-observer.js?v=2.0"></script>
    <script src="${pageContext.request.contextPath}/assets/js/aita-copilot.js?v=2.0"></script>
</body>
</html>
