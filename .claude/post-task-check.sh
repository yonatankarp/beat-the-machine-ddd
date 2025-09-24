#!/usr/bin/env bash

# Read JSON input from stdin
input=$(cat)

# Extract tool name from JSON input
tool_name=$(echo "$input" | jq -r '.tool_name // empty')

# Only run check for tools that modify code
case "$tool_name" in
  Write|Edit|MultiEdit)
    echo "Running ./gradlew check after code modification..."
    cd "$(dirname "$0")/.." || exit 1
    ./gradlew check
    exit_code=$?

    if [ $exit_code -ne 0 ]; then
      echo "❌ ./gradlew check failed with exit code $exit_code"
      exit 2  # Block and show error to Claude
    else
      echo "✅ ./gradlew check passed"
      exit 0
    fi
    ;;
  *)
    # For other tools, just pass through
    exit 0
    ;;
esac