# -*- coding: utf-8 -*-
import urllib.request
import urllib.parse
import json
import time
import sys

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

ZEROTTS_URL = 'http://127.0.0.1:8008'

cases = [
    ('Technical Terms', 'Hàm public static void main, thuật toán Jaccard Similarity, cây Abstract Syntax Tree AST.')
]

print("=== TEST ZEROTTS PRONUNCIATION & PHONETICS ===")
for name, text in cases:
    t0 = time.time()
    url = f"{ZEROTTS_URL}/tts/stream?text={urllib.parse.quote(text)}&voice=maichi"
    req = urllib.request.Request(url)
    with urllib.request.urlopen(req, timeout=30) as resp:
        audio_bytes = resp.read()
        dur = time.time() - t0
        ct = resp.headers.get("Content-Type")
        print(f"[{name}] OK! {len(audio_bytes):,} bytes | Type: {ct} | Time: {dur:.2f}s")

print("\n=== TEST CHAT WITH FILE & IMAGE ===")
chat_payload = {
    "message": "Kiểm tra tệp mã nguồn này",
    "role": "student",
    "file_name": "Calculator.java",
    "file_content": "public class Calculator { public int add(int a, int b) { return a + b; } }"
}
req = urllib.request.Request(
    f"{ZEROTTS_URL}/api/chat",
    data=json.dumps(chat_payload).encode("utf-8"),
    headers={"Content-Type": "application/json"}
)
with urllib.request.urlopen(req, timeout=10) as resp:
    res = json.loads(resp.read().decode("utf-8"))
    print("Reply preview:", res.get("reply")[:80], "...")
    print("Voice summary:", res.get("voice_summary"))
