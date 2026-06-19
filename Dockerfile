# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY . .
RUN ./gradlew --no-daemon :beat-the-machine-adapters:bootJar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /src/beat-the-machine-adapters/build/libs/adapters.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENV BTM_DB_PATH=/data/beat-the-machine.db
VOLUME ["/data"]
ENTRYPOINT ["java", "-jar", "app.jar"]
