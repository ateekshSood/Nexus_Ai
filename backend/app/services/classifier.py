import numpy as np
from fastembed import TextEmbedding

CODE_ANCHOR = (
    "Programming, software development, coding, debugging, "
    "technical computer science concepts, computer science, code"
)
GENERAL_ANCHOR = (
    "General conversations, casual chatting, history, daily life, "
    "storytelling, and non-technical topics"
)


def _cosine(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))


class PromptClassifier:
    def __init__(self, model_name: str = "sentence-transformers/all-MiniLM-L6-v2"):
        self._model = TextEmbedding(model_name)
        self._code, self._general = list(self._model.embed([CODE_ANCHOR, GENERAL_ANCHOR]))

    def classify(self, prompt: str) -> str:
        if not prompt.strip():
            return "GENERAL"
        embedding = next(iter(self._model.embed([prompt])))
        code_score = _cosine(embedding, self._code)
        general_score = _cosine(embedding, self._general)
        return "CODE" if code_score > general_score else "GENERAL"
