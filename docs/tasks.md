# Project Implementation Tasks

This document tracks all implementation tasks organized by DDD architectural layers. Tasks are tracked with status indicators and updated as work progresses.

## Status Legend
- [ ] TODO - Not started
- [🚧] IN_PROGRESS - Currently being worked on
- [✅] DONE - Completed

---

## Domain Layer Tasks

### Current Domain State (Completed)
- [✅] **Value Objects**:
  - Word (riddle package) - single word with validation, case normalization, obfuscation
  - Prompt (riddle package) - ordered list of words, factory methods (of/from)
  - ImageUrl (riddle package) - validated URL as @JvmInline value class
  - Guess (riddle package) - player's guess collection, factory methods (of/from)
  - WordFeedback (riddle package) - word + status with nested Status enum
  - GuessResult (riddle package) - @JvmInline value class with isAllCorrect property and index operator
  - AttemptNumber (game package) - @JvmInline value class with validation
  - MaxAttempts (game package) - @JvmInline value class with validation
  - AttemptHistory (game package) - collection of guesses with isEmpty and count properties
- [✅] **Entities**:
  - Riddle (riddle package) - rich domain entity with ALL evaluation business logic
  - Game (game package) - aggregate root with nested Game.Id and Game.State
- [✅] **Nested Types**:
  - Game.Id - UUID-based game identifier (nested in Game)
  - Game.State - game state enum: ABANDONED, COMPLETED, IN_PROGRESS (nested in Game)
- [✅] **Refactorings Completed**:
  - Feature envy eliminated from tests (GuessResult API improvements)
  - Functions converted to properties where applicable
  - ImageUrl optimized as @JvmInline value class

### 1. Game Aggregate Root
- [ ] **D1.1**: Create Game entity as aggregate root in `domain/game/Game.kt`
  - ✅ gameId (as Game.Id nested type)
  - ✅ riddles collection
  - ✅ currentRiddleIndex tracking
  - ✅ game state (as Game.State nested enum)
  - ✅ attempt tracking with AttemptHistory
  - ✅ Business methods: start(), submitGuess(), nextRiddle()
  - ✅ Computed properties: isComplete, currentRiddle
  - [ ] Enforce game rules: attempt limits, win/lose conditions
- [ ] **D1.2**: Add Player aggregate root in `domain/player/Player.kt`
  - playerId as Player.Id value object
  - activeGameId (reference to current game)
  - playerStats (games played, wins, etc.)
  - Business method: startNewGame()
  - Enforce: one active game per player rule
- [ ] **D1.3**: Enhance Game aggregate with attempt limit logic
  - maxAttempts calculation based on prompt size
  - attemptLimitExceeded() method
  - giveUp() business method (show answer, no points, move to next)
  - Computed property: canSubmitGuess (checks attempt limit)

### 2. Domain Events System
- [ ] **D2.1**: Create base DomainEvent interface in `domain/events/DomainEvent.kt`
  - Include timestamp and aggregateId properties
- [ ] **D2.2**: Create GameStarted event in `domain/events/GameStarted.kt`
- [ ] **D2.3**: Create GuessSubmitted event in `domain/events/GuessSubmitted.kt`
- [ ] **D2.4**: Create RiddleSolved event in `domain/events/RiddleSolved.kt`
- [ ] **D2.5**: Create GameCompleted event in `domain/events/GameCompleted.kt`
- [ ] **D2.6**: Create PlayerGaveUp event in `domain/events/PlayerGaveUp.kt`
- [ ] **D2.7**: Create AttemptLimitExceeded event in `domain/events/AttemptLimitExceeded.kt`
- [ ] **D2.8**: Create RiddleQueueLow event in `domain/events/RiddleQueueLow.kt`
  - Trigger async riddle generation when queue is low
- [ ] **D2.9**: Create RiddleGenerated event in `domain/events/RiddleGenerated.kt`
  - Published when new riddle is created

### 3. Game State Management
- [✅] **D3.1**: Create GameState enum
  - ✅ Implemented as nested Game.State enum
  - ✅ Values: ABANDONED, COMPLETED, IN_PROGRESS
  - ✅ Track game progression and completion status

### 4. Attempt Tracking Value Objects
- [✅] **D4.1**: Create AttemptNumber value object in `domain/game/AttemptNumber.kt`
  - ✅ @JvmInline value class with validation (value >= 0)
  - ✅ increment() method
- [✅] **D4.2**: Create MaxAttempts value object in `domain/game/MaxAttempts.kt`
  - ✅ @JvmInline value class with validation (value > 0)
  - ✅ isExceeded() method
- [✅] **D4.3**: Create AttemptHistory value object in `domain/game/AttemptHistory.kt`
  - ✅ Collection of all guesses made for a riddle
  - ✅ isEmpty and count properties (converted from functions)
  - ✅ addGuess() method
  - ✅ Factory method: empty()

### 5. Repository Interfaces (Domain Layer - Output Ports)
- [ ] **D5.1**: Create GameRepository interface in `domain/GameRepository.kt`
  - Methods: save(), findById(), delete()
- [ ] **D5.2**: Create RiddleRepository interface in `domain/RiddleRepository.kt`
  - Methods: findRandom(), findByDifficulty(), findAll()
  - findAvailableCount() - check riddle queue status
  - findBatch(size: Int) - get batch of riddles
- [ ] **D5.3**: Create PlayerRepository interface in `domain/PlayerRepository.kt`
  - Methods: save(), findById(), findByUsername()
  - findActiveGame(playerId) - get player's active game

### 6. Domain Services
- [ ] **D6.1**: Create GameProgressionService in `domain/services/GameProgressionService.kt`
  - Handle riddle sequencing logic
  - Continuous play logic (game continues while riddles available)
- [ ] **D6.2**: Create ScoreCalculationService in `domain/services/ScoreCalculationService.kt`
  - Calculate scores based on attempts/time (future implementation)
- [ ] **D6.3**: Create RiddleSelectionService in `domain/services/RiddleSelectionService.kt`
  - Smart riddle selection algorithms
- [ ] **D6.4**: Create WordSimilarityService in `domain/services/WordSimilarityService.kt`
  - Dictionary-based word matching
  - Stemming algorithm (e.g., Porter stemmer)
  - Synonym detection
  - Typo tolerance (Levenshtein distance)
  - Returns similarity score for word matching
- [ ] **D6.5**: Create AttemptLimitCalculator in `domain/services/AttemptLimitCalculator.kt`
  - Calculate max attempts based on prompt word count
  - Configurable formula (e.g., wordCount * multiplier + base)
  - Business rule: ensure fair gameplay based on difficulty

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
- [ ] **D9.5**: Create NoRiddlesAvailableException in `domain/exceptions/NoRiddlesAvailableException.kt`
- [ ] **D9.6**: Create PlayerAlreadyHasActiveGameException in `domain/exceptions/PlayerAlreadyHasActiveGameException.kt`

### 10. Enhanced Business Logic
- [ ] **D10.1**: Add time tracking capabilities to Game entity
- [ ] **D10.2**: Implement hint system with penalty calculations (future)
- [ ] **D10.3**: Add difficulty levels affecting scoring (future)
- [ ] **D10.4**: Add multi-player game support foundation (future)
- [ ] **D10.5**: Enhance Riddle entity with RiddleStatus value object
  - Status enum: AVAILABLE, IN_PROGRESS, SOLVED, SKIPPED
  - markAsInProgress(), markAsSolved(), markAsSkipped() methods
- [ ] **D10.6**: Add RiddleQueue value object in `domain/riddle/RiddleQueue.kt`
  - Collection of available riddles
  - isLow() method (checks if below threshold)
  - needsGeneration() method
  - threshold configurable per environment

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

### Current Application State (Completed)
- [✅] **Application Module Structure**:
  - beat-the-machine-application module created
  - Gradle build configuration with domain dependency
  - Proper package structure following hexagonal architecture
- [✅] **DomainError sealed interface** in `domain/game/exceptions/DomainError.kt`:
  - GameNotFound inner class for game not found errors
  - Extends RuntimeException for proper error handling
- [✅] **ImageUrl optimization**: Converted to @JvmInline value class for performance

### 1. Use Cases / Application Services (Input Ports)
- [✅] **A1.1**: Create StartGameUseCase in `application/ports/input/StartGameUseCase.kt`
  - ✅ Input port interface with invoke operator
  - ✅ StartGame implementation in `application/usecases/StartGame.kt`
  - ✅ Uses FindAvailableRiddles output port
  - ✅ Returns new Game domain object
  - ✅ Follows TDD with comprehensive test coverage
  - ✅ All quality gates pass (97% coverage, 100% mutation kill)
  - [ ] Future: Check player doesn't have active game (one game per player)
  - [ ] Future: Check riddle availability (throw NoRiddlesAvailableException if empty)
