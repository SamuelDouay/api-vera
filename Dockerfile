# Stage de build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests

# Stage final
FROM openjdk:21-ea-21-jdk-slim
WORKDIR /app

RUN apt-get update && apt-get upgrade -y && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/logs

# Copie depuis le stage de build
COPY --from=builder /workspace/target/*-jar-with-dependencies.jar app.jar

RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]