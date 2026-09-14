"""
AITA CodeDefend - Realtime Gemini Streaming & Sentence Chunker Pipeline
Module độc lập (< 120 dòng) - Xử lý streamGenerateContent và tách câu song song
"""

from __future__ import annotations

import json
import os
import re
import socket
import urllib.parse
import urllib.request
from typing import Generator, Optional

_orig_getaddrinfo = socket.getaddrinfo
def _patched_getaddrinfo(host, port, family=0, type=0, proto=0, flags=0):
    if host == "generativelanguage.googleapis.com":
        return _orig_getaddrinfo("172.217.114.4", port, family, type, proto, flags)
    return _orig_getaddrinfo(host, port, family, type, proto, flags)
socket.getaddrinfo = _patched_getaddrinfo

def _load_env_keys() -> list[str]:
    keys = []
    for var in ["GEMINI_API_KEY", "GEMINI_API_KEY_BACKUP"]:
        v = os.environ.get(var, "").strip()
        if v and v not in keys:
            keys.append(v)
    env_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), ".env")
    if os.path.exists(env_path):
        try:
            with open(env_path, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if "=" in line and not line.startswith("#"):
                        k, val = line.split("=", 1)
                        k, val = k.strip(), val.strip()
                        if k in ("GEMINI_API_KEY", "GEMINI_API_KEY_BACKUP") and val and val not in keys:
                            keys.append(val)
        except Exception:
            pass
    return keys

KEYS = _load_env_keys()
MODELS = ["gemini-3-flash-preview", "gemini-3.6-flash", "gemini-3.1-flash-lite-preview", "gemini-2.5-flash-lite"]

def stream_gemini(
    message: str,
    role: str = "student",
    context: str = "dashboard",
    file_name: Optional[str] = None,
    file_content: Optional[str] = None,
    image_base64: Optional[str] = None
) -> Generator[str, None, None]:
    parts = []
    sys_prompt = (
        f"Bạn là Trợ lý AI AITA CodeDefend của FPT University (ngữ cảnh: {context}, vai trò: {role}). "
        "Giám sát liêm chính học thuật môn PRJ301, cây AST Java, mã băm SHA-256 bài nộp ZIP. "
        "Hãy phản hồi súc tích, chuyên nghiệp bằng tiếng Việt tự nhiên (1-3 đoạn ngắn)."
    )
    if file_name and file_content:
        parts.append({"text": f"--- Tệp mã nguồn: {file_name} ---\n```\n{file_content}\n```"})
    if image_base64:
        try:
            b64_str = image_base64
            mime_type = "image/png"
            if "," in b64_str:
                header, b64_str = b64_str.split(",", 1)
                m = re.search(r"data:(.*?);base64", header)
                if m:
                    mime_type = m.group(1)
            parts.append({"inline_data": {"mime_type": mime_type, "data": b64_str}})
        except Exception:
            pass
    parts.append({"text": f"{sys_prompt}\n\nNgười dùng: {message}"})

    payload = {
        "contents": [{"parts": parts}],
        "generationConfig": {
            "temperature": 0.7,
            "maxOutputTokens": 1024
        }
    }

    connected = False
    for k in KEYS:
        if not k:
            continue
        for model in MODELS:
            try:
                url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:streamGenerateContent?alt=sse&key={k}"
                req = urllib.request.Request(
                    url,
                    data=json.dumps(payload).encode("utf-8"),
                    headers={"Content-Type": "application/json", "Host": "generativelanguage.googleapis.com"}
                )
                with urllib.request.urlopen(req, timeout=20) as resp:
                    connected = True
                    for raw_line in resp:
                        line = raw_line.decode("utf-8").strip()
                        if not line.startswith("data:"):
                            continue
                        data_str = line[5:].strip()
                        if not data_str:
                            continue
                        try:
                            chunk = json.loads(data_str)
                            parts_chunk = chunk.get("candidates", [{}])[0].get("content", {}).get("parts", [])
                            for p in parts_chunk:
                                text_delta = p.get("text", "")
                                if text_delta:
                                    yield text_delta
                        except Exception:
                            continue
                    return
            except Exception as e:
                print(f"[Gemini Stream {model} failed with key {k[:8]}]:", e)
                continue
    if not connected:
        yield "Hệ thống AITA CodeDefend đã ghi nhận yêu cầu của bạn. Hiện tại máy chủ đang xử lý dữ liệu kiểm định."

def extract_sentences(text_stream: Generator[str, None, None]) -> Generator[tuple[str, str], None, None]:
    """
    Nhận stream token từ Gemini, yield song song:
    ('token', delta) -> Cho UI cập nhật hiệu ứng gõ chữ
    ('sentence', micro_clause) -> Cho ZeroTTS tổng hợp phát âm tức thì (< 400ms)
    """
    buffer = ""
    is_first = True
    for delta in text_stream:
        yield ("token", delta)
        buffer += delta
        # Chunk đầu tiên ngắt siêu sớm (dấu phẩy/chấm với >= 2 từ hoặc 5 từ) để phát âm ngay
        if is_first:
            m = re.search(r'([,;:!?\n]+|\. )', buffer)
            if m:
                idx = m.end()
                clause = buffer[:idx].strip()
                if len(clause.split()) >= 2:
                    yield ("sentence", clause)
                    buffer = buffer[idx:]
                    is_first = False
            elif len(buffer.split()) >= 5:
                words = buffer.split()
                yield ("sentence", " ".join(words[:4]))
                buffer = " ".join(words[4:])
                is_first = False
        else:
            m = re.search(r'([;!?\n]+|\. |,\s*)', buffer)
            if m:
                idx = m.end()
                clause = buffer[:idx].strip()
                if len(clause.split()) >= 4:
                    yield ("sentence", clause)
                    buffer = buffer[idx:]
            elif len(buffer.split()) >= 9:
                words = buffer.split()
                yield ("sentence", " ".join(words[:7]))
                buffer = " ".join(words[7:])
    if buffer.strip():
        yield ("sentence", buffer.strip())
