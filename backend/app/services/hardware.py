import os

import psutil

GB = 1024**3


def free_ram_gb() -> float:
    return psutil.virtual_memory().available / GB


def system_info() -> str:
    memory = psutil.virtual_memory()
    cpu_load = psutil.cpu_percent(interval=1)
    process = psutil.Process(os.getpid())
    return (
        f"CPU Load : {cpu_load:.2f}% || "
        f"Available RAM : {memory.available / GB:.2f} GB / {memory.total / GB:.2f} GB || "
        f"Current Process : {process.name()} (pid {process.pid})"
    )
