# ============================================================
#  JUNCTION Backend — Multi-Stage Production Dockerfile
#  Java 21 | Spring Boot 4.1.2 | Eclipse Temurin Minimal Runtime
# ============================================================

# Stage 1: Build & Package
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Install maven
RUN apk add --no-cache maven

# Copy POM and download dependencies for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Distroless / Hardened Alpine Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S junction && adduser -S junction -G junction

# Copy extracted application jar from builder
COPY --from=builder /workspace/target/junction-backend-*.jar app.jar

# Run as non-root user
USER junction:junction

# Default port
ENV PORT=8080
EXPOSE 8080

# Configure JVM flags for containers & Java 21 Virtual Threads
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

