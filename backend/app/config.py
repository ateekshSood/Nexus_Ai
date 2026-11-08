from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

BASE_DIR = Path(__file__).resolve().parent.parent


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=BASE_DIR / ".env", extra="ignore")

    database_url: str
    ollama_base_url: str = "http://localhost:11434"
    models_file: Path = BASE_DIR / "models.yaml"
    # Total messages sent to the model per turn, including the new one.
    context_messages: int = 20
    cors_origins: list[str] = ["http://localhost:5173", "http://127.0.0.1:5173"]


settings = Settings()
