# Stage 1: Build stage using Gradle image
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY admin-jetpack ./admin-jetpack
COPY admin-ui ./admin-ui
COPY build.gradle.kts .
COPY settings.gradle.kts .

ARG GATEWAY_BASE_URL=http://localhost:8080
ENV GATEWAY_BASE_URL=$GATEWAY_BASE_URL

RUN gradle admin-jetpack:wasmJsBrowserDistribution --no-daemon

# Stage 2: Runtime stage - serve static files with nginx
FROM nginx:alpine

COPY --from=builder /app/admin-jetpack/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/

EXPOSE 80