- [✅] **A1.2**: Create SubmitGuessUseCase in `application/ports/input/SubmitGuessUseCase.kt`
  - ✅ Input port interface with invoke operator
  - ✅ SubmitGuess implementation in `application/usecases/SubmitGuess.kt`
  - ✅ Uses FindGameById and SaveGame output ports
  - ✅ Throws DomainError.GameNotFound when game not found
  - ✅ Updates game with submitted guess
  - ✅ Returns GuessResult with feedback
  - ✅ Follows TDD with comprehensive test coverage
  - ✅ All quality gates pass (100% coverage, 100% mutation kill)
  - [ ] Future: Process player guess with word similarity matching
  - [ ] Future: Check if attempt limit exceeded
  - [ ] Future: Publish GuessSubmitted event
- [ ] **A1.3**: Create GetCurrentRiddleUseCase in `application/usecases/GetCurrentRiddleUseCase.kt`
  - Retrieve current riddle for display
  - Check game state validity
- [ ] **A1.4**: Create CompleteGameUseCase in `application/usecases/CompleteGameUseCase.kt`
  - Finalize game session
  - Calculate final score (future)
  - Mark game as completed
- [ ] **A1.5**: Create GetGameStatusUseCase in `application/usecases/GetGameStatusUseCase.kt`
  - Retrieve current game state and progress
  - Include attempt count and limit
- [ ] **A1.6**: Create GiveUpRiddleUseCase in `application/usecases/GiveUpRiddleUseCase.kt`
  - Show riddle answer to player
  - No points awarded
  - Move to next riddle
  - Publish PlayerGaveUp event
- [ ] **A1.7**: Create CheckRiddleQueueStatusUseCase in `application/usecases/CheckRiddleQueueStatusUseCase.kt`
  - Check available riddle count
  - Return status: AVAILABLE, LOW, EMPTY
  - Trigger generation if low
- [ ] **A1.8**: Create GetPlayerActiveGameUseCase in `application/usecases/GetPlayerActiveGameUseCase.kt`
  - Get player's current active game
  - Return null if no active game

### 2. Command Objects (CQRS)
- [ ] **A2.1**: Create StartGameCommand in `application/commands/StartGameCommand.kt`
  - playerId field
- [ ] **A2.2**: Create SubmitGuessCommand in `application/commands/SubmitGuessCommand.kt`
  - gameId, guess fields
- [ ] **A2.3**: Create CompleteGameCommand in `application/commands/CompleteGameCommand.kt`
  - gameId field
- [ ] **A2.4**: Create GiveUpRiddleCommand in `application/commands/GiveUpRiddleCommand.kt`
  - gameId field
- [ ] **A2.5**: Create TriggerRiddleGenerationCommand in `application/commands/TriggerRiddleGenerationCommand.kt`
  - batchSize field (default from config)

### 3. Query Objects (CQRS)
- [ ] **A3.1**: Create GetGameQuery in `application/queries/GetGameQuery.kt`
  - gameId field
- [ ] **A3.2**: Create GetRiddleQuery in `application/queries/GetRiddleQuery.kt`
  - riddleId field
- [ ] **A3.3**: Create GetGameHistoryQuery in `application/queries/GetGameHistoryQuery.kt`
  - playerId field
- [ ] **A3.4**: Create GetRiddleQueueStatusQuery in `application/queries/GetRiddleQueueStatusQuery.kt`
  - No parameters, returns queue status
- [ ] **A3.5**: Create GetPlayerActiveGameQuery in `application/queries/GetPlayerActiveGameQuery.kt`
  - playerId field

### 4. DTOs (Data Transfer Objects)
- [ ] **A4.1**: Create GameDto in `application/dto/GameDto.kt`
  - Include attemptCount, maxAttempts, canSubmitGuess
- [ ] **A4.2**: Create RiddleDto in `application/dto/RiddleDto.kt`
  - Include status, obfuscated prompt for in-progress riddles
- [ ] **A4.3**: Create GuessResultDto in `application/dto/GuessResultDto.kt`
  - Include word feedback and attempt info
- [ ] **A4.4**: Create GameStatusDto in `application/dto/GameStatusDto.kt`
  - Include all game state information
- [ ] **A4.5**: Create RiddleQueueStatusDto in `application/dto/RiddleQueueStatusDto.kt`
  - availableCount, status (AVAILABLE/LOW/EMPTY), generationInProgress
- [ ] **A4.6**: Create PlayerDto in `application/dto/PlayerDto.kt`
  - Basic player information and stats

### 5. Application Event Handlers
- [ ] **A5.1**: Create GameStartedEventHandler in `application/events/GameStartedEventHandler.kt`
  - Log game start
  - Initialize player stats tracking
- [ ] **A5.2**: Create RiddleSolvedEventHandler in `application/events/RiddleSolvedEventHandler.kt`
  - Update player stats
  - Check if more riddles available
- [ ] **A5.3**: Create GameCompletedEventHandler in `application/events/GameCompletedEventHandler.kt`
  - Finalize player stats
  - Archive game data
- [ ] **A5.4**: Create RiddleQueueLowEventHandler in `application/events/RiddleQueueLowEventHandler.kt`
  - Trigger async riddle generation
  - Publish generation started notification
- [ ] **A5.5**: Create AttemptLimitExceededEventHandler in `application/events/AttemptLimitExceededEventHandler.kt`
  - Show answer to player
  - Move to next riddle
  - No points awarded

### 6. Application Services
- [ ] **A6.1**: Create GameApplicationService in `application/services/GameApplicationService.kt`
  - Orchestrate use cases
  - Transaction management
- [ ] **A6.2**: Create RiddleApplicationService in `application/services/RiddleApplicationService.kt`
  - Riddle retrieval and management
  - Queue status monitoring
- [ ] **A6.3**: Create AsyncRiddleGenerationOrchestrator in `application/services/AsyncRiddleGenerationOrchestrator.kt`
  - Coordinate Ollama (prompt generation) + Stable Diffusion (image generation)
  - Batch processing logic
  - Error handling and retry logic
  - Progress tracking

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
  - POST /api/games - Start new game (check player has no active game)
  - GET /api/games/{id} - Get game status
  - POST /api/games/{id}/guess - Submit guess
  - POST /api/games/{id}/give-up - Give up current riddle
  - POST /api/games/{id}/complete - Complete game
  - GET /api/players/{playerId}/active-game - Get player's active game
- [ ] **AD1.2**: Create RiddleController in `adapters/in/web/RiddleController.kt`
  - GET /api/riddles/{id} - Get riddle details
  - GET /api/riddles/random - Get random riddle
  - GET /api/riddles/queue-status - Get riddle queue status
- [ ] **AD1.3**: Create PlayerController in `adapters/in/web/PlayerController.kt`
  - POST /api/players - Register new player
  - GET /api/players/{id} - Get player profile
  - GET /api/players/{id}/stats - Get player statistics

### 2. Request/Response Models
- [ ] **AD2.1**: Create StartGameRequest in `adapters/in/web/requests/StartGameRequest.kt`
  - playerId field
- [ ] **AD2.2**: Create SubmitGuessRequest in `adapters/in/web/requests/SubmitGuessRequest.kt`
  - guess words array
- [ ] **AD2.3**: Create GameResponse in `adapters/in/web/responses/GameResponse.kt`
  - Include attempt info, max attempts, can submit
- [ ] **AD2.4**: Create GuessResultResponse in `adapters/in/web/responses/GuessResultResponse.kt`
  - Word feedback, attempt count, limit reached
- [ ] **AD2.5**: Create RiddleQueueStatusResponse in `adapters/in/web/responses/RiddleQueueStatusResponse.kt`
  - Available count, status, generation in progress
- [ ] **AD2.6**: Create GiveUpResponse in `adapters/in/web/responses/GiveUpResponse.kt`
  - Revealed answer, next riddle info

### 3. Repository Implementations (Outbound Adapters)
- [ ] **AD3.1**: Create JpaGameRepository in `adapters/out/persistence/JpaGameRepository.kt`
  - Implement GameRepository interface
  - Map domain to JPA entities
- [ ] **AD3.2**: Create JpaRiddleRepository in `adapters/out/persistence/JpaRiddleRepository.kt`
  - Implement RiddleRepository interface
  - Map domain to JPA entities
  - findAvailableCount() implementation
  - findBatch(size) implementation
- [ ] **AD3.3**: Create JpaPlayerRepository in `adapters/out/persistence/JpaPlayerRepository.kt`
  - Implement PlayerRepository interface
  - Map domain to JPA entities
  - findActiveGame(playerId) implementation

### 4. JPA Entity Models
- [ ] **AD4.1**: Create GameEntity in `adapters/out/persistence/entities/GameEntity.kt`
  - Include attemptHistory, maxAttempts fields
