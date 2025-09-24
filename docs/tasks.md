# Project Implementation Tasks

This document tracks all implementation tasks organized by DDD architectural layers. Tasks are tracked with status indicators and updated as work progresses.

## Status Legend
- [ ] TODO - Not started
- [🚧] IN_PROGRESS - Currently being worked on
- [✅] DONE - Completed

---

## Domain Layer Tasks

### Current Domain State (Completed)
- [✅] **Value Objects**: Word, Prompt, ImageUrl, GameId, Guess, WordFeedback, GuessResult
- [✅] **Entity**: Riddle (with evaluation business logic)

### 1. Game Aggregate Root
- [ ] **D1.1**: Create Game entity as aggregate root in `domain/game/Game.kt`
  - Include: gameId, riddles collection, current riddle index, game state, attempt tracking
  - Business methods: startGame(), submitGuess(), nextRiddle(), isComplete(), getCurrentRiddle()
  - Enforce game rules: attempt limits, riddle progression, win/lose conditions

### 2. Domain Events System
- [ ] **D2.1**: Create base DomainEvent interface in `domain/events/DomainEvent.kt`
  - Include timestamp and aggregateId properties
- [ ] **D2.2**: Create GameStarted event in `domain/events/GameStarted.kt`
- [ ] **D2.3**: Create GuessSubmitted event in `domain/events/GuessSubmitted.kt`
- [ ] **D2.4**: Create RiddleSolved event in `domain/events/RiddleSolved.kt`
- [ ] **D2.5**: Create GameCompleted event in `domain/events/GameCompleted.kt`

### 3. Game State Management
- [ ] **D3.1**: Create GameState enum in `domain/game/GameState.kt`
  - Values: NOT_STARTED, IN_PROGRESS, COMPLETED, ABANDONED
  - Track game progression and completion status

### 4. Attempt Tracking Value Objects
- [ ] **D4.1**: Create AttemptNumber value object in `domain/game/AttemptNumber.kt`
- [ ] **D4.2**: Create MaxAttempts value object in `domain/game/MaxAttempts.kt`
- [ ] **D4.3**: Create AttemptHistory value object in `domain/game/AttemptHistory.kt`
  - Collection of all guesses made for a riddle

### 5. Repository Interfaces (Domain Layer)
- [ ] **D5.1**: Create GameRepository interface in `domain/GameRepository.kt`
  - Methods: save(), findById(), delete()
- [ ] **D5.2**: Create RiddleRepository interface in `domain/RiddleRepository.kt`
  - Methods: findRandom(), findByDifficulty(), findAll()

### 6. Domain Services
- [ ] **D6.1**: Create GameProgressionService in `domain/services/GameProgressionService.kt`
  - Handle riddle sequencing logic
- [ ] **D6.2**: Create ScoreCalculationService in `domain/services/ScoreCalculationService.kt`
  - Calculate scores based on attempts/time
- [ ] **D6.3**: Create RiddleSelectionService in `domain/services/RiddleSelectionService.kt`
  - Smart riddle selection algorithms

### 7. Factory Objects
- [ ] **D7.1**: Create GameFactory in `domain/factories/GameFactory.kt`
  - Create new games with riddle sets
- [ ] **D7.2**: Create RiddleFactory in `domain/factories/RiddleFactory.kt`
  - Generate riddles from prompts and images

### 8. Specifications Pattern
- [ ] **D8.1**: Create GameCompletionSpecification in `domain/specifications/GameCompletionSpecification.kt`
- [ ] **D8.2**: Create ValidGuessSpecification in `domain/specifications/ValidGuessSpecification.kt`
- [ ] **D8.3**: Create RiddleProgressionSpecification in `domain/specifications/RiddleProgressionSpecification.kt`

### 9. Domain Exception Hierarchy
- [ ] **D9.1**: Create DomainException base class in `domain/exceptions/DomainException.kt`
- [ ] **D9.2**: Create InvalidGameStateException in `domain/exceptions/InvalidGameStateException.kt`
- [ ] **D9.3**: Create ExceededAttemptsException in `domain/exceptions/ExceededAttemptsException.kt`
- [ ] **D9.4**: Create InvalidGuessException in `domain/exceptions/InvalidGuessException.kt`

### 10. Enhanced Business Logic
- [ ] **D10.1**: Add time tracking capabilities to Game entity
- [ ] **D10.2**: Implement hint system with penalty calculations
- [ ] **D10.3**: Add difficulty levels affecting scoring
- [ ] **D10.4**: Add multi-player game support foundation

