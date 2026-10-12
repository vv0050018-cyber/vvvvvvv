import os
from pathlib import Path
from dotenv import load_dotenv

BASE_DIR = Path(__file__).resolve().parent

# Load .env file if available
load_dotenv(BASE_DIR / ".env")

class Config:
    SECRET_KEY = os.getenv("SECRET_KEY", "prepai-dev-insecure-secret-key-change-me")
    DATABASE_PATH = os.getenv("DATABASE_PATH", str(BASE_DIR / "database" / "prepai.db"))
    
    # Session security
    SESSION_COOKIE_HTTPONLY = True
    SESSION_COOKIE_SAMESITE = "Lax"
    PERMANENT_SESSION_LIFETIME = 86400 * 7  # 7 days
    
    # AI API keys
    GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
    OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
    
    # AI evaluation fallback indicator
    USE_AI_FALLBACK = not bool(GEMINI_API_KEY or OPENAI_API_KEY)
