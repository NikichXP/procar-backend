# Stage 1: Build stage using Gradle image
FROM gradle:8.10-jdk21 AS builder

WORKDIR /app

# Copy gradle configuration first to cache dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY user-api/build.gradle.kts ./user-api/
COPY auth-api/build.gradle.kts ./auth-api/
COPY commons/build.gradle.kts ./commons/
COPY user/build.gradle.kts ./user/

# Cache gradle dependencies
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle user:dependencies --no-daemon

# Copy source code
COPY user-api ./user-api
COPY auth-api ./auth-api
COPY commons ./commons
COPY user ./user

# Build the application
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle user:bootJar -x test --no-daemon -Dorg.gradle.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=512m"

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/user/build/libs/procar-user-*.jar app.jar

# Expose port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
