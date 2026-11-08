from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class ChatRequest(BaseModel):
    message: str = Field(min_length=1)
    chatId: str | None = None


class ChatResponse(BaseModel):
    chatId: str
    text: str


class SessionOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str
    title: str | None
    created_at: datetime = Field(serialization_alias="createdAt")


class MessageOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str
    sender: str
    text: str
    timestamp: datetime
