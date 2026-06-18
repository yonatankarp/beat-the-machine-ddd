FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY build/libs/adapters.jar app.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "app.jar"]
