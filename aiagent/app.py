from fastapi import FastAPI

from aiagent.api.routes import register_routes
from aiagent.core.config import load_env


def create_app() -> FastAPI:
    load_env()
    app = FastAPI(title="AI Agent Backend", version="0.1.0")
    register_routes(app)
    return app
