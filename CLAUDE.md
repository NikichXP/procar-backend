# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

This project uses Gradle wrapper — always use `./gradlew`, never a system-installed `gradle`.

```bash
# Build all modules
./gradlew build

# Run a specific service (run from root, specify module)
./gradlew :gateway:bootRun
./gradlew :auth:bootRun
./gradlew :auction-provider-procar:bootRun

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :auction-provider-procar:test

# Run a single test class
./gradlew :auction-provider-procar:test --tests "com.procar.auction.SomeTest"

# List available tasks
./gradlew tasks
```

## Architecture Overview

This is a multi-module Kotlin/Spring Boot microservices backend for an automobile auction platform.

### Module Dependency Graph

```
gateway  ──depends on──>  auction-provider-api
                          auth-api

auction-provider-procar  ──implements──>  auction-provider-api

auth  ──implements──>  auth-api
```

**API contract modules** (`auth-api`, `auction-provider-api`) define Spring WebFlux controller interfaces and shared
DTOs. Services implement these interfaces; the gateway uses them as HTTP client proxies via `HttpServiceProxyFactory`.

### Services and Ports

| Module                    | Port | Purpose                                        |
|---------------------------|------|------------------------------------------------|
| `gateway`                 | 8080 | Public-facing API gateway, routes to providers |
| `auth`                    | 8082 | Authentication, token management               |
| `auction-provider-procar` | 8083 | In-house auction provider implementation       |

### Key Architectural Patterns

**API-First with Spring HTTP Interfaces:** Controller interfaces in `*-api` modules are shared between implementation
services and the gateway. The gateway wires them via `ConnectorConfig.kt` using `HttpServiceProxyFactory` — the same
interface is both a `@RestController` implementation and an HTTP client proxy.

**Reactive Stack:** All services use Spring WebFlux + Project Reactor. Use Kotlin coroutines with `suspend` functions
and `Flow` where appropriate; reactor extensions (`asFlow()`, `awaitFirst()`, etc.) bridge between Reactor and
coroutines.

**Auth Token Pattern:** Auth service has pluggable `AccessTokenService` — either `InMemoryAccessTokenService` or
`RedisAccessTokenService`. Tokens are managed separately from refresh tokens (MongoDB-persisted).

### Domain Model (auction-provider-procar)

The `LotDocument` is the central entity — it models a vehicle auction lot with nested objects for vehicle specs, auction
metadata, location, and seller info. The `BidDocument` tracks bids per lot. Converters (`converter/` package) transform
between MongoDB documents and API response models.

### Testing

`auction-provider-procar` has Cucumber BDD tests with feature files in `src/test/resources/features/` and uses Embedded
MongoDB for integration tests. Standard unit tests use JUnit 5 + Mockito-Kotlin.

## Tech Stack

- **Kotlin 2.2.21** / **Java 21** / **Spring Boot 3.3.0**
- **Spring WebFlux** (reactive, non-blocking)
- **MongoDB** (reactive) — lots, bids, auth credentials, refresh tokens
- **Redis** (reactive) — access token caching
- **Gradle 9.3.1** multi-module build
- **Cucumber 7.15.0** — BDD integration tests
- **SpringDoc OpenAPI 2.5.0** — Swagger UI available on each service
