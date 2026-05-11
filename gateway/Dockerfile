# Stage 1: Build stage using Gradle image
FROM gradle:8.10-jdk21 AS builder

WORKDIR /app

# Copy source code (only dependencies needed for gateway)
COPY gateway ./gateway
COPY gateway-api ./gateway-api
COPY auth-api ./auth-api
COPY auction-provider-api ./auction-provider-api
COPY user-api ./user-api
COPY commons ./commons

# Copy build configuration
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# Build the application
RUN gradle gateway:bootJar -x test --no-daemon -Dorg.gradle.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=512m"

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/gateway/build/libs/procar-gateway-*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
