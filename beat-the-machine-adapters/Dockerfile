FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY beat-the-machine-adapters/build/libs/adapters.jar app.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "app.jar"]
