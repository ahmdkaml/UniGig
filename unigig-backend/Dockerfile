# ==============================================================================
# Stage 1: Build the Spring Boot Executable JAR
# ==============================================================================
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /workspace

# Argument to optionally build with the 'slim' profile (e.g., --build-arg MAVEN_ARGS="-Pslim")
ARG MAVEN_ARGS=""

# Copy Maven wrapper and POM first to leverage Docker layer caching for dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline --batch-mode --no-transfer-progress

# Copy application source and package the executable JAR
COPY src/ src/
RUN ./mvnw clean package -DskipTests ${MAVEN_ARGS} --batch-mode --no-transfer-progress \
    && cp target/*.jar app.jar

# ==============================================================================
# Stage 2: Minimal Alpine JRE Runtime
# ==============================================================================
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

# Run as a non-root user and prepare writable directory for optional embedded H2 storage
RUN addgroup -S unigig && adduser -S unigig -G unigig \
    && mkdir -p /app/data && chown -R unigig:unigig /app

COPY --from=builder --chown=unigig:unigig /workspace/app.jar /app/app.jar

USER unigig

# Runtime environment defaults (override at container start or via docker-compose / hosting env)
ENV PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=70.0 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
