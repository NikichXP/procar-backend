# Procar Backend

This repository contains the backend microservices for the Procar platform.

## Prerequisites

- Docker and Docker Compose
- Java 21+ (if building/running locally outside of Docker)

## Getting Started

1. **Environment Setup:**
   Copy the provided `.env.example` to `.env` and fill in the required configuration values.
   ```bash
   cp .env.example .env
   ```

2. **MongoDB Configuration:**
   The application requires a MongoDB database to run. You can either:
   - Provide connection details for a **MongoDB Atlas** instance in your `.env` file.
   - Or, host your own local MongoDB database (e.g., start a local container using `docker run --name mongo -p 27017:27017 -d mongo`) and update the `.env` file to point to your local host.

3. **Running the Application:**
   You can start the entire backend infrastructure—including dependencies like Redis and RabbitMQ, as well as all backend microservices—using Docker Compose:
   ```bash
   docker compose up -d --build
   ```

   This will build the Docker images and start the following services:
   - `gateway` (Port 8080)
   - `auth` (Port 8082)
   - `auction-provider-procar` (Port 8083)
   - `user` (Port 8084)
   - `redis` (Port 6379)
   - `rabbitmq` (Ports 5672, 15672)

4. **Stopping the Application:**
   ```bash
   docker compose down
   ```

## Note on Jetpack Modules

For now, **completely ignore** the `admin-jetpack` and `customer-jetpack` modules located in this repository.
