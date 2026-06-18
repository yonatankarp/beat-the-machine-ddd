FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY beat-the-machine-adapters/build/libs/adapters.jar app.jar
USER app
# Run on an unprivileged port so the container does not need root to bind.
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
