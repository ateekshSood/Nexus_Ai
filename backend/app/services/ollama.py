from dataclasses import dataclass

import httpx


class OllamaError(Exception):
    """Ollama could not be reached or returned an error."""


@dataclass(frozen=True)
class InstalledModel:
    name: str
    size_bytes: int


class OllamaClient:
    def __init__(self, base_url: str):
        self._http = httpx.Client(
            base_url=base_url, timeout=httpx.Timeout(120.0, connect=5.0)
        )

    def list_models(self) -> list[InstalledModel]:
        data = self._request("GET", "/api/tags")
        return [
            InstalledModel(name=m["name"], size_bytes=m.get("size", 0))
            for m in data.get("models", [])
        ]

    def chat(self, model: str, messages: list[dict[str, str]]) -> str:
        data = self._request(
            "POST",
            "/api/chat",
            json={"model": model, "messages": messages, "stream": False},
        )
        return data["message"]["content"]

    def _request(self, method: str, path: str, **kwargs) -> dict:
        try:
            response = self._http.request(method, path, **kwargs)
            response.raise_for_status()
            return response.json()
        except httpx.HTTPStatusError as e:
            raise OllamaError(f"Ollama returned: {e.response.text}") from e
        except httpx.HTTPError as e:
            raise OllamaError(f"Could not reach Ollama: {e}") from e
