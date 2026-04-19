# Stage 1: Copy libatomic from node:20 (has it, gradle:8.5-jdk21 does not)
FROM node:20 AS node

FROM gradle:8.5-jdk21 AS builder

COPY --from=node /usr/lib/x86_64-linux-gnu/libatomic.so.1 /usr/lib/x86_64-linux-gnu/libatomic.so.1
COPY --from=node /usr/lib/x86_64-linux-gnu/libatomic.so.1.2.0 /usr/lib/x86_64-linux-gnu/libatomic.so.1.2.0

WORKDIR /app

COPY admin-jetpack ./admin-jetpack
COPY admin-ui ./admin-ui
COPY build.gradle.kts .
COPY settings.gradle.kts .

ENV GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=1g"

RUN gradle admin-jetpack:wasmJsBrowserDistribution --no-daemon -Dkotlin.daemon.jvmargs="-Xmx4g -XX:MaxMetaspaceSize=1g"

# Stage 2: Runtime stage - serve static files with nginx
FROM nginx:alpine

COPY --from=builder /app/admin-jetpack/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/

EXPOSE 80
