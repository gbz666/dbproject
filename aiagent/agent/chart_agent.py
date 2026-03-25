import re
from typing import Any

from aiagent.api.schemas import ChartHint

_TIME_KEYWORDS = {"date", "time", "month", "year", "quarter", "week", "day", "period", "日期", "月", "年"}


def _is_numeric(value: Any) -> bool:
    if isinstance(value, (int, float)):
        return True
    if isinstance(value, str):
        try:
            float(value)
            return True
        except (ValueError, TypeError):
            return False
    return False


def _looks_like_time(col_name: str) -> bool:
    lower = col_name.lower()
    if any(kw in lower for kw in _TIME_KEYWORDS):
        return True
    if re.match(r"\d{4}[-/]\d{2}", str(col_name)):
        return True
    return False


def is_binary_value_query(columns: list[str], rows: list[list[Any]]) -> bool:
    """判断查询结果是否为二值型（名称列+数值列），适合饼图/柱状图。"""
    if len(columns) != 2 or not rows:
        return False
    sample = rows[0]
    has_str = not _is_numeric(sample[0])
    has_num = _is_numeric(sample[1])
    return has_str and has_num


def has_time_column(columns: list[str], rows: list[list[Any]]) -> bool:
    """判断查询结果中是否包含时间/日期列，适合折线图。"""
    for col in columns:
        if _looks_like_time(col):
            return True
    if rows:
        for val in rows[0]:
            if isinstance(val, str) and re.match(r"\d{4}[-/]\d{2}", val):
                return True
    return False


def select_chart_type(
    columns: list[str],
    rows: list[list[Any]],
    chart_hint: ChartHint,
) -> str:
    """
    根据列类型、行数、chartHint 自动选择图表类型。
    返回 "bar" / "pie" / "line"。
    """
    if chart_hint.type and chart_hint.type in ("bar", "pie", "line"):
        return chart_hint.type

    if has_time_column(columns, rows):
        return "line"

    if is_binary_value_query(columns, rows):
        unique_labels = len(set(str(r[0]) for r in rows))
        return "pie" if unique_labels <= 8 else "bar"

    return "bar"


def _find_col_index(columns: list[str], target: str | None) -> int | None:
    if target is None:
        return None
    lower = target.lower()
    for i, c in enumerate(columns):
        if c.lower() == lower:
            return i
    return None


def build_chart_spec(
    question: str,
    columns: list[str],
    rows: list[list[Any]],
    chart_hint: ChartHint,
) -> dict:
    """
    构建图表渲染规格。
    """
    chart_type = select_chart_type(columns, rows, chart_hint)
    title = question[:60]

    x_idx = _find_col_index(columns, chart_hint.x)
    y_idx = _find_col_index(columns, chart_hint.y)
    series_idx = _find_col_index(columns, chart_hint.series)

    if x_idx is None:
        x_idx = 0
    if y_idx is None:
        for i, col in enumerate(columns):
            if i != x_idx and rows and _is_numeric(rows[0][i]):
                y_idx = i
                break
        if y_idx is None:
            y_idx = min(1, len(columns) - 1)

    if chart_type == "line" and series_idx is not None:
        series_values = sorted(set(str(r[series_idx]) for r in rows))
        x_values = sorted(set(str(r[x_idx]) for r in rows))
        data_map: dict[str, dict[str, float]] = {s: {} for s in series_values}
        for r in rows:
            s = str(r[series_idx])
            x = str(r[x_idx])
            data_map[s][x] = float(r[y_idx]) if _is_numeric(r[y_idx]) else 0.0
        y_series = [[data_map[s].get(x, 0.0) for x in x_values] for s in series_values]
        return {
            "chart_type": "line",
            "title": title,
            "x_data": x_values,
            "y_series": y_series,
            "series_names": series_values,
        }

    if chart_type == "line":
        x_data = [str(r[x_idx]) for r in rows]
        y_data = [float(r[y_idx]) if _is_numeric(r[y_idx]) else 0.0 for r in rows]
        col_label = columns[y_idx] if y_idx < len(columns) else "数值"
        return {
            "chart_type": "line",
            "title": title,
            "x_data": x_data,
            "y_series": [y_data],
            "series_names": [col_label],
        }

    labels = [str(r[x_idx]) for r in rows]
    values = [float(r[y_idx]) if _is_numeric(r[y_idx]) else 0.0 for r in rows]
    return {
        "chart_type": chart_type,
        "title": title,
        "labels": labels,
        "values": values,
    }
