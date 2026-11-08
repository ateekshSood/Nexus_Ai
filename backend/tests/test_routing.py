from app.services.ollama import InstalledModel
from app.services.routing import ModelEntry, choose_model

CATALOG = {
    "GENERAL": [
        ModelEntry("tiny", 2.0),
        ModelEntry("medium", 2.5),
        ModelEntry("large", 16.0),
    ],
    "CODE": [ModelEntry("coder", 4.0)],
}


def installed(*names):
    return [InstalledModel(name, 100) for name in names]


def test_picks_most_capable_fit_regardless_of_catalog_order():
    assert choose_model("GENERAL", 3.0, installed("tiny", "medium", "large"), CATALOG) == "medium"


def test_skips_models_that_are_not_installed():
    assert choose_model("GENERAL", 32.0, installed("tiny"), CATALOG) == "tiny"


def test_uses_category_catalog():
    assert choose_model("CODE", 8.0, installed("tiny", "coder"), CATALOG) == "coder"


def test_fallback_uses_smallest_installed_model():
    models = [InstalledModel("huge", 9000), InstalledModel("little", 300)]
    assert choose_model("CODE", 1.0, models, CATALOG) == "little"


def test_returns_none_when_nothing_is_installed():
    assert choose_model("GENERAL", 32.0, [], CATALOG) is None
