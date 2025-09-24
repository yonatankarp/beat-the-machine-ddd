#!/usr/bin/env bash
# Hook to validate that all TODOs are completed before finishing a task

# Read input from stdin
input=$(cat)

# Extract session ID and transcript path
session_id=$(echo "$input" | jq -r '.session_id // empty')
transcript_path=$(echo "$input" | jq -r '.transcript_path // empty')

# Check if there's a TODO file for this session
todo_file="$HOME/.claude/todos/${session_id}.json"

if [ -f "$todo_file" ]; then
    # Check for any pending or in_progress todos
    incomplete=$(jq '[.todos[] | select(.status != "completed")] | length' "$todo_file")

    if [ "$incomplete" -gt 0 ]; then
        # Block if there are incomplete todos
        echo "{\"continue\":false,\"stopReason\":\"There are $incomplete incomplete TODO items. Please complete all TODOs before finishing the task.\"}"
        exit 0
    fi
fi

# Allow continuation if no incomplete todos
echo '{"continue":true}'