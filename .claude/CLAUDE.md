# Claude Code Configuration

This file contains configuration and useful commands for Claude Code when working with this project.

## Project Overview

This is a Spring Boot application built with Kotlin and Gradle, following Domain-Driven Design (DDD) principles.

## Key Technologies

- **Language**: Kotlin
- **Framework**: Spring Boot
- **Build Tool**: Gradle
- **JDK**: 21+
- **Architecture**: Domain-Driven Design (DDD)

## Project Structure

- `beat-the-machine/` - Main application module
- `beat-the-machine-domain/` - Domain layer module
- `buildSrc/` - Build configuration
- `docs/` - Documentation
  - `docs/tasks.md` - **Master task list organized by DDD layers (Domain, Application, Adapters)**

## Task Management Agreement

**IMPORTANT**: All implementation work is tracked in `docs/tasks.md`. This file contains:
- Comprehensive task breakdown by architectural layer (Domain, Application, Adapters)
- Status tracking for all tasks (TODO, IN_PROGRESS, DONE)
- Task IDs for easy reference (e.g., D1.1, A2.3, AD5.2)

**Workflow**:
1. **Before starting work**: Check `docs/tasks.md` for the current task list
2. **During work**: Update task status in `docs/tasks.md` as you progress
3. **Add new tasks**: When discovering new requirements, add them to the appropriate section
4. **Reference tasks**: Use task IDs when discussing specific work items
5. **Always keep synchronized**: The task list must reflect the current project state

## Domain Model Summary

The Beat The Machine application implements an AI-generated image guessing game following DDD principles.

### Game Context
Players view AI-generated images and attempt to guess the original text prompt that was used to generate the image. The game provides feedback on each word guess similar to Wordle-style games:
- **Green**: Correct word in correct position
- **Yellow**: Correct word in wrong position
- **Red**: Word not in the original prompt

### Value Objects (Immutable)
- **Word**: Single word with validation (no spaces), case normalization, and obfuscation capability. Located in `domain` package.
- **Prompt**: Ordered list of words representing the AI prompt, with infix functions for natural language (`prompt contains word`). Factory methods support varargs and space-separated text. Located in `domain.riddle` package.
- **ImageUrl**: Validated URL pointing to the AI-generated image using `runCatching` for idiomatic validation. Located in `domain.riddle` package.
- **GameId**: UUID-based unique identifier for game sessions. Located in `domain.game` package.
- **Guess**: Collection of words representing a player's complete guess with factory methods (`of()`, `from()`). Located in `domain.riddle` package.
- **WordFeedback**: Combines a word with its evaluation status. Contains nested `Status` enum (CORRECT_POSITION, WRONG_POSITION, WRONG_WORD). Located in `domain.riddle` package.
- **GuessResult**: Value class wrapping list of WordFeedback. Simple data container with no business logic. Located in `domain.riddle` package.

### Entities
- **Riddle**: Rich domain entity containing prompt and image URL. Contains ALL evaluation business logic moved from GuessResult. Evaluates guesses and returns detailed feedback. Located in `domain.riddle` package.
- **Game**: (Planned) Aggregate root managing multiple riddles and overall game state

### Core Business Behaviors

#### Riddle Evaluation
- **Guess Processing**: Riddle entity evaluates complete guesses against the original prompt using `riddle.evaluate(guess)` method
- **Position-based Feedback**: Determines if words are correct position, wrong position, or completely wrong
- **Rich Domain Logic**: ALL evaluation logic resides in Riddle entity, not in static utility methods
- **Clean Function Structure**: Higher-level functions above lower-level ones for top-to-bottom readability
- **Evaluation Algorithm**:
  - `evaluate(guess)` → main business method
  - `determineWordStatus(word, index)` → determines individual word status
  - `isCorrectPosition(word, index)` → checks exact position match
  - `isWrongPosition(word)` → checks if word exists in prompt

#### Word Management
- **Single Word Constraint**: Prevents multi-word inputs in individual Word objects
- **Case Normalization**: Automatically converts all words to lowercase for consistent comparison
- **Obfuscation**: Words can render themselves as masked strings (e.g., "hello" → "-----")

#### Domain Language
- **Expressive API**: Uses infix functions for natural language (`prompt contains word`)
- **Ubiquitous Language**: Method names and concepts directly reflect business terminology

### Business Rules Enforced
- **Word Validation**: Single words only, no spaces allowed
- **URL Validation**: Image URLs must be valid format
- **Prompt Integrity**: Prompts cannot be blank
- **Case Insensitivity**: All text comparisons normalized to lowercase
- **Rich Feedback**: Every guess returns detailed positional feedback for UI rendering

