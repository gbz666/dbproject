import uuid


def generate_chart_id() -> str:
    """生成唯一的图表 ID（短 UUID）。"""
    return uuid.uuid4().hex[:12]
