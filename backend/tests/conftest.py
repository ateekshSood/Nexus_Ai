import os

os.environ["DATABASE_URL"] = "sqlite://"

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.db import Base, get_db
from app.main import app, get_catalog, get_classifier, get_ollama
from app.services.ollama import InstalledModel, OllamaError
from app.services.routing import ModelEntry


class FakeOllama:
    def __init__(self):
        self.calls = []
        self.down = False

    def list_models(self):
        if self.down:
            raise OllamaError("Could not reach Ollama")
        return [InstalledModel("small:1b", 100)]

    def chat(self, model, messages):
        self.calls.append((model, messages))
        return f"reply {len(self.calls)}"


class FakeClassifier:
    def classify(self, prompt):
        return "GENERAL"


@pytest.fixture
def ollama():
    return FakeOllama()


@pytest.fixture
def client(ollama):
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(engine)
    TestSession = sessionmaker(bind=engine, expire_on_commit=False)

    def override_db():
        with TestSession() as db:
            yield db

    catalog = {"GENERAL": [ModelEntry("small:1b", 0.0)], "CODE": []}
    app.dependency_overrides[get_db] = override_db
    app.dependency_overrides[get_ollama] = lambda: ollama
    app.dependency_overrides[get_classifier] = lambda: FakeClassifier()
    app.dependency_overrides[get_catalog] = lambda: catalog
    yield TestClient(app)
    app.dependency_overrides.clear()