### Domain Invariants
- Words are always single tokens without spaces
- Prompts are always non-empty collections of words (validated at creation)
- Guesses are always non-empty collections of words (validated at creation)
- Game sessions have unique, immutable identities
- Riddle evaluation always returns feedback for every guessed word
- All domain objects are properly validated upon creation
- GuessResult is a simple value class with no business behavior
- Evaluation logic lives exclusively in the Riddle entity

## Development Principles

### Test-Driven Development (TDD)
This project MUST follow Test-Driven Development practices with strict Red-Green-Refactor cycles:

#### TDD Cycle Requirements:
- **RED**: Write ONE minimal failing test that defines the next small piece of functionality
- **GREEN**: Write the minimal code needed to make that specific test pass (avoid implementing more than required)
- **REFACTOR**: Improve code quality while keeping all tests green (mandatory step that cannot be skipped)
- **Repeat**: Continue with the next small increment

#### TDD Best Practices:
- Use the `red-test-creator` subagent to create failing tests first
- Use the `tdd-green-test` subagent to implement minimal code to make tests pass
- Use the `tdd-refactoring-agent` subagent to refactor code while keeping tests green
- Use the `qa-coverage-enforcer` subagent to ensure comprehensive test coverage
- **NEVER implement multiple requirements in one cycle**
- **ALWAYS complete the refactor phase** - skipping refactoring violates TDD principles
- ALL new functionality must be developed using the Red-Green-Refactor cycle

#### Test Structure Requirements:
- Use Given/When/Then comments for test structure clarity
- Use Kotest assertions (`shouldBe`, `shouldThrow`) instead of JUnit assertions
- Prefer `.not()` over `!` for boolean negation in Kotlin for better readability
- Keep Given/When/Then structure comments in tests - these are required for test readability

### Acceptance Test-Driven Development (ATDD)
ALL features MUST be validated using ATDD:
- **Feature-level tests** that validate complete user scenarios
- **End-to-end integration tests** using TestContainers
- **API contract tests** for all endpoints
- **Behavior-driven scenarios** using Cucumber or Kotest BDD
- Tests must be written from the user's perspective, not implementation details

### Domain-Driven Design (DDD)
This project strictly follows DDD principles and patterns:

#### Required DDD Concepts:
- **Value Objects**: Immutable objects representing descriptive aspects of the domain
- **Entities**: Objects with identity that can change over time
- **Aggregates**: Clusters of domain objects treated as a single unit
- **Aggregate Roots**: The only entry point to an aggregate
- **Domain Events**: Events that represent business-significant occurrences
- **Rich Domain Models**: Business logic encapsulated within domain objects
- **Domain Services**: Operations that don't naturally fit within entities or value objects
- **Repositories**: Abstractions for data access that speak the domain language
- **Factories**: Objects responsible for creating complex domain objects
- **Specifications**: Business rules that can be combined and tested

#### DDD Architecture Layers:
- **Domain Layer**: Core business logic, entities, value objects, domain services
- **Application Layer**: Orchestrates domain objects, handles use cases
- **Infrastructure Layer**: Technical implementations (persistence, external services)
- **Presentation Layer**: User interface and API controllers

#### DDD Tactical Patterns (ALL REQUIRED):
- Bounded Contexts with clear boundaries
- Ubiquitous Language throughout the codebase
- Anti-Corruption Layers when integrating with external systems
- Domain Events for decoupling and side effects
- CQRS (Command Query Responsibility Segregation) where appropriate

#### DDD Implementation Guidelines:
- **Rich Domain Models**: Avoid anemic domain models - business logic belongs in domain objects (e.g., Riddle contains evaluation logic, not GuessResult)
- **Value Objects as Kotlin Value Classes**: Use `@JvmInline value class` for simple data containers without behavior (e.g., GuessResult)
- **Data Classes for Rich Value Objects**: Use `data class` for value objects with validation and behavior (e.g., Word, Prompt, Guess)
- **Expressive Domain Language**: Use infix functions and extension functions for natural business language (`prompt contains word`)
- **Proper Dependency Direction**: Higher-level concepts should depend on lower-level ones (e.g., Prompt contains Word, not Word knows about Prompt)
- **Domain Object Construction**: Use companion object factories when you need validation, transformation, or multiple construction patterns (like varargs)
- **Nested Enums**: Place enums inside the classes where they make semantic sense (e.g., `WordFeedback.Status`)
- **Business Method Naming**: Method names should reflect business operations, not technical implementations
- **Function Organization**: Higher-level functions above lower-level ones for top-to-bottom readability
- **Clean Return Types**: Omit obvious return types (e.g., Boolean) for cleaner code
- **Kotlin Idiomatic Code**: Always prefer Kotlin idioms over Java patterns (e.g., prefer `runCatching` over try-catch, `.not()` over `!`, extension functions over utility classes, data classes over POJOs, etc.)

## Required Libraries and Dependencies

### Core Spring Boot Libraries (MUST USE):
- `spring-boot-starter-web` - Web layer
- `spring-boot-starter-data-jpa` - Data persistence
- `spring-boot-starter-validation` - Bean validation
- `spring-boot-starter-security` - Security framework
- `spring-boot-starter-actuator` - Monitoring and management

### Testing Libraries (MUST USE):
- `spring-boot-starter-test` - Testing framework
- `testcontainers` - Integration testing with real databases
- `mockk` - Mocking framework for Kotlin
- `kotest` - Kotlin testing framework
- `kotest-extensions-spring` - Kotest Spring integration
- `konsist` - Architecture testing for Kotlin
- `cucumber-java` - BDD testing for ATDD scenarios
- `cucumber-spring` - Cucumber Spring integration

### DDD Support Libraries (MUST USE):
- `jackson-module-kotlin` - JSON serialization for Kotlin
- `kotlin-reflect` - Reflection support
- Custom domain event publisher (implement if not exists)

### Database Libraries (MUST USE):
- `postgresql` - Database drivers
- `flyway-core` - Database migrations

### Validation and Mapping (MUST USE):
- `jackson` - Object mapping
- `hibernate-validator` - Bean validation implementation

## Code Quality

This project enforces strict code quality standards through multiple linters and validators.

### Required Quality Tools:
- **Spotless** - Code formatting and style enforcement
- **Diktat** - Kotlin code style analyzer
- **JaCoCo** - Code coverage measurement
- **Konsist** - Architecture testing
- **Detekt** - Static code analysis for Kotlin

### Quality Gates (ALL REQUIRED):
- **All tests must pass** (unit, integration, ATDD)
- **JaCoCo Code Coverage**: Minimum 97% line coverage
- **JaCoCo Branch Coverage**: Minimum 95% branch coverage
- **No Spotless violations** - Code must be properly formatted
- **No Diktat violations** - Kotlin style guide compliance
- **No Detekt violations** - Static analysis issues must be resolved
- **No Konsist violations** - Architecture rules must be enforced
- **All ATDD scenarios must pass** - Feature acceptance criteria met
- **All domain concepts must follow DDD patterns**

### Build Integration:
All quality tools are integrated into the build pipeline. The `./gradlew check` command runs:
- All tests (unit, integration, architecture)
- JaCoCo coverage verification (97% line, 95% branch)
- Detekt static code analysis
- Diktat Kotlin style checking
- Konsist architecture testing

## Important Development Guidelines

### Library and Technology Usage:
- **NEVER make assumptions** about which libraries or frameworks are available
- **ALWAYS check existing dependencies** in build.gradle.kts before suggesting or using any library
- **ONLY use libraries explicitly listed** in the "Required Libraries" section above
- **If a library is needed but not listed**, ask the user before proceeding
- **Check neighboring files and imports** to understand existing patterns and frameworks

### Architecture Verification:
- Use **Konsist** (not ArchUnit) for architecture testing
- Verify all architectural constraints with Konsist tests
- Ensure DDD layer boundaries are enforced through Konsist rules

## Claude Code Behavior Guidelines

### Code Comments Policy:
- **ABSOLUTELY NEVER ADD ANY CODE COMMENTS** unless explicitly requested by the user
- **DO NOT ADD COMMENTS OF ANY KIND** - this includes implementation comments, documentation comments, TODO comments, or any other form of code comments
- **EXCEPTION: Given/When/Then comments in tests are REQUIRED** - these structure comments must be kept in test files
- **NEVER add comments even if you think they would be helpful** - only add them when explicitly asked
- Code should be self-documenting through clear naming and structure
- This is a STRICT requirement that must be followed without exception

### Task Execution and Planning:
- **ALWAYS create and show an execution plan** before starting any multi-step task
- **ALWAYS use the TodoWrite tool** to track task progress and show execution status
- **ALWAYS update todo status in real-time** as tasks are completed
- **SHOW PROGRESS VISIBILITY** to the user throughout task execution
- Break down complex tasks into smaller, trackable steps
- Mark tasks as in_progress, completed, or pending to provide clear status updates

### Dependency Management Policy:
- **MANDATORY**: Use the `dependency-manager` subagent for ANY dependency-related work
- This includes: adding libraries, updating dependencies, discussing build files, mentioning any library names, configuring plugins, or ANY conversation involving build.gradle.kts, version catalogs, or dependency management
- **NEVER work with dependencies directly** - always delegate to the dependency-manager agent first
- This ensures proper version catalog management and follows project dependency standards
