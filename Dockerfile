FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

ARG ELASTIC_APM_AGENT_VERSION=1.56.0

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /opt/elastic \
    && curl -fsSL -o /opt/elastic/elastic-apm-agent.jar \
        "https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/${ELASTIC_APM_AGENT_VERSION}/elastic-apm-agent-${ELASTIC_APM_AGENT_VERSION}.jar" \
    && chmod 0444 /opt/elastic/elastic-apm-agent.jar

RUN groupadd --system spring \
    && useradd --system --gid spring spring

RUN mkdir -p /var/log/sitiopro \
    && chown -R spring:spring /var/log/sitiopro

COPY --from=build /workspace/target/sitiopro-0.0.1-SNAPSHOT.jar app.jar

USER spring:spring

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD curl -fsS "http://localhost:${SERVER_PORT:-8083}/actuator/health/readiness" || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
