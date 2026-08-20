FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system spring \
    && useradd --system --gid spring spring

COPY --from=build /workspace/target/sitiopro-0.0.1-SNAPSHOT.jar app.jar

USER spring:spring

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD curl -fsS "http://localhost:${SERVER_PORT:-8083}/health" || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
