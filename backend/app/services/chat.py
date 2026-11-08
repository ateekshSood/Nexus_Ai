import logging
from datetime import datetime

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.config import settings
from app.models import ChatMessage, ChatSession
from app.services.classifier import PromptClassifier
from app.services.hardware import free_ram_gb
from app.services.ollama import OllamaClient
from app.services.routing import Catalog, choose_model

logger = logging.getLogger(__name__)

TITLE_LENGTH = 40
ROLE_BY_SENDER = {"USER": "user", "AI": "assistant"}


class SessionNotFound(Exception):
    pass


class NoModelInstalled(Exception):
    pass


def _title_from(message: str) -> str:
    if len(message) > TITLE_LENGTH:
        return message[:TITLE_LENGTH] + "..."
    return message


def _recent_history(db: Session, session_id: str) -> list[dict[str, str]]:
    """Return the latest messages for model context, oldest first.

    This only limits what the model sees. Stored history is never trimmed.
    """
    rows = db.scalars(
        select(ChatMessage)
        .where(ChatMessage.session_id == session_id)
        .order_by(ChatMessage.timestamp.desc())
        .limit(settings.context_messages - 1)
    ).all()
    return [
        {"role": ROLE_BY_SENDER[row.sender], "content": row.text}
        for row in reversed(rows)
    ]


def handle_chat(
    db: Session,
    message: str,
    chat_id: str | None,
    ollama: OllamaClient,
    classifier: PromptClassifier,
    catalog: Catalog,
) -> tuple[str, str]:
    """Generate a reply and return (chat_id, reply).

    The session and both messages are saved in one transaction after the model
    responds, so a failed request leaves nothing behind.
    """
    is_new_chat = not chat_id or not chat_id.strip()
    if is_new_chat:
        history = []
    else:
        if db.get(ChatSession, chat_id) is None:
            raise SessionNotFound(chat_id)
        history = _recent_history(db, chat_id)

    category = classifier.classify(message)
    ram = free_ram_gb()
    model = choose_model(category, ram, ollama.list_models(), catalog)
    if model is None:
        raise NoModelInstalled
    logger.info("category=%s free_ram_gb=%.2f model=%s", category, ram, model)

    asked_at = datetime.now()
    reply = ollama.chat(model, [*history, {"role": "user", "content": message}])

    if is_new_chat:
        session = ChatSession(title=_title_from(message))
        db.add(session)
        db.flush()
        chat_id = session.id

    db.add_all(
        [
            ChatMessage(session_id=chat_id, sender="USER", text=message, timestamp=asked_at),
            ChatMessage(session_id=chat_id, sender="AI", text=reply, timestamp=datetime.now()),
        ]
    )
    db.commit()
    return chat_id, reply
