# Stage 1: Copy libatomic from node:20 (has it, gradle:8.5-jdk21 does not)
FROM node:20 AS node

FROM gradle:8.10-jdk21 AS builder

COPY --from=node /usr/lib/x86_64-linux-gnu/libatomic.so.1 /usr/lib/x86_64-linux-gnu/libatomic.so.1
COPY --from=node /usr/lib/x86_64-linux-gnu/libatomic.so.1.2.0 /usr/lib/x86_64-linux-gnu/libatomic.so.1.2.0

WORKDIR /app

# Copy gradle configuration first to cache dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gateway-api/build.gradle.kts ./gateway-api/
COPY admin-jetpack/build.gradle.kts ./admin-jetpack/

# Cache gradle dependencies
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle :admin-jetpack:dependencies --no-daemon

# Copy source code
COPY gateway-api ./gateway-api
COPY admin-jetpack ./admin-jetpack

ENV GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"

RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle :admin-jetpack:wasmJsBrowserDistribution --no-daemon -Dkotlin.daemon.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=512m"

RUN rm -rf /app/admin-jetpack/build/compileSync \
           /app/admin-jetpack/build/kotlin \
           /app/admin-jetpack/build/tmp \
           /app/.gradle/caches

# Stage 2: Runtime stage - serve static files with nginx
FROM nginx:alpine

COPY --from=builder /app/admin-jetpack/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/

EXPOSE 80
