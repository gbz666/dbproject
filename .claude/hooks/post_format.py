"""PostToolUse hook: auto-format and type-check after file edits."""
import json
import os
import subprocess
import sys


def find_project_root():
    """Find project root by locating the .claude directory."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    candidate = os.path.dirname(script_dir)
    if os.path.isdir(candidate):
        return os.path.dirname(candidate)
    path = os.getcwd()
    for _ in range(10):
        if os.path.isdir(os.path.join(path, ".claude")):
            return path
        parent = os.path.dirname(path)
        if parent == path:
            break
        path = parent
    return os.getcwd()


PROJECT_ROOT = find_project_root()


def run(cmd: str, cwd: str | None = None) -> tuple[int, str]:
    try:
        result = subprocess.run(
            cmd, shell=True, capture_output=True, text=True,
            cwd=cwd or PROJECT_ROOT, timeout=60
        )
        return result.returncode, (result.stdout + result.stderr).strip()
    except subprocess.TimeoutExpired:
        return -1, "Command timed out"
    except Exception as e:
        return -1, str(e)


def format_frontend(file_path: str):
    rel = os.path.relpath(file_path, PROJECT_ROOT).replace("\\", "/")
    frontend_dir = os.path.join(PROJECT_ROOT, "frontend")

    code, out = run(f"npx prettier --write \"{rel}\"", cwd=frontend_dir)
    if code != 0 and out:
        print(f"[prettier] {out}", file=sys.stderr)

    code, out = run("npx vue-tsc --build --noEmit", cwd=frontend_dir)
    if code != 0 and out:
        print(f"[vue-tsc] {out}", file=sys.stderr)


def format_python(file_path: str):
    code, out = run(f"python -m black --quiet \"{file_path}\"")
    if code != 0 and out:
        print(f"[black] {out}", file=sys.stderr)

    code, out = run(f"python -m ruff check --fix \"{file_path}\"")
    if code != 0 and out:
        print(f"[ruff] {out}", file=sys.stderr)


def format_java(file_path: str):
    backend_dir = os.path.join(PROJECT_ROOT, "backend")
    code, out = run("mvn compile -q -f pom.xml", cwd=backend_dir)
    if code != 0 and out:
        print(f"[mvn compile] {out}", file=sys.stderr)


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

    normalized = file_path.replace("\\", "/")

    if any(normalized.endswith(ext) for ext in (".vue", ".ts", ".js", ".tsx", ".jsx")):
        format_frontend(file_path)
    elif normalized.endswith(".py"):
        format_python(file_path)
    elif normalized.endswith(".java"):
        format_java(file_path)

    sys.exit(0)


if __name__ == "__main__":
    main()
