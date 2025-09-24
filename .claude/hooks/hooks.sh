#!/usr/bin/env bash
# Unified hook handler for Claude Code
# Usage: hooks.sh <hook_type> [additional_params...]

set -euo pipefail

# Get the hook type from the first argument
HOOK_TYPE="${1:-}"

if [ -z "$HOOK_TYPE" ]; then
    echo "Error: Hook type not specified" >&2
    jq -n '{continue: false, stopReason: "Internal error: Hook type not specified"}'
    exit 1
fi

# Read input from stdin
input=$(cat)

# Extract session information
session_id=$(echo "$input" | jq -r '.session_id // empty')
transcript_path=$(echo "$input" | jq -r '.transcript_path // empty')
stop_reason=$(echo "$input" | jq -r '.stop_reason // empty')
tool_name=$(echo "$input" | jq -r '.tool_name // empty')

# Function to show TDD reminder
show_tdd_reminder() {
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
            ;;
    esac
}

# Function to validate todos with quality checks
validate_todos() {
    # Only validate TODOs if this is a complete stop (end of turn, not just a pause)
    # Allow if stop_reason is "max_turns" or "end_turn" (natural conversation flow)
    if [ "$stop_reason" = "max_turns" ] || [ "$stop_reason" = "end_turn" ]; then
        # This is just a pause in conversation, allow it
        return 0
    fi

    # Check if there's a TODO file for this session
    todo_file="$HOME/.claude/todos/${session_id}.json"

    if [ -f "$todo_file" ]; then
        # Check for any pending or in_progress todos
        incomplete=$(jq '[.todos[] | select(.status != "completed")] | length' "$todo_file")

        if [ "$incomplete" -gt 0 ]; then
            # Block if there are incomplete todos and this is an intentional stop
            jq -n \
                --arg reason "There are $incomplete incomplete TODO items. Please complete all TODOs before finishing the task." \
                '{
                    continue: false,
                    stopReason: $reason
                }'
            exit 0
        fi

        # All TODOs completed - run quality checks before allowing stop
        echo "✅ All TODOs completed! Running comprehensive quality checks..." >&2
        echo "📊 Validating quality gates..." >&2

        # Find project directory with gradlew
        project_dir=""
        current_dir=$(pwd)

        # Look for gradlew in current directory or walk up
        check_dir="$current_dir"
        while [ "$check_dir" != "/" ]; do
            if [ -f "$check_dir/gradlew" ]; then
                project_dir="$check_dir"
                break
            fi
            check_dir=$(dirname "$check_dir")
        done

        # If we found a Gradle project, run quality checks
        if [ -n "$project_dir" ] && [ -f "$project_dir/gradlew" ]; then
            cd "$project_dir" || exit 1
            ./gradlew check >&2
            exit_code=$?

            if [ $exit_code -ne 0 ]; then
                jq -n '{
                    continue: false,
                    stopReason: "Quality gates failed! Please fix issues: JaCoCo coverage (≥97% line, ≥95% branch), PITest mutations (100%), all tests passing, no linting violations."
                }'
                exit 0
            else
                echo "✅ All quality gates passed!" >&2
            fi
        fi
    fi

    return 0
}

# Function to update tasks.md based on completed todos
update_tasks() {
    # Only update if we have a session and completed todos
    todo_file="$HOME/.claude/todos/${session_id}.json"

    if [ ! -f "$todo_file" ]; then
        return 0
    fi

    # Check if all todos are completed
    incomplete=$(jq '[.todos[] | select(.status != "completed")] | length' "$todo_file")

    if [ "$incomplete" -gt 0 ]; then
        # Still have incomplete todos, don't update tasks.md yet
        return 0
    fi

    # Find the project directory by looking for docs/tasks.md
    project_dir=""
    if [ -n "$transcript_path" ]; then
        # Extract working directory from transcript path or use current directory
        current_dir=$(pwd)
        if [ -f "$current_dir/docs/tasks.md" ]; then
            project_dir="$current_dir"
        fi
    fi

    # If we found a project with tasks.md, update it
    if [ -n "$project_dir" ] && [ -f "$project_dir/docs/tasks.md" ]; then
        # Extract completed todo information
        completed_todos=$(jq -r '.todos[] | select(.status == "completed") | .content' "$todo_file" 2>/dev/null || echo "")

        if [ -n "$completed_todos" ]; then
            # Add a note to tasks.md about the completed session
            echo "" >> "$project_dir/docs/tasks.md"
            echo "<!-- Session completed at $(date) -->" >> "$project_dir/docs/tasks.md"
            echo "<!-- Completed TODOs:" >> "$project_dir/docs/tasks.md"
            echo "$completed_todos" | while read -r todo; do
                echo "<!-- - $todo -->" >> "$project_dir/docs/tasks.md"
            done
            echo "<!-- End session -->" >> "$project_dir/docs/tasks.md"
        fi
    fi

    return 0
}

# Handle different hook types
case "$HOOK_TYPE" in
    "Stop"|"SubagentStop")
        # First validate todos (includes quality checks)
        validate_todos

        # Then update tasks if validation passed
        update_tasks

        # Allow continuation
        jq -n '{continue: true}'
        ;;
    "PreToolUse")
        # Show TDD reminder for relevant tools
        show_tdd_reminder
        # Always allow tool execution
        jq -n '{
            permissionDecision: "allow",
            hookSpecificOutput: {
                hookEventName: "PreToolUse"
            }
        }'
        ;;
    *)
        echo "Error: Unknown hook type: $HOOK_TYPE" >&2
        jq -n --arg type "$HOOK_TYPE" '{continue: false, stopReason: ("Unknown hook type: " + $type)}'
        exit 1
        ;;
esac