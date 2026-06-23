# Beat the Machine — thin wrappers around Gradle and Docker.
# Gradle remains the source of truth; this file is for discoverability and shorthand.

GRADLE       := ./gradlew
ADAPTERS     := :beat-the-machine-adapters
IMAGE        := beat-the-machine
DOCKERFILE   := beat-the-machine-adapters/Dockerfile
JAR          := beat-the-machine-adapters/build/libs/adapters.jar
FRONTEND     := :beat-the-machine-frontend
UI_DIR       := beat-the-machine-frontend
PORT         := 8080
UI_PORT      := 5173
DB_PATH      := beat-the-machine.db
BTM_IMAGE_LOCAL_SD_BASE_URL ?= http://localhost:7860
BASE_URL     := http://localhost:$(PORT)
UI_URL       := http://localhost:$(UI_PORT)/app/

# Prints where to reach the app. The URL appears above Spring's startup logs,
# so scroll up if the boot output has buried it.
define banner
	@printf '\n\033[1;32m▶ Beat the Machine\033[0m starting on \033[36m%s\033[0m\n' '$(BASE_URL)'
	@printf '  API:    \033[36m%s/api/challenges\033[0m\n' '$(BASE_URL)'
	@printf '  Health: \033[36m%s/health\033[0m\n\n' '$(BASE_URL)'
endef

.DEFAULT_GOAL := help

.PHONY: help setup build test coverage check seed-images seed-challenges run run-inmemory ui ui-build jar clean docker-build docker-run

help: ## Show this help
	@grep -hE '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) \
		| awk 'BEGIN{FS=":.*?## "}{printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

setup: ## One-time: enable the local Spotless pre-commit hook
	@git config core.hooksPath .githooks
	@printf '\033[1;32m▶\033[0m git hooks enabled: \033[36mcore.hooksPath=.githooks\033[0m (pre-commit runs spotlessCheck)\n'

build: ## Compile and assemble all modules (no tests)
	$(GRADLE) build -x test

test: ## Run the full test suite
	$(GRADLE) test

coverage: ## Run tests and generate JaCoCo coverage reports
	$(GRADLE) test jacocoTestReport

check: ## Run the full verification (build + tests)
	$(GRADLE) build

seed-images: ## Generate the 30 bundled seed images using local Stable Diffusion
	BTM_IMAGE_LOCAL_SD_BASE_URL=$(BTM_IMAGE_LOCAL_SD_BASE_URL) scripts/generate-seed-images-local-sd.sh

seed-challenges: ## Start durable backend and seed 30 curated challenge templates
	$(banner)
	@printf '  Seeds:  \033[36m30 curated templates\033[0m (10 per difficulty) in SQLite\n'
	@printf '  DB:     \033[36m%s\033[0m\n\n' '$(DB_PATH)'
	PORT=$(PORT) BTM_DB_PATH=$(DB_PATH) BTM_PROMPT_PROVIDER=seed BTM_IMAGE_PROVIDER=seed BTM_POOL_TARGET=10 $(GRADLE) $(ADAPTERS):bootRun

run: ## Run the app (default persistence)
	$(banner)
	PORT=$(PORT) $(GRADLE) $(ADAPTERS):bootRun

run-inmemory: ## Run the app with in-memory persistence
	$(banner)
	PORT=$(PORT) $(GRADLE) $(ADAPTERS):bootRun --args='--btm.persistence=inmemory'

ui: ## Run the SPA dev server (Vite, hot reload); needs the backend running (make run-inmemory)
	@printf '\n\033[1;32m▶ UI dev server\033[0m on \033[36m%s\033[0m\n' '$(UI_URL)'
	@printf '  proxies /api to the backend on \033[36m%s\033[0m — start it with `make run-inmemory`\n\n' '$(BASE_URL)'
	cd $(UI_DIR) && npm install && npm run dev

ui-build: ## Build the production SPA bundle (also part of `make build`)
	$(GRADLE) $(FRONTEND):buildWebApp

jar: ## Build the executable bootJar (adapters.jar)
	$(GRADLE) $(ADAPTERS):bootJar

clean: ## Remove build outputs
	$(GRADLE) clean

docker-build: jar ## Build the Docker image (builds the jar first)
	docker build -f $(DOCKERFILE) -t $(IMAGE) .

docker-run: ## Run the Docker image on port 8080
	$(banner)
	docker run --rm -p $(PORT):8080 $(IMAGE)
