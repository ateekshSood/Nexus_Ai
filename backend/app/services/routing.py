from dataclasses import dataclass
from pathlib import Path

import yaml

from app.services.ollama import InstalledModel


@dataclass(frozen=True)
class ModelEntry:
    name: str
    min_ram_gb: float


Catalog = dict[str, list[ModelEntry]]


def load_catalog(path: Path) -> Catalog:
    raw = yaml.safe_load(path.read_text())
    return {
        category.upper(): [ModelEntry(**entry) for entry in entries]
        for category, entries in raw.items()
    }


def choose_model(
    category: str,
    free_ram_gb: float,
    installed: list[InstalledModel],
    catalog: Catalog,
) -> str | None:
    """Pick the most capable installed model that fits in free RAM.

    If no catalog model fits, fall back to the smallest installed model, which
    is the one most likely to load. Returns None if nothing is installed.
    """
    if not installed:
        return None

    installed_names = {model.name for model in installed}
    candidates = sorted(catalog[category], key=lambda m: m.min_ram_gb, reverse=True)
    for entry in candidates:
        if free_ram_gb >= entry.min_ram_gb and entry.name in installed_names:
            return entry.name

    return min(installed, key=lambda m: m.size_bytes).name
