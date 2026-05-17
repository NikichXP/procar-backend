# Stage 1: Build stage using Gradle image
FROM gradle:8.10-jdk21 AS builder

WORKDIR /app

# Copy gradle configuration first to cache dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY auth-api/build.gradle.kts ./auth-api/
COPY commons/build.gradle.kts ./commons/
COPY auth/build.gradle.kts ./auth/

# Cache gradle dependencies
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle auth:dependencies --no-daemon

# Copy source code
COPY auth-api ./auth-api
COPY commons ./commons
COPY auth ./auth

# Build the application
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle auth:bootJar -x test --no-daemon -Dorg.gradle.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=512m"

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/auth/build/libs/procar-auth-*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
