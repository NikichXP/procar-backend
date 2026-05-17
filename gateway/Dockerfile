# Stage 1: Build stage using Gradle image
FROM gradle:8.10-jdk21 AS builder

WORKDIR /app

# Copy gradle configuration first to cache dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gateway-api/build.gradle.kts ./gateway-api/
COPY auth-api/build.gradle.kts ./auth-api/
COPY auction-provider-api/build.gradle.kts ./auction-provider-api/
COPY user-api/build.gradle.kts ./user-api/
COPY commons/build.gradle.kts ./commons/
COPY gateway/build.gradle.kts ./gateway/

# Cache gradle dependencies
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle gateway:dependencies --no-daemon

# Copy source code
COPY gateway-api ./gateway-api
COPY auth-api ./auth-api
COPY auction-provider-api ./auction-provider-api
COPY user-api ./user-api
COPY commons ./commons
COPY gateway ./gateway

# Build the application
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle gateway:bootJar -x test --no-daemon -Dorg.gradle.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=512m"

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/gateway/build/libs/procar-gateway-*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
