# Beat the Machine — thin wrappers around Gradle and Docker.
# Gradle remains the source of truth; this file is for discoverability and shorthand.

GRADLE       := ./gradlew
ADAPTERS     := :beat-the-machine-adapters
IMAGE        := beat-the-machine
DOCKERFILE   := beat-the-machine-adapters/Dockerfile
JAR          := beat-the-machine-adapters/build/libs/adapters.jar

.DEFAULT_GOAL := help

.PHONY: help build test coverage check run run-inmemory jar clean docker-build docker-run

help: ## Show this help
	@grep -hE '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) \
		| awk 'BEGIN{FS=":.*?## "}{printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

build: ## Compile and assemble all modules (no tests)
	$(GRADLE) build -x test

test: ## Run the full test suite
	$(GRADLE) test

coverage: ## Run tests and generate JaCoCo coverage reports
	$(GRADLE) test jacocoTestReport

check: ## Run the full verification (build + tests)
	$(GRADLE) build

run: ## Run the app (default persistence)
	$(GRADLE) $(ADAPTERS):bootRun

run-inmemory: ## Run the app with in-memory persistence
	$(GRADLE) $(ADAPTERS):bootRun --args='--btm.persistence=inmemory'

jar: ## Build the executable bootJar (adapters.jar)
	$(GRADLE) $(ADAPTERS):bootJar

clean: ## Remove build outputs
	$(GRADLE) clean

docker-build: jar ## Build the Docker image (builds the jar first)
	docker build -f $(DOCKERFILE) -t $(IMAGE) .

docker-run: ## Run the Docker image on port 8080
	docker run --rm -p 8080:8080 $(IMAGE)