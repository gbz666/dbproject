"""
只读 SQL 执行器 —— 供 LangGraph Agent 自验证（试跑）使用。
使用 .env 中配置的 ai_readonly 账号，仅有 SELECT 权限。
"""
import logging
import time
from typing import Any

import requests

from aiagent.core.config import load_settings

logger = logging.getLogger(__name__)

_cached_backend_token: str = ""
_cached_backend_token_expire_at: float = 0.0


def _fetch_backend_token(settings: dict[str, Any]) -> str:
    """
    从 Java 后端登录并换取 token。
    优先使用服务账号（staffName/password）。
    """
    staff_name = (settings.get("java_backend_staff_name") or "").strip()
    password = settings.get("java_backend_password") or ""
    if not staff_name or not password:
        return ""

    login_url = f"{settings['java_backend_url'].rstrip('/')}/api/auth/login"
    try:
        resp = requests.post(
            login_url,
            json={"staffName": staff_name, "password": password},
            timeout=10.0,
        )
        resp.raise_for_status()
        payload = resp.json() or {}
        data = payload.get("data") or {}
        token = data.get("token") or ""
        expiration_seconds = int(data.get("expiration") or 7200)
        if token:
            global _cached_backend_token, _cached_backend_token_expire_at
            _cached_backend_token = token
            # 预留 60 秒缓冲，避免临界过期
            _cached_backend_token_expire_at = time.time() + max(0, expiration_seconds - 60)
            logger.info("Java 后端登录成功，已获取 AI 试跑专用 token")
            return token
    except requests.exceptions.RequestException as e:
        logger.warning("获取 Java 后端 token 失败: %s", e)
    except Exception as e:
        logger.warning("解析 Java 后端登录响应失败: %s", e)
    return ""


def _get_backend_auth_token(settings: dict[str, Any], force_refresh: bool = False) -> str:
    """
    获取 Java 后端调用 token：
    1) JAVA_BACKEND_TOKEN
    2) 缓存 token
    3) 服务账号登录换取 token
    """
    fixed_token = (settings.get("java_backend_token") or "").strip()
    if fixed_token:
        return fixed_token

    if not force_refresh and _cached_backend_token and time.time() < _cached_backend_token_expire_at:
        return _cached_backend_token

    return _fetch_backend_token(settings)


def _build_headers(settings: dict[str, Any], force_refresh: bool = False) -> dict[str, str]:
    headers = {"Content-Type": "application/json"}
    token = _get_backend_auth_token(settings, force_refresh=force_refresh)
    if token:
        headers["Authorization"] = f"Bearer {token}"
    return headers

def execute_readonly_sql(sql: str) -> dict[str, Any]:
    """
    通过调用 Java 后端的 MCP Tool Server 接口执行只读 SQL 测试。

    返回:
        {
            "success": bool,
            "columns": list[str],
            "rows": list[list],
            "row_count": int,
            "error": str,
        }
    """
    settings = load_settings()
    url = f"{settings['java_backend_url'].rstrip('/')}/api/ai/internal/trial-execute"
    
    try:
        headers = _build_headers(settings)
        if "Authorization" not in headers:
            logger.warning(
                "未配置 Java 后端 token。请在 aiagent/.env 配置 JAVA_BACKEND_TOKEN，"
                "或 JAVA_BACKEND_STAFF_NAME + JAVA_BACKEND_PASSWORD"
            )

        resp = requests.post(url, json={"sql": sql}, headers=headers, timeout=15.0)

        # 若 token 过期且使用的是服务账号模式，尝试强制刷新 token 后重试一次
        if resp.status_code == 401 and not (settings.get("java_backend_token") or "").strip():
            retry_headers = _build_headers(settings, force_refresh=True)
            if "Authorization" in retry_headers:
                resp = requests.post(url, json={"sql": sql}, headers=retry_headers, timeout=15.0)

        resp.raise_for_status()
        result = resp.json()
        
        # 统一 Java 返回值和 Python 需要的类型的格式
        success = result.get("success", False)
        error = result.get("error", "")
        
        if success:
            logger.info(
                "MCP Trial Server 试跑成功 ✓ 返回 %d 列, %d 行",
                len(result.get("columns", [])), len(result.get("rows", []))
            )
        else:
            logger.warning("MCP Trial Server 试跑失败 ✗ 错误: %s", error[:300])
            
        return {
            "success": success,
            "columns": result.get("columns", []),
            "rows": result.get("rows", []),
            "row_count": result.get("rowCount", 0),  # Java 返回的是 camelCase rowCount
            "error": error,
        }
    except requests.exceptions.Timeout:
        err_msg = "Timeout: Java MCP 试跑服务响应超时"
        logger.warning(err_msg)
        return {"success": False, "columns": [], "rows": [], "row_count": 0, "error": err_msg}
    except requests.exceptions.RequestException as e:
        err_msg = f"RequestException: {e}"
        if getattr(e, 'response', None) is not None:
             err_msg = f"RequestException: HTTP {e.response.status_code} - {e.response.text}"
        logger.warning("请求 Java MCP 试跑服务失败: %s", err_msg)
        return {"success": False, "columns": [], "rows": [], "row_count": 0, "error": err_msg}
    except Exception as e:
        err_msg = f"{type(e).__name__}: {e}"
        logger.warning("试跑调用发生未知错误: %s", err_msg)
        return {"success": False, "columns": [], "rows": [], "row_count": 0, "error": err_msg}
