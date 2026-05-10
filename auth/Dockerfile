# Stage 1: Build stage using Gradle image
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy source code (only dependencies needed for auth)
COPY auth ./auth
COPY auth-api ./auth-api
COPY commons ./commons

# Copy build configuration
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Build the application
RUN gradle auth:bootJar -x test --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/auth/build/libs/procar-auth-*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
