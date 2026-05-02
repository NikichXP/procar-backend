# Stage 1: Copy libatomic from node:20 (has it, gradle:8.5-jdk21 does not)
FROM node:20 AS node

FROM gradle:8.10-jdk21 AS builder

COPY --from=node /usr/lib/x86_64-linux-gnu/libatomic.so.1 /usr/lib/x86_64-linux-gnu/libatomic.so.1
COPY --from=node /usr/lib/x86_64-linux-gnu/libatomic.so.1.2.0 /usr/lib/x86_64-linux-gnu/libatomic.so.1.2.0

WORKDIR /app

# Copy gradle wrapper and config
COPY gradlew .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
COPY gradle ./gradle
COPY gradle.properties .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy admin-jetpack source
COPY admin-jetpack ./admin-jetpack

# Minimal settings.gradle.kts for admin-jetpack only to avoid missing module errors
RUN echo 'pluginManagement {' > settings.gradle.kts && \
    echo '    repositories {' >> settings.gradle.kts && \
    echo '        google()' >> settings.gradle.kts && \
    echo '        mavenCentral()' >> settings.gradle.kts && \
    echo '        gradlePluginPortal()' >> settings.gradle.kts && \
    echo '    }' >> settings.gradle.kts && \
    echo '}' >> settings.gradle.kts && \
    echo 'rootProject.name = "procar-backend"' >> settings.gradle.kts && \
    echo 'include("admin-jetpack")' >> settings.gradle.kts

ENV GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=1g"

RUN ./gradlew :admin-jetpack:wasmJsBrowserDistribution --no-daemon -Dkotlin.daemon.jvmargs="-Xmx4g -XX:MaxMetaspaceSize=1g" && \
    rm -rf /app/admin-jetpack/build/compileSync \
           /app/admin-jetpack/build/kotlin \
           /app/admin-jetpack/build/tmp \
           /app/.gradle/caches

# Stage 2: Runtime stage - serve static files with nginx
FROM nginx:alpine

COPY --from=builder /app/admin-jetpack/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/

EXPOSE 80
