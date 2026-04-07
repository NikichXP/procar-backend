# Stage 1: Build stage using Gradle image
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy source code
COPY user ./user
COPY user-api ./user-api
COPY auth-api ./auth-api
COPY auction-provider-api ./auction-provider-api

# Copy build configuration
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Build the application
RUN gradle user:bootJar -x test --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/user/build/libs/procar-user-*.jar app.jar

# Expose port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
