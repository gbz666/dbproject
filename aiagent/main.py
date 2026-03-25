"""
启动方式：
  cd aiagent
  uvicorn main:app --host 0.0.0.0 --port 8001 --reload
"""
from aiagent.app import create_app

app = create_app()
