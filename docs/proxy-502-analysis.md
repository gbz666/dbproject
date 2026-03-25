# AI Agent 调用 Ollama 502 问题与空响应分析

## 一、为什么不设 NO_PROXY 就会 502？

### 根因：系统代理劫持了本地请求

你的电脑上装了科学上网工具（Clash / V2Ray 等），它在系统层面设置了 HTTP 代理环境变量：

```
http_proxy=http://127.0.0.1:7890
https_proxy=http://127.0.0.1:7890
```

Python 的 `openai` SDK 底层使用 `httpx` 库发 HTTP 请求。`httpx` 会**自动读取这些环境变量**，将所有 HTTP 请求（包括访问 `localhost:11434` 的 Ollama）通过代理转发。

### 请求链路对比

**不设 NO_PROXY（502 路径）：**

```
Python openai SDK
  → httpx 发现 http_proxy 环境变量
  → 把请求发给 Clash 代理 (127.0.0.1:7890)
  → Clash 尝试转发到 localhost:11434
  → Clash 无法正确代理本地回环地址 / 超时
  → 返回 502 Bad Gateway
```

**设了 NO_PROXY（正常路径）：**

```
Python openai SDK
  → httpx 发现 NO_PROXY 包含 localhost
  → 绕过代理，直连 localhost:11434
  → Ollama 正常响应
  → 200 OK
```

### 为什么 curl 直接访问 Ollama 没问题？

你在终端执行 `curl http://localhost:11434/api/generate ...` 是正常的，因为：

1. Windows 的 `curl.exe` 默认**不读取** `http_proxy` 环境变量（除非 PowerShell 环境里手动 `$env:http_proxy` 设了）
2. 而 Python 的 `httpx` / `requests` 库会**主动读取**进程级环境变量

所以表现为：curl 直连 Ollama 正常，Python SDK 走代理导致 502。

### 正确的修复方式

在 Windows 上，`httpx.Client(proxy=None)` **无效**——httpx 仍然会从 Windows 注册表
（`HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings`）读取系统代理设置，
`proxy=None` 只是表示"没有显式指定"，并不能覆盖注册表里的配置。

唯一可靠的方式是**通过环境变量强制绕过**：

```python
import os
os.environ.setdefault("NO_PROXY", "127.0.0.1,localhost")
os.environ["http_proxy"] = ""
os.environ["https_proxy"] = ""
```

这会让 httpx 在检查环境变量阶段就确定"不走代理"，不再去读注册表。
代价是会影响同进程内所有 HTTP 请求的代理行为——不过对于本项目，
Python 端只需要连本地 Ollama，所以没有副作用。

> 以后如果切到云端 Gemini API（需要科学上网），需要把这三行环境变量去掉，
> 或者改为只清空 `http_proxy` 而保留 `https_proxy`。

---

## 二、为什么修好 502 后，响应全是空的？

### 现象

请求返回 **200 OK**，但所有字段都是默认空值：

```json
{
  "sqlTemplate": "",
  "paramsSpec": [],
  "reason": "",
  "chartHint": {"type": null, "x": null, "y": null, "series": null},
  "confidence": 0.0,
  "warnings": []
}
```

**和没响应的区别：没响应是 500 Internal Server Error（代理 502 导致的异常抛出），而这个空响应是 200 OK——说明 LLM 确实被调通了，Ollama 确实返回了内容，但内容没被正确解析。**

### 根因分析

代码的解析链路：

```
LLM 返回原始文本
  → _extract_json() 尝试提取 JSON
  → parse_llm_response() 用 .get() 取字段
  → 如果字段名对不上，全部返回默认值
```

问题出在 `parse_llm_response` 使用的 `.get("sqlTemplate", "")` 这类调用——**如果 LLM 返回的 JSON key 和预期不一致**（这在小模型上非常常见），所有字段都会 fallback 到默认值。

qwen2.5-coder:7b 是 7B 参数的小模型，常见的输出偏差包括：

| 预期 key | 小模型可能返回的 key |
|---|---|
| `sqlTemplate` | `sql_template`、`sql`、`query` |
| `paramsSpec` | `params_spec`、`parameters`、`params` |
| `chartHint` | `chart_hint`、`chart` |
| `confidence` | 可能直接省略 |

此外，小模型还可能：
- 在 JSON 前后加多余的解释文字（导致 json.loads 失败 → 异常被吞 → 返回 `{}`）
- 返回不完整的 JSON（缺少闭合括号）
- 嵌套结构和 prompt 要求的不一致

### 当前代码的致命缺陷

`call_llm_json` 中有这行：

```python
content = response.choices[0].message.content or "{}"
```

如果 LLM 返回空字符串，就直接变成 `{}`，然后 `parse_llm_response` 对 `{}` 取 `.get()` 全部返回默认值——**静默失败，没有任何日志或报错**。

同时 `_extract_json` 如果 `json.loads` 抛异常，异常会一路传到 FastAPI 变成 500，但日志可能不够明显。

---

## 三、结论与修复建议

| 问题 | 原因 | 修复 |
|---|---|---|
| 502 Bad Gateway | 系统代理劫持了到 localhost 的 HTTP 请求 | 创建 OpenAI 客户端时显式禁用代理 |
| 响应全空 | LLM 返回的 JSON key 与代码预期不匹配，且没有错误处理 | 增加 key 模糊匹配 + 日志 + 错误处理 |
| 看不到具体错误 | logging 未配置，LLM 原始响应被吞 | 增加详细日志输出 |