### 11. Domain Architecture Testing
- [ ] **D11.1**: Create Konsist tests for DDD layer boundaries in `test/architecture/`
- [ ] **D11.2**: Verify no infrastructure dependencies in domain
- [ ] **D11.3**: Validate aggregate boundaries and invariants

### 12. Domain Integration Testing
- [ ] **D12.1**: Create end-to-end game workflow tests
- [ ] **D12.2**: Verify domain events are properly fired
- [ ] **D12.3**: Test aggregate consistency and invariants

---

## Application Layer Tasks

### 1. Use Cases / Application Services
- [ ] **A1.1**: Create StartGameUseCase in `application/usecases/StartGameUseCase.kt`
  - Initialize new game session
  - Select riddles for the game
- [ ] **A1.2**: Create SubmitGuessUseCase in `application/usecases/SubmitGuessUseCase.kt`
  - Process player guess
  - Return feedback
  - Update game state
- [ ] **A1.3**: Create GetCurrentRiddleUseCase in `application/usecases/GetCurrentRiddleUseCase.kt`
  - Retrieve current riddle for display
- [ ] **A1.4**: Create CompleteGameUseCase in `application/usecases/CompleteGameUseCase.kt`
  - Finalize game session
  - Calculate final score
- [ ] **A1.5**: Create GetGameStatusUseCase in `application/usecases/GetGameStatusUseCase.kt`
  - Retrieve current game state and progress

### 2. Command Objects (CQRS)
- [ ] **A2.1**: Create StartGameCommand in `application/commands/StartGameCommand.kt`
- [ ] **A2.2**: Create SubmitGuessCommand in `application/commands/SubmitGuessCommand.kt`
- [ ] **A2.3**: Create CompleteGameCommand in `application/commands/CompleteGameCommand.kt`

### 3. Query Objects (CQRS)
- [ ] **A3.1**: Create GetGameQuery in `application/queries/GetGameQuery.kt`
- [ ] **A3.2**: Create GetRiddleQuery in `application/queries/GetRiddleQuery.kt`
- [ ] **A3.3**: Create GetGameHistoryQuery in `application/queries/GetGameHistoryQuery.kt`

### 4. DTOs (Data Transfer Objects)
- [ ] **A4.1**: Create GameDto in `application/dto/GameDto.kt`
- [ ] **A4.2**: Create RiddleDto in `application/dto/RiddleDto.kt`
- [ ] **A4.3**: Create GuessResultDto in `application/dto/GuessResultDto.kt`
- [ ] **A4.4**: Create GameStatusDto in `application/dto/GameStatusDto.kt`

### 5. Application Event Handlers
- [ ] **A5.1**: Create GameStartedEventHandler in `application/events/GameStartedEventHandler.kt`
- [ ] **A5.2**: Create RiddleSolvedEventHandler in `application/events/RiddleSolvedEventHandler.kt`
- [ ] **A5.3**: Create GameCompletedEventHandler in `application/events/GameCompletedEventHandler.kt`

### 6. Application Services
- [ ] **A6.1**: Create GameApplicationService in `application/services/GameApplicationService.kt`
  - Orchestrate use cases
  - Transaction management
- [ ] **A6.2**: Create RiddleApplicationService in `application/services/RiddleApplicationService.kt`
  - Riddle retrieval and management

### 7. Application Exception Handling
- [ ] **A7.1**: Create ApplicationException base class
- [ ] **A7.2**: Create GameNotFoundException
- [ ] **A7.3**: Create InvalidCommandException
- [ ] **A7.4**: Create ConcurrentModificationException

### 8. Validation
- [ ] **A8.1**: Create command validators using Bean Validation
- [ ] **A8.2**: Create query validators
- [ ] **A8.3**: Add cross-field validation logic

---

## Adapters Layer Tasks

### 1. REST API Controllers (Inbound Adapters)
- [ ] **AD1.1**: Create GameController in `adapters/in/web/GameController.kt`
  - POST /api/games - Start new game
  - GET /api/games/{id} - Get game status
  - POST /api/games/{id}/guess - Submit guess
  - POST /api/games/{id}/complete - Complete game
- [ ] **AD1.2**: Create RiddleController in `adapters/in/web/RiddleController.kt`
  - GET /api/riddles/{id} - Get riddle details
  - GET /api/riddles/random - Get random riddle

### 2. Request/Response Models
- [ ] **AD2.1**: Create StartGameRequest in `adapters/in/web/requests/StartGameRequest.kt`
- [ ] **AD2.2**: Create SubmitGuessRequest in `adapters/in/web/requests/SubmitGuessRequest.kt`
- [ ] **AD2.3**: Create GameResponse in `adapters/in/web/responses/GameResponse.kt`
- [ ] **AD2.4**: Create GuessResultResponse in `adapters/in/web/responses/GuessResultResponse.kt`

