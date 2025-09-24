# Open Tasks

## Domain

### Status Legend
- [ ] TODO - Not started
- [🚧] IN_PROGRESS - Currently being worked on
- [✅] DONE - Completed

### Current Domain State
- [✅] **Value Objects**: Word, Prompt, ImageUrl, GameId, Guess, WordFeedback, GuessResult
- [✅] **Entity**: Riddle (with evaluation business logic)

### 1. Game Aggregate Root Implementation
- [ ] **Task 1.1**: Create Game entity as aggregate root in `domain/game/Game.kt`
  - Include: gameId, riddles collection, current riddle index, game state, attempt tracking
  - Business methods: startGame(), submitGuess(), nextRiddle(), isComplete(), getCurrentRiddle()
  - Enforce game rules: attempt limits, riddle progression, win/lose conditions

### 2. Domain Events System
- [ ] **Task 2.1**: Create base DomainEvent interface in `domain/events/DomainEvent.kt`
  - Include timestamp and aggregateId properties
- [ ] **Task 2.2**: Create GameStarted event in `domain/events/GameStarted.kt`
- [ ] **Task 2.3**: Create GuessSubmitted event in `domain/events/GuessSubmitted.kt`
- [ ] **Task 2.4**: Create RiddleSolved event in `domain/events/RiddleSolved.kt`
- [ ] **Task 2.5**: Create GameCompleted event in `domain/events/GameCompleted.kt`

### 3. Game State Management
- [ ] **Task 3.1**: Create GameState enum in `domain/game/GameState.kt`
  - Values: NOT_STARTED, IN_PROGRESS, COMPLETED, ABANDONED
  - Track game progression and completion status

### 4. Attempt Tracking Value Objects
- [ ] **Task 4.1**: Create AttemptNumber value object in `domain/game/AttemptNumber.kt`
- [ ] **Task 4.2**: Create MaxAttempts value object in `domain/game/MaxAttempts.kt`
- [ ] **Task 4.3**: Create AttemptHistory value object in `domain/game/AttemptHistory.kt`
  - Collection of all guesses made for a riddle

### 5. Repository Interfaces
- [ ] **Task 5.1**: Create GameRepository interface in `domain/GameRepository.kt`
  - Methods: save(), findById(), delete()
- [ ] **Task 5.2**: Create RiddleRepository interface in `domain/RiddleRepository.kt`
  - Methods: findRandom(), findByDifficulty(), findAll()

### 6. Domain Services
- [ ] **Task 6.1**: Create GameProgressionService in `domain/services/GameProgressionService.kt`
  - Handle riddle sequencing logic
- [ ] **Task 6.2**: Create ScoreCalculationService in `domain/services/ScoreCalculationService.kt`
  - Calculate scores based on attempts/time
- [ ] **Task 6.3**: Create RiddleSelectionService in `domain/services/RiddleSelectionService.kt`
  - Smart riddle selection algorithms

### 7. Factory Objects
- [ ] **Task 7.1**: Create GameFactory in `domain/factories/GameFactory.kt`
  - Create new games with riddle sets
- [ ] **Task 7.2**: Create RiddleFactory in `domain/factories/RiddleFactory.kt`
  - Generate riddles from prompts and images

### 8. Specifications Pattern
- [ ] **Task 8.1**: Create GameCompletionSpecification in `domain/specifications/GameCompletionSpecification.kt`
- [ ] **Task 8.2**: Create ValidGuessSpecification in `domain/specifications/ValidGuessSpecification.kt`
- [ ] **Task 8.3**: Create RiddleProgressionSpecification in `domain/specifications/RiddleProgressionSpecification.kt`

### 9. Domain Exception Hierarchy
- [ ] **Task 9.1**: Create DomainException base class in `domain/exceptions/DomainException.kt`
- [ ] **Task 9.2**: Create InvalidGameStateException in `domain/exceptions/InvalidGameStateException.kt`
- [ ] **Task 9.3**: Create ExceededAttemptsException in `domain/exceptions/ExceededAttemptsException.kt`
- [ ] **Task 9.4**: Create InvalidGuessException in `domain/exceptions/InvalidGuessException.kt`

### 10. Enhanced Business Logic
- [ ] **Task 10.1**: Add time tracking capabilities to Game entity
- [ ] **Task 10.2**: Implement hint system with penalty calculations
- [ ] **Task 10.3**: Add difficulty levels affecting scoring
- [ ] **Task 10.4**: Add multi-player game support foundation

### 11. Architecture Testing
- [ ] **Task 11.1**: Create Konsist tests for DDD layer boundaries in `test/architecture/`
- [ ] **Task 11.2**: Verify no infrastructure dependencies in domain
- [ ] **Task 11.3**: Validate aggregate boundaries and invariants

### 12. Integration Testing
- [ ] **Task 12.1**: Create end-to-end game workflow tests
- [ ] **Task 12.2**: Verify domain events are properly fired
- [ ] **Task 12.3**: Test aggregate consistency and invariants

## Quality Guards

### Status Legend
- [ ] TODO - Not started
- [🚧] IN_PROGRESS - Currently being worked on
- [✅] DONE - Completed

### Quality Tools Setup
- [ ] **QG 1**: Configure Spotless for code formatting
- [ ] **QG 2**: Configure Diktat for Kotlin style checking
- [ ] **QG 3**: Configure Detekt for static code analysis
- [ ] **QG 4**: Configure JaCoCo with 97% line coverage requirement
- [ ] **QG 5**: Configure Konsist for architecture testing
- [ ] **QG 6**: Integrate all quality checks into `./gradlew check` command
- [ ] **QG 7**: Create quality gate validation in CI/CD pipeline