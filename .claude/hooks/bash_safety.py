"""PreToolUse hook: block dangerous bash commands."""
import json
import sys

DANGEROUS_PATTERNS = [
    "rm -rf /",
    "rm -rf /*",
    "rm -rf ~",
    "> /dev/sda",
    "dd if=",
    "mkfs.",
    "chmod -R 777 /",
    "chmod 777 /",
    "curl.*\\|.*bash",
    "curl.*\\|.*sh",
    "wget.*\\|.*bash",
    "wget.*\\|.*sh",
]

DANGEROUS_SQL = [
    "DROP TABLE",
    "DROP DATABASE",
    "DELETE FROM",
    "TRUNCATE TABLE",
    "ALTER TABLE.*DROP",
]

GIT_FORCE_PATTERNS = [
    "git push --force",
    "git push -f",
    "git reset --hard",
    "git checkout -- .",
    "git clean -f",
]


def check_command(command: str) -> str | None:
    lower_cmd = command.lower().strip()

    for pattern in DANGEROUS_PATTERNS:
        if pattern.lower() in lower_cmd:
            return f"[BLOCKED] Dangerous command pattern: {pattern}"

    for pattern in DANGEROUS_SQL:
        if pattern.lower() in lower_cmd:
            return f"[BLOCKED] Dangerous SQL command: {pattern}"

    for pattern in GIT_FORCE_PATTERNS:
        if pattern.lower() in lower_cmd:
            if "main" in lower_cmd or "master" in lower_cmd or "origin" in lower_cmd:
                return f"[BLOCKED] Dangerous git operation on main/master: {pattern}"

    if "../../" in command:
        return "[BLOCKED] Path traversal detected (../../)"

    return None


def main():
    try:
        raw = sys.stdin.read()
        if not raw:
            sys.exit(0)
        data = json.loads(raw)
    except (json.JSONDecodeError, IOError):
        sys.exit(0)

    # Correct format: {"tool_input": {"command": "..."}}
    tool_input = data.get("tool_input", {})
    command = tool_input.get("command", "")
    if command:
        reason = check_command(command)
        if reason:
            print(reason, file=sys.stderr)
            sys.exit(2)

    sys.exit(0)


if __name__ == "__main__":
    main()
