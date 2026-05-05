"""PreToolUse hook: block edits to sensitive files."""
import json
import os
import sys
import fnmatch

SENSITIVE_PATTERNS = [
    "*.env",
    "*.pem",
    "*.key",
    "*.p12",
    "*.pfx",
    "*credentials*",
    "*secret*",
    ".ssh/*",
    ".aws/*",
    "id_rsa",
    "id_ed25519",
    "*.jks",
    "*.keystore",
]

SAFE_EXCEPTIONS = [
    ".env.example",
    ".env.sample",
    ".env.template",
]


def is_sensitive(file_path: str) -> bool:
    normalized = file_path.replace("\\", "/")
    basename = normalized.split("/")[-1]

    for exception in SAFE_EXCEPTIONS:
        if fnmatch.fnmatch(basename, exception) or fnmatch.fnmatch(normalized, f"*/{exception}"):
            return False

    for pattern in SENSITIVE_PATTERNS:
        if fnmatch.fnmatch(basename, pattern) or fnmatch.fnmatch(normalized, f"*/{pattern}"):
            return True

    return False


def main():
    try:
        raw = sys.stdin.read()
        if not raw:
            sys.exit(0)
        data = json.loads(raw)
    except (json.JSONDecodeError, IOError):
        sys.exit(0)

    # Correct format: {"tool_input": {"file_path": "..."}}
    tool_input = data.get("tool_input", {})
    file_path = tool_input.get("file_path", "")
    if not file_path:
        sys.exit(0)

    if is_sensitive(file_path):
        print(f"[BLOCKED] Refuse to modify sensitive file: {file_path}", file=sys.stderr)
        sys.exit(2)

    sys.exit(0)


if __name__ == "__main__":
    main()
