#!/usr/bin/env bash

# Read JSON input from stdin
input=$(cat)

# Extract session ID from JSON input
session_id=$(echo "$input" | jq -r '.session_id // empty')

# Check if there's a TODO file for this session
todo_file="$HOME/.claude/todos/${session_id}.json"

if [ -f "$todo_file" ]; then
    # Check for any pending or in_progress todos
    incomplete=$(jq '[.todos[] | select(.status != "completed")] | length' "$todo_file")

    if [ "$incomplete" -eq 0 ]; then
        # All TODOs are completed, run comprehensive quality checks
        echo "✅ All TODOs completed! Running comprehensive quality checks..."
        echo "📊 Validating:"
        echo "   - 97% line coverage (JaCoCo)"
        echo "   - 95% branch coverage (JaCoCo)"
        echo "   - 100% mutation coverage (PITest)"
        echo "   - All tests passing"
        echo "   - Code quality (Detekt, Diktat, Spotless)"
        echo ""

        cd "$(dirname "$0")/.." || exit 1
        ./gradlew check
        exit_code=$?

        if [ $exit_code -ne 0 ]; then
          echo ""
          echo "❌ Quality gates failed!"
          echo "   Please ensure:"
          echo "   - JaCoCo line coverage ≥ 97%"
          echo "   - JaCoCo branch coverage ≥ 95%"
          echo "   - PITest mutation coverage = 100%"
          echo "   - All tests pass"
          echo "   - No linting violations"
          exit 2  # Block and show error to Claude
        else
          echo ""
          echo "✅ All quality gates passed!"
          echo "   ✓ Code coverage requirements met"
          echo "   ✓ Mutation testing passed"
          echo "   ✓ All tests passing"
          echo "   ✓ Code quality validated"
          exit 0
        fi
    else
        # Still have incomplete TODOs, skip check
        exit 0
    fi
else
    # No TODO file, skip check
    exit 0
fi