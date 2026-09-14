"""
AITA CodeDefend - Advanced ZeroTTS Voice & Multimodal AI Copilot Service
Cung cấp API TTS ZeroTTS tiếng Việt tối ưu ngắt câu (chunking) và Endpoint Chat Multimodal
Port mặc định: 8008
"""

from __future__ import annotations

import hashlib
import io
import json
import os
import re
import socket
import sys
import threading
import time
import urllib.parse
import urllib.request
from typing import Optional

sys.stdout.reconfigure(encoding="utf-8")

_orig_getaddrinfo = socket.getaddrinfo
def _patched_getaddrinfo(host, port, family=0, type=0, proto=0, flags=0):
    if host == "generativelanguage.googleapis.com":
        return _orig_getaddrinfo("172.217.114.4", port, family, type, proto, flags)
    return _orig_getaddrinfo(host, port, family, type, proto, flags)
socket.getaddrinfo = _patched_getaddrinfo

import numpy as np
import soundfile as sf
import uvicorn
from fastapi import FastAPI, HTTPException, Query, Response
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

tools_dir = os.path.dirname(os.path.abspath(__file__))
if tools_dir not in sys.path:
    sys.path.insert(0, tools_dir)
from gemini_stream import stream_gemini, extract_sentences

zerotts_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "ZeroTTS", "src"))
if os.path.exists(zerotts_path):
    sys.path.insert(0, zerotts_path)
from zerotts import ZeroTTS, normalize_vi_text
from zerotts.audio import concat_with_silence
from zerotts.chunking import chunk_text, clean_segment_punctuation, normalize_punctuation

app = FastAPI(
    title="AITA CodeDefend - Voice & AI Copilot Service",
    description="ZeroTTS Speech Engine & Multimodal Assistant for PRJ301",
    version="2.5.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

tts_instance: Optional[ZeroTTS] = None
SAMPLE_RATE = 48000
DEFAULT_VOICE = "maichi"

# Từ điển chuẩn hóa phát âm thuật ngữ CNTT cho tiếng Việt
TECH_PHONETICS = [
    (r"\bAST\b", "A S T"), (r"\bSHA-?256\b", "S H A hai trăm năm mươi sáu"), (r"\bPRJ301\b", "P R J ba không một"),
    (r"\bPRJ30x\b", "P R J ba không ích"), (r"\bJava\b", "Ja va"), (r"\bJWT\b", "J W T"), (r"\bSQL\b", "S Q L"),
    (r"\bJSON\b", "J son"), (r"\bAPI\b", "A P I"), (r"\bURL\b", "U R L"), (r"\bOAuth\b", "O Auth"), (r"\bZIP\b", "díp"),
    (r"\bMVC2?\b", "M V C"), (r"\bDAO\b", "D A O"), (r"\bUI/UX\b", "U I U X"), (r"\b3NF\b", "ba N F")
]

def preprocess_phonetics(text: str) -> str:
    cleaned = text
    for pattern, repl in TECH_PHONETICS:
        cleaned = re.sub(pattern, repl, cleaned, flags=re.IGNORECASE)
    return cleaned

def get_tts() -> ZeroTTS:
    global tts_instance
    if tts_instance is None:
        print("[ZeroTTS] Đang nạp mô hình ZeroTTS vào RAM...")
        t0 = time.time()
        tts_instance = ZeroTTS.from_pretrained("zeroweight-ai/ZeroTTS", intra_op_num_threads=4)
        print(f"[ZeroTTS] Nạp mô hình thành công trong {time.time() - t0:.2f}s!")
    return tts_instance

AUDIO_CACHE: dict[str, bytes] = {}
SYNTH_LOCK = threading.Lock()
SYNTH_EVENTS: dict[str, threading.Event] = {}

def get_cache_key(text: str, voice: str) -> str:
    cleaned = re.sub(r"\s+", " ", text.strip().lower())
    return f"{voice}:{hashlib.md5(cleaned.encode('utf-8')).hexdigest()}"

def prewarm_cache():
    tts = get_tts()
    warmup_texts = [
        "Xin chào,",
        "Chào bạn,",
        "Chào Thầy,",
        "Dạ vâng,",
        "Xin chào! Tôi là Trợ lý AI của Hệ thống AITA CodeDefend.",
        "Chào Thầy Nguyễn Hoàng Hà! Hệ thống ghi nhận lớp PRJ301 có bài nộp vượt ngưỡng cờ đỏ.",
        "Chào bạn Quốc Huy! Hãy truy cập Cổng Sinh Viên để kiểm tra hạn nộp bài tập."
    ]
    for text in warmup_texts:
        key = get_cache_key(text, DEFAULT_VOICE)
        if key not in AUDIO_CACHE:
            try:
                AUDIO_CACHE[key] = synthesize_audio_data(text, voice=DEFAULT_VOICE, use_cache=False)
            except Exception:
                pass

@app.on_event("startup")
def startup_event():
    get_tts()
    threading.Thread(target=prewarm_cache, daemon=True).start()

@app.get("/health")
def health_check():
    tts = get_tts()
    return {
        "status": "online",
        "system": "AITA CodeDefend Voice & Copilot Engine",
        "cache_size": len(AUDIO_CACHE),
        "sample_rate": SAMPLE_RATE,
        "default_voice": DEFAULT_VOICE,
        "voices": tts.list_voices()
    }

def synthesize_audio_data(text: str, voice: str = DEFAULT_VOICE, cfg: float = 1.0, use_cache: bool = True) -> bytes:
    key = get_cache_key(text, voice)
    if use_cache and key in AUDIO_CACHE:
        return AUDIO_CACHE[key]

    with SYNTH_LOCK:
        if use_cache and key in AUDIO_CACHE:
            return AUDIO_CACHE[key]
        if key in SYNTH_EVENTS:
            ev = SYNTH_EVENTS[key]
            must_synth = False
        else:
            ev = threading.Event()
            SYNTH_EVENTS[key] = ev
            must_synth = True

    if not must_synth:
        ev.wait(timeout=12.0)
        return AUDIO_CACHE.get(key, b"")

    try:
        tts = get_tts()
        prepared = preprocess_phonetics(text.strip())
        norm_text = normalize_vi_text(prepared)
        formatted = normalize_punctuation(norm_text)
        
        raw_segments = chunk_text(formatted, max_chunk_sec=8.0)
        segments = [clean_segment_punctuation(s) for s in raw_segments if s.strip()]
        if not segments:
            segments = [clean_segment_punctuation(norm_text)]

        audio_chunks = []
        for seg in segments:
            seg_audio = tts.synthesize(
                seg, voice=voice, cfg_scale=cfg,
                audio_temperature=0.8, audio_repetition_penalty=1.2
            )
            audio_chunks.append(seg_audio)

        if len(audio_chunks) > 1:
            final_audio = concat_with_silence(audio_chunks, silence_sec=0.15, sample_rate=SAMPLE_RATE)
        else:
            final_audio = audio_chunks[0]

        buf = io.BytesIO()
        sf.write(buf, final_audio.squeeze(), SAMPLE_RATE, format="WAV", subtype="PCM_16")
        buf.seek(0)
        wav_bytes = buf.getvalue()
        if len(AUDIO_CACHE) < 500:
            AUDIO_CACHE[key] = wav_bytes
        return wav_bytes
    finally:
        with SYNTH_LOCK:
            ev.set()
            SYNTH_EVENTS.pop(key, None)

@app.get("/tts/stream")
def stream_tts(
    text: str = Query(..., description="Nội dung cần đọc"),
    voice: str = Query(DEFAULT_VOICE, description="ID giọng đọc"),
    cfg: float = Query(1.0, description="CFG Scale")
):
    if not text.strip():
        raise HTTPException(status_code=400, detail="Văn bản rỗng")
    try:
        wav_bytes = synthesize_audio_data(text, voice=voice, cfg=cfg)
        return Response(
            content=wav_bytes,
            media_type="audio/wav",
            headers={
                "Content-Disposition": "inline; filename=aita_speech.wav",
                "Cache-Control": "no-cache",
                "Content-Length": str(len(wav_bytes)),
                "Access-Control-Allow-Origin": "*"
            }
        )
    except Exception as e:
        print(f"[ZeroTTS Error] {e}")
        raise HTTPException(status_code=500, detail=str(e))

# ==============================================================================
# MULTIMODAL CHAT ENDPOINT
# ==============================================================================
class ChatRequest(BaseModel):
    message: str
    role: Optional[str] = "guest"
    context: Optional[str] = "dashboard"
    file_name: Optional[str] = None
    file_content: Optional[str] = None
    image_base64: Optional[str] = None

def call_gemini(req: ChatRequest) -> tuple[str, str]:
    tokens = list(stream_gemini(req.message, req.role or "student", req.context or "dashboard", req.file_name, req.file_content, req.image_base64))
    reply = "".join(tokens).strip()
    sentences = [s.strip() for s in re.split(r'[.!?\n]+', reply) if len(s.strip().split()) >= 3]
    v_sum = re.sub(r"[*_#`]", "", sentences[0] if sentences else reply[:80]).strip()
    return reply, v_sum

@app.post("/api/chat")
def chat_endpoint(req: ChatRequest):
    reply, voice_summary = call_gemini(req)
    if voice_summary:
        threading.Thread(target=lambda: synthesize_audio_data(voice_summary, voice=DEFAULT_VOICE), daemon=True).start()
    return {"reply": reply, "voice_summary": voice_summary}

@app.post("/api/chat/stream")
def chat_stream_endpoint(req: ChatRequest):
    def event_generator():
        token_gen = stream_gemini(
            req.message, req.role or "student", req.context or "dashboard",
            req.file_name, req.file_content, req.image_base64
        )
        for kind, val in extract_sentences(token_gen):
            if kind == "token":
                data = json.dumps({"type": "text", "delta": val}, ensure_ascii=False)
                yield f"data: {data}\n\n"
            elif kind == "sentence":
                clean_sent = re.sub(r"[*_#`]", "", val).strip()
                if clean_sent:
                    threading.Thread(
                        target=lambda s=clean_sent: synthesize_audio_data(s, voice=DEFAULT_VOICE),
                        daemon=True
                    ).start()
                    data = json.dumps({
                        "type": "audio",
                        "sentence": clean_sent,
                        "url": f"/tts/stream?text={urllib.parse.quote(clean_sent)}&voice={DEFAULT_VOICE}"
                    }, ensure_ascii=False)
                    yield f"data: {data}\n\n"
        yield f"data: {json.dumps({'type': 'done'})}\n\n"

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "Connection": "keep-alive", "X-Accel-Buffering": "no"}
    )

if __name__ == "__main__":
    print("=" * 70)
    print("   AITA CODEDEFEND - ZEROTTS VOICE & CHAT SERVICE (PORT 8008)")
    print("   Engine: ZeroTTS ONNX Runtime + Multimodal Copilot")
    print("=" * 70)
    uvicorn.run(app, host="127.0.0.1", port=8008, log_level="info")
