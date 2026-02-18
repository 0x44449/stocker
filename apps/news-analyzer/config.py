import os

# Database
DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql+psycopg2://stocker:stocker@localhost:5433/stocker"
)

# Ollama
OLLAMA_HOST = os.getenv("OLLAMA_HOST", "localhost")
OLLAMA_PORT = os.getenv("OLLAMA_PORT", "11434")
OLLAMA_BASE_URL = f"http://{OLLAMA_HOST}:{OLLAMA_PORT}"

# LLM
LLM_MODEL = "exaone3.5:7.8b"
PROMPT_VERSION = "v1"

# Logging
LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")
