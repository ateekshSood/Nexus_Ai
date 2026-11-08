def send(client, message, chat_id=None):
    return client.post("/api/ai/chat", json={"message": message, "chatId": chat_id})


def test_new_chat_creates_session_with_truncated_title(client):
    response = send(client, "x" * 50)
    assert response.status_code == 200
    body = response.json()
    assert body["text"] == "reply 1"

    sessions = client.get("/api/chats").json()
    assert [s["id"] for s in sessions] == [body["chatId"]]
    assert sessions[0]["title"] == "x" * 40 + "..."
    assert "createdAt" in sessions[0]


def test_messages_are_returned_in_order(client):
    chat_id = send(client, "hello").json()["chatId"]
    messages = client.get(f"/api/chats/{chat_id}/messages").json()
    assert [(m["sender"], m["text"]) for m in messages] == [
        ("USER", "hello"),
        ("AI", "reply 1"),
    ]


def test_context_is_windowed_but_stored_history_is_kept(client, ollama):
    chat_id = send(client, "turn 0").json()["chatId"]
    for turn in range(1, 15):
        send(client, f"turn {turn}", chat_id)

    assert len(client.get(f"/api/chats/{chat_id}/messages").json()) == 30

    _, sent_to_model = ollama.calls[-1]
    assert len(sent_to_model) == 20
    assert sent_to_model[-1] == {"role": "user", "content": "turn 14"}


def test_unknown_chat_returns_404(client):
    assert send(client, "hello", "missing").status_code == 404


def test_blank_chat_id_starts_new_chat(client):
    assert send(client, "hello", "  ").status_code == 200
    assert len(client.get("/api/chats").json()) == 1


def test_empty_message_is_rejected(client):
    assert send(client, "").status_code == 422


def test_ollama_failure_returns_503_and_saves_nothing(client, ollama):
    ollama.down = True
    assert send(client, "hello").status_code == 503
    assert client.get("/api/chats").json() == []


def test_delete_removes_session_and_messages(client):
    chat_id = send(client, "hello").json()["chatId"]
    assert client.delete(f"/api/chats/{chat_id}").status_code == 204
    assert client.get("/api/chats").json() == []
    assert client.get(f"/api/chats/{chat_id}/messages").json() == []
