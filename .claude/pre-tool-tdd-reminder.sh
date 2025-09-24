#!/usr/bin/env bash

# Read JSON input from stdin
input=$(cat)

# Extract tool name from JSON input
tool_name=$(echo "$input" | jq -r '.tool_name // empty')

# Only remind for tools that involve code changes
case "$tool_name" in
  Write|Edit|MultiEdit|Task)
    cat << 'EOF'
⚠️ TDD REMINDER:
- Follow Red-Green-Refactor cycle strictly
- Write ONE failing test first (RED)
- Write minimal code to pass that test (GREEN)
- REFACTOR immediately after green - do NOT skip this step
- Use red-test-creator, tdd-green-test, and tdd-refactoring-agent subagents
- NEVER implement multiple requirements in one cycle
- Refactoring is MANDATORY in each cycle, not just at the end
EOF
    exit 0
    ;;
  *)
    # For other tools, just pass through
    exit 0
    ;;
esac