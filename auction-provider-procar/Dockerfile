# Stage 1: Build stage using Gradle image
FROM gradle:8.10-jdk21 AS builder

WORKDIR /app

# Copy gradle configuration first to cache dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY auction-provider-api/build.gradle.kts ./auction-provider-api/
COPY commons/build.gradle.kts ./commons/
COPY auction-provider-procar/build.gradle.kts ./auction-provider-procar/

# Cache gradle dependencies
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle auction-provider-procar:dependencies --no-daemon

# Copy source code
COPY auction-provider-api ./auction-provider-api
COPY commons ./commons
COPY auction-provider-procar ./auction-provider-procar

# Build the application
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle auction-provider-procar:bootJar -x test --no-daemon -Dorg.gradle.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=512m"

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/auction-provider-procar/build/libs/procar-auction-provider-procar-*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