- [ ] **AD4.2**: Create RiddleEntity in `adapters/out/persistence/entities/RiddleEntity.kt`
  - Include status field (AVAILABLE, IN_PROGRESS, SOLVED, SKIPPED)
- [ ] **AD4.3**: Create GuessEntity in `adapters/out/persistence/entities/GuessEntity.kt`
  - Store individual guess attempts
- [ ] **AD4.4**: Create PlayerEntity in `adapters/out/persistence/entities/PlayerEntity.kt`
  - Player profile and stats
  - activeGameId reference

### 5. Domain-to-Entity Mappers
- [ ] **AD5.1**: Create GameMapper in `adapters/out/persistence/mappers/GameMapper.kt`
  - Map attempt history correctly
- [ ] **AD5.2**: Create RiddleMapper in `adapters/out/persistence/mappers/RiddleMapper.kt`
  - Map riddle status
- [ ] **AD5.3**: Create PlayerMapper in `adapters/out/persistence/mappers/PlayerMapper.kt`
  - Map player data and active game reference

### 6. Database Migrations (Flyway)
- [ ] **AD6.1**: Create V1__create_games_table.sql
  - Include attempt tracking columns
- [ ] **AD6.2**: Create V2__create_riddles_table.sql
  - Include status column
- [ ] **AD6.3**: Create V3__create_guesses_table.sql
  - Link to games table
- [ ] **AD6.4**: Create V4__create_players_table.sql
  - Include active_game_id column
- [ ] **AD6.5**: Create V5__add_indexes.sql
  - Index on riddle status for queue queries
  - Index on player active_game_id

### 7. External API Integration - AI Services (Outbound Adapters)
- [ ] **AD7.1**: Create OllamaPromptGeneratorAdapter in `adapters/out/ai/ollama/`
  - Generate creative prompts for images
  - REST client for local Ollama instance
  - Error handling and retry logic
  - Anti-corruption layer for Ollama API
- [ ] **AD7.2**: Create StableDiffusionImageGeneratorAdapter in `adapters/out/ai/stablediffusion/`
  - Generate images from prompts
  - REST client for local Stable Diffusion instance
  - Handle image storage/URL generation
  - Anti-corruption layer for Stable Diffusion API
- [ ] **AD7.3**: Create AsyncRiddleGenerationAdapter in `adapters/out/ai/`
  - Coordinate Ollama + Stable Diffusion
  - Batch processing with configurable size
  - Store generated riddles in database
  - Publish RiddleGenerated events

### 8. Exception Handling & Error Responses
- [ ] **AD8.1**: Create GlobalExceptionHandler with @ControllerAdvice
- [ ] **AD8.2**: Create ErrorResponse model
- [ ] **AD8.3**: Map domain exceptions to HTTP status codes
  - NoRiddlesAvailableException → 503 Service Unavailable
  - PlayerAlreadyHasActiveGameException → 409 Conflict
  - ExceededAttemptsException → 422 Unprocessable Entity

### 9. API Documentation
- [ ] **AD9.1**: Configure OpenAPI/Swagger
- [ ] **AD9.2**: Add API documentation annotations
- [ ] **AD9.3**: Create API usage examples

### 10. WebSocket Support (Inbound Adapter)
- [ ] **AD10.1**: Create WebSocketConfig in `adapters/in/websocket/WebSocketConfig.kt`
  - Configure STOMP messaging
- [ ] **AD10.2**: Create RiddleQueueStatusWebSocketController in `adapters/in/websocket/`
  - Push riddle availability updates to clients
  - Notify when riddles are being generated
  - Notify when new riddles available
- [ ] **AD10.3**: Create WebSocket message models
  - RiddleQueueUpdateMessage
  - GenerationProgressMessage

### 11. Dictionary Service Integration (Outbound Adapter)
- [ ] **AD11.1**: Create DictionaryServiceAdapter in `adapters/out/dictionary/`
  - Implement word similarity matching
  - Integrate with dictionary API or local dictionary
  - Support stemming (Porter stemmer library)
  - Support synonym lookup (WordNet or similar)
  - Support fuzzy matching (Levenshtein distance)

### 12. Security Configuration
- [ ] **AD12.1**: Configure Spring Security (future)
- [ ] **AD12.2**: Add authentication endpoints (future)
- [ ] **AD12.3**: Implement JWT token handling (future)
- [ ] **AD12.4**: Add authorization rules (future)

