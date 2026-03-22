# Stage 1: Build stage using Gradle image
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy source code
COPY gateway ./gateway
COPY auth ./auth
COPY auth-api ./auth-api
COPY auction-provider-api ./auction-provider-api
COPY auction-provider-procar ./auction-provider-procar

# Copy build configuration
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Build the application
RUN gradle gateway:bootJar --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/gateway/build/libs/procar-gateway-*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
