from typing import Any

from aiagent.api.schemas import ChartHint


def is_binary_value_query(columns: list[str], rows: list[list[Any]]) -> bool:
    """判断查询结果是否为二值型（名称列+数值列），适合饼图/柱状图。"""
    pass


def has_time_column(columns: list[str], rows: list[list[Any]]) -> bool:
    """判断查询结果中是否包含时间/日期列，适合折线图。"""
    pass


def select_chart_type(
    columns: list[str],
    rows: list[list[Any]],
    chart_hint: ChartHint,
) -> str:
    """
    根据列类型、行数、chartHint 自动选择图表类型。
    返回 "bar" / "pie" / "line"。
    """
    pass


def build_chart_spec(
    question: str,
    columns: list[str],
    rows: list[list[Any]],
    chart_hint: ChartHint,
) -> dict:
    """
    构建图表渲染规格：
    {
        "chart_type": "bar" | "pie" | "line",
        "title": "...",
        "labels": [...],
        "values": [...],
        "x_data": [...],        # 折线图用
        "y_series": [[...]],    # 折线图用
        "series_names": [...]   # 折线图用
    }
    """
    pass
