import logging
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException, Request, Response
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import PlainTextResponse
from sqlalchemy import delete, select
from sqlalchemy.orm import Session

from app.config import settings
from app.db import get_db
from app.models import ChatMessage, ChatSession
from app.schemas import ChatRequest, ChatResponse, MessageOut, SessionOut
from app.services.chat import NoModelInstalled, SessionNotFound, handle_chat
from app.services.classifier import PromptClassifier
from app.services.hardware import free_ram_gb, system_info
from app.services.ollama import OllamaClient, OllamaError
from app.services.routing import Catalog, load_catalog

logging.basicConfig(level=logging.INFO)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    app.state.ollama = OllamaClient(settings.ollama_base_url)
    app.state.classifier = PromptClassifier()
    app.state.catalog = load_catalog(settings.models_file)
    yield


def get_ollama(request: Request) -> OllamaClient:
    return request.app.state.ollama


def get_classifier(request: Request) -> PromptClassifier:
    return request.app.state.classifier


def get_catalog(request: Request) -> Catalog:
    return request.app.state.catalog


Db = Annotated[Session, Depends(get_db)]

app = FastAPI(title="Nexus AI", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_methods=["GET", "POST", "DELETE"],
    allow_headers=["*"],
)


@app.post("/api/ai/chat", response_model=ChatResponse)
def chat(
    request: ChatRequest,
    db: Db,
    ollama: Annotated[OllamaClient, Depends(get_ollama)],
    classifier: Annotated[PromptClassifier, Depends(get_classifier)],
    catalog: Annotated[Catalog, Depends(get_catalog)],
) -> ChatResponse:
    try:
        chat_id, text = handle_chat(
            db, request.message, request.chatId, ollama, classifier, catalog
        )
    except SessionNotFound:
        raise HTTPException(404, "Chat session not found")
    except NoModelInstalled:
        raise HTTPException(503, "No Ollama models are installed")
    except OllamaError as e:
        raise HTTPException(503, str(e))
    return ChatResponse(chatId=chat_id, text=text)


@app.get("/api/chats", response_model=list[SessionOut])
def list_sessions(db: Db):
    return db.scalars(select(ChatSession).order_by(ChatSession.created_at.desc())).all()


@app.get("/api/chats/{session_id}/messages", response_model=list[MessageOut])
def list_messages(session_id: str, db: Db):
    return db.scalars(
        select(ChatMessage)
        .where(ChatMessage.session_id == session_id)
        .order_by(ChatMessage.timestamp.asc())
    ).all()


@app.delete("/api/chats/{session_id}", status_code=204)
def delete_session(session_id: str, db: Db) -> Response:
    db.execute(delete(ChatMessage).where(ChatMessage.session_id == session_id))
    db.execute(delete(ChatSession).where(ChatSession.id == session_id))
    db.commit()
    return Response(status_code=204)


@app.get("/api/system/ram", response_class=PlainTextResponse)
def ram() -> str:
    return f"{free_ram_gb():.2f}"


@app.get("/api/system/info", response_class=PlainTextResponse)
def info() -> str:
    return system_info()