### 3. Repository Implementations (Outbound Adapters)
- [ ] **AD3.1**: Create JpaGameRepository in `adapters/out/persistence/JpaGameRepository.kt`
  - Implement GameRepository interface
  - Map domain to JPA entities
- [ ] **AD3.2**: Create JpaRiddleRepository in `adapters/out/persistence/JpaRiddleRepository.kt`
  - Implement RiddleRepository interface
  - Map domain to JPA entities

### 4. JPA Entity Models
- [ ] **AD4.1**: Create GameEntity in `adapters/out/persistence/entities/GameEntity.kt`
- [ ] **AD4.2**: Create RiddleEntity in `adapters/out/persistence/entities/RiddleEntity.kt`
- [ ] **AD4.3**: Create GuessEntity in `adapters/out/persistence/entities/GuessEntity.kt`

### 5. Domain-to-Entity Mappers
- [ ] **AD5.1**: Create GameMapper in `adapters/out/persistence/mappers/GameMapper.kt`
- [ ] **AD5.2**: Create RiddleMapper in `adapters/out/persistence/mappers/RiddleMapper.kt`

### 6. Database Migrations (Flyway)
- [ ] **AD6.1**: Create V1__create_games_table.sql
- [ ] **AD6.2**: Create V2__create_riddles_table.sql
- [ ] **AD6.3**: Create V3__create_guesses_table.sql
- [ ] **AD6.4**: Create V4__add_indexes.sql

### 7. External API Integration (AI Image Generation)
- [ ] **AD7.1**: Create AIImageGenerationService in `adapters/out/ai/`
- [ ] **AD7.2**: Create ImageGenerationAdapter
- [ ] **AD7.3**: Add anti-corruption layer for AI service

### 8. Exception Handling & Error Responses
- [ ] **AD8.1**: Create GlobalExceptionHandler with @ControllerAdvice
- [ ] **AD8.2**: Create ErrorResponse model
- [ ] **AD8.3**: Map domain exceptions to HTTP status codes

### 9. API Documentation
- [ ] **AD9.1**: Configure OpenAPI/Swagger
- [ ] **AD9.2**: Add API documentation annotations
- [ ] **AD9.3**: Create API usage examples

### 10. Security Configuration
- [ ] **AD10.1**: Configure Spring Security
- [ ] **AD10.2**: Add authentication endpoints
- [ ] **AD10.3**: Implement JWT token handling
- [ ] **AD10.4**: Add authorization rules

### 11. Integration Testing (Adapters)
- [ ] **AD11.1**: Create REST API integration tests with TestContainers
- [ ] **AD11.2**: Create repository integration tests
- [ ] **AD11.3**: Create end-to-end ATDD scenarios with Cucumber

---

## Cross-Cutting Concerns

### 1. Configuration
- [ ] **CC1.1**: Create application.yml configuration
- [ ] **CC1.2**: Create profiles (dev, test, prod)
- [ ] **CC1.3**: Configure database connections
- [ ] **CC1.4**: Configure external service URLs

### 2. Logging & Monitoring
- [ ] **CC2.1**: Configure structured logging
- [ ] **CC2.2**: Add correlation IDs
- [ ] **CC2.3**: Configure Spring Actuator endpoints
- [ ] **CC2.4**: Add metrics and health checks

### 3. Quality Assurance
- [ ] **CC3.1**: Configure Spotless for code formatting
- [ ] **CC3.2**: Configure Diktat for Kotlin style checking
- [ ] **CC3.3**: Configure Detekt for static code analysis
- [ ] **CC3.4**: Configure JaCoCo with 97% line coverage requirement
- [ ] **CC3.5**: Configure Konsist for architecture testing
- [ ] **CC3.6**: Integrate all quality checks into `./gradlew check` command
- [ ] **CC3.7**: Create quality gate validation in CI/CD pipeline

### 4. Build & Deployment
- [ ] **CC4.1**: Create Docker configuration
- [ ] **CC4.2**: Create docker-compose for local development
- [ ] **CC4.3**: Configure CI/CD pipeline
- [ ] **CC4.4**: Add deployment scripts

---

## Notes

- All tasks follow TDD with Red-Green-Refactor cycles
- ATDD scenarios required for all user-facing features
- Architecture tests validate DDD boundaries
- Minimum 97% code coverage required
- All quality gates must pass before merging