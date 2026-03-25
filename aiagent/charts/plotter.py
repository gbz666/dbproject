from pathlib import Path
from typing import Any

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.font_manager as fm

from aiagent.api.schemas import ChartHint
from aiagent.agent.chart_agent import build_chart_spec, select_chart_type
from aiagent.utils.ids import generate_chart_id

plt.rcParams["font.sans-serif"] = ["SimHei", "Microsoft YaHei", "WenQuanYi Micro Hei", "Arial"]
plt.rcParams["axes.unicode_minus"] = False


def ensure_chart_dir(chart_dir: Path) -> None:
    chart_dir.mkdir(parents=True, exist_ok=True)


def build_bar_chart(title: str, labels: list[str], values: list[float], output_path: Path) -> None:
    fig, ax = plt.subplots(figsize=(max(8, len(labels) * 0.8), 5))
    bars = ax.bar(range(len(labels)), values, color="#4C78A8")
    ax.set_xticks(range(len(labels)))
    ax.set_xticklabels(labels, rotation=45, ha="right", fontsize=9)
    ax.set_title(title, fontsize=13)
    ax.set_ylabel("数值")
    for bar, val in zip(bars, values):
        ax.text(bar.get_x() + bar.get_width() / 2, bar.get_height(),
                f"{val:,.1f}", ha="center", va="bottom", fontsize=8)
    fig.tight_layout()
    fig.savefig(str(output_path), dpi=150)
    plt.close(fig)


def build_pie_chart(title: str, labels: list[str], values: list[float], output_path: Path) -> None:
    fig, ax = plt.subplots(figsize=(7, 7))
    wedges, texts, autotexts = ax.pie(
        values, labels=labels, autopct="%1.1f%%", startangle=140,
        textprops={"fontsize": 9},
    )
    ax.set_title(title, fontsize=13)
    fig.tight_layout()
    fig.savefig(str(output_path), dpi=150)
    plt.close(fig)


def build_line_chart(
    title: str,
    x_data: list[Any],
    y_series: list[list[float]],
    series_names: list[str],
    output_path: Path,
) -> None:
    fig, ax = plt.subplots(figsize=(max(8, len(x_data) * 0.6), 5))
    for y_vals, name in zip(y_series, series_names):
        ax.plot(x_data, y_vals, marker="o", markersize=4, label=name)
    ax.set_title(title, fontsize=13)
    ax.set_ylabel("数值")
    ax.legend(fontsize=9)
    ax.tick_params(axis="x", rotation=45)
    fig.tight_layout()
    fig.savefig(str(output_path), dpi=150)
    plt.close(fig)


def render_chart(
    question: str,
    columns: list[str],
    rows: list[list[Any]],
    chart_hint: ChartHint,
    output_dir: Path,
) -> tuple[str, str]:
    """
    统一入口：自动选图 -> 构建 spec -> 渲染 PNG -> 返回 (chartId, fileName)。
    """
    ensure_chart_dir(output_dir)
    chart_id = generate_chart_id()
    file_name = f"{chart_id}.png"
    output_path = output_dir / file_name

    spec = build_chart_spec(question, columns, rows, chart_hint)
    chart_type = spec.get("chart_type", "bar")
    title = spec.get("title", question)

    if chart_type == "pie":
        build_pie_chart(title, spec["labels"], spec["values"], output_path)
    elif chart_type == "line":
        build_line_chart(title, spec["x_data"], spec["y_series"], spec["series_names"], output_path)
    else:
        build_bar_chart(title, spec["labels"], spec["values"], output_path)

    return chart_id, file_name