### 13. React Frontend Integration (Inbound Adapter)
- [ ] **AD13.1**: Create React app structure in `src/main/react/`
  - Component structure following atomic design
  - State management setup (Redux/Context)
- [ ] **AD13.2**: Create game play components
  - RiddleDisplay component (shows image)
  - GuessInput component (word input)
  - FeedbackDisplay component (shows word feedback colors)
  - AttemptCounter component (shows attempts left)
- [ ] **AD13.3**: Create riddle queue status components
  - QueueStatusIndicator (shows available/low/empty)
  - GenerationProgressIndicator (shows generation in progress)
- [ ] **AD13.4**: Configure Gradle to build React app to build directory
  - Add Gradle task to run npm build
  - Copy build output to static resources
  - Integrate with Spring Boot serving

### 14. Integration Testing (Adapters)
- [ ] **AD14.1**: Create REST API integration tests with TestContainers
  - Test game flow with attempt limits
  - Test give up functionality
  - Test riddle queue status
- [ ] **AD14.2**: Create repository integration tests
  - Test riddle queue queries
  - Test player active game queries
- [ ] **AD14.3**: Create end-to-end ATDD scenarios with Cucumber
  - Scenario: Player completes riddle within attempt limit
  - Scenario: Player exceeds attempt limit (show answer, move to next)
  - Scenario: Player gives up (show answer, no points)
  - Scenario: No riddles available (show "come back later")
  - Scenario: Continuous play (unlimited riddles)
- [ ] **AD14.4**: Create async riddle generation tests
  - Mock Ollama and Stable Diffusion
  - Test batch generation
  - Test queue replenishment
- [ ] **AD14.5**: Create WebSocket integration tests
  - Test real-time queue status updates

---

## Cross-Cutting Concerns

### 1. Configuration
- [ ] **CC1.1**: Create application.yml configuration
  - Riddle generation batch size (default: 10)
  - Riddle queue low threshold
  - Max attempts calculation formula parameters
  - Ollama API URL (local)
  - Stable Diffusion API URL (local)
- [ ] **CC1.2**: Create profiles (dev, test, prod)
  - Different batch sizes per environment
  - Different queue thresholds
- [ ] **CC1.3**: Configure database connections
  - PostgreSQL for production
  - H2 for testing
- [ ] **CC1.4**: Configure external service URLs
  - Ollama local instance
  - Stable Diffusion local instance
  - Dictionary service (if external)

### 2. Logging & Monitoring
- [ ] **CC2.1**: Configure structured logging
  - Log riddle generation progress
  - Log queue status changes
  - Log player game actions
- [ ] **CC2.2**: Add correlation IDs
  - Track requests across async operations
- [ ] **CC2.3**: Configure Spring Actuator endpoints
  - Custom health check for riddle queue status
  - Custom health check for Ollama availability
  - Custom health check for Stable Diffusion availability
- [ ] **CC2.4**: Add metrics and health checks
  - Riddle generation metrics (success rate, duration)
  - Queue depth metrics
  - Player activity metrics

### 3. Quality Assurance
- [✅] **CC3.1**: Configure Spotless for code formatting
- [✅] **CC3.2**: Configure Diktat for Kotlin style checking
- [✅] **CC3.3**: Configure Detekt for static code analysis
- [✅] **CC3.4**: Configure JaCoCo with 97% line coverage requirement
- [✅] **CC3.5**: Configure PITest for mutation testing (100% kill rate required)
- [✅] **CC3.6**: Configure Konsist for architecture testing
- [✅] **CC3.7**: Integrate all quality checks into `./gradlew check` command
- [✅] **CC3.8**: Create quality gate validation in CI/CD pipeline

### 4. Build & Deployment
- [ ] **CC4.1**: Create Docker configuration
  - Main application Dockerfile
  - Ollama service container
  - Stable Diffusion service container
- [ ] **CC4.2**: Create docker-compose for local development
  - PostgreSQL database
  - Ollama service
  - Stable Diffusion service
  - Application service
  - React frontend (development mode)
- [ ] **CC4.3**: Configure CI/CD pipeline
  - Run all quality gates
  - Build React frontend
  - Build Docker images
- [ ] **CC4.4**: Add deployment scripts
  - Database migration scripts
  - Service startup scripts

---

## Notes

- All tasks follow TDD with Red-Green-Refactor cycles
- ATDD scenarios required for all user-facing features
- Architecture tests validate DDD boundaries
- Minimum 97% code coverage required
- All quality gates must pass before merging
