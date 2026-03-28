# Stage 1: Build stage using Gradle image
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy source code
COPY auction-provider-procar ./auction-provider-procar
COPY auction-provider-api ./auction-provider-api
COPY auth ./auth
COPY auth-api ./auth-api
COPY gateway ./gateway

# Copy build configuration
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Build the application
RUN gradle auction-provider-procar:bootJar -x test --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/auction-provider-procar/build/libs/procar-auction-provider-procar-*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
