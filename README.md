# Receiving Service

A microservice designed to handle the goods receiving process in a warehouse management system (WMS). 

## Table of Contents

- [Core Business Process](#core-business-process)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running the Application](#running-the-application)
- [API Overview](#api-overview)
- [Integrations](#integrations)
  - [Outgoing Events (Kafka)](#outgoing-events-kafka)
- [Testing Strategy](#testing-strategy)

## Core Business Process

The service automates the "happy path" of a typical warehouse receiving process:

1.  **ASN Registration**: An **Advanced Shipping Notice (ASN)**, which details an expected inbound delivery, is registered in the system.
2.  **Receiving Start**: When the truck arrives, a manager finds the corresponding ASN and initiates a **Goods Receipt**, opening it for workers.
3.  **Worker Session**: Warehouse workers can join the active Goods Receipt.
4.  **Scanning**: Workers scan the **LPN (License Plate Number)** of each handling unit (e.g., pallet, box) and the **SKU (Stock Keeping Unit)** of the contents inside. The service validates each scan against the original ASN data.
5.  **Receiving Close**: Manager closes the Goods Receipt. The service then triggers an event to report any discrepancies between the expected and actually received goods.

## Key Features

-   **Role-Based Access Control**: Differentiates between `MANAGER` and `WORKER` roles with distinct permissions.
-   **Hierarchical Scanning**: Supports nested handling units (e.g., boxes within a pallet).
-   **Stateful Worker Sessions**: Worker progress is persisted, allowing them to safely disconnect and reconnect without losing their current scanning context.
-   **Asynchronous Integration**: Communicates with other microservices (e.g., Inventory, ERP) via Kafka.

## Architecture


-   **Rich Domain Model**, where entities (`InboundDelivery`, `GoodsReceipt`, `WorkerReceivingSession`) encapsulate business rules and invariants. 

-   **Hexagonal Architecture (Ports & Adapters)**: The application core (domain and application layers) is decoupled from infrastructure concerns. For example, the service uses a `DiscrepanciesReportPort` interface to send reports, with a concrete `KafkaAdapter` in the infrastructure layer providing the implementation. This makes the core independent of external technologies like Kafka.

-   **Event-Driven Communication**: The service publishes domain events to a Kafka topic upon completion of key business processes (e.g., `goods.received.v1`). This decouples the Receiving service from downstream consumers.

-   **Layered Structure**: The code is organized into four distinct layers:
    -   `api`: Controllers, DTOs, and other web-related components.
    -   `application`: Service classes that orchestrate business workflows.
    -   `domain`: Aggregates, Entities, and business rules.
    -   `infrastructure`: Repositories, Kafka producers, and other external service integrations.

## Technology Stack

| Component              | Technology               |
| ---------------------- |--------------------------|
| **Language**           | Java 25 (leveraging features like Virtual Threads) |
| **Framework**          | Spring Boot 4            |
| **Data Persistence**   | Spring Data JPA / Hibernate, PostgreSQL |
| **Messaging**          | Spring for Apache Kafka  |
| **API**                | Spring Web (REST)        |
| **Security**           | Spring Security (JWT for stateless authentication) |
| **Build Tool**         | Maven                    |
| **Testing**            | JUnit, Testcontainers, Mockito |
| **Utilities**          | Lombok, JJWT             |

## Getting Started

### Prerequisites

-   JDK 25 or later
-   Apache Maven
-   Docker

### Running the Application

1.  **Start Infrastructure Services:**
    Launch the required backing services (PostgreSQL, Kafka, Zookeeper) using Docker Compose.

    ```sh
    docker compose up
    ```

2.  **Run the Spring Boot Application:**
    Use the Maven Spring Boot plugin to build and run the service. The application will automatically connect to the services running in Docker.

    ```sh
    mvn spring-boot:run
    ```

The API will be available at `http://localhost:8080`.

## API Overview

The service provides a RESTful API for managing the receiving process. Key endpoints include:

For detailed information, please refer to the OpenAPI/Swagger documentation, which can be enabled in the application. `http://localhost:8080/swagger-ui/index.html`

## Integrations

### Outgoing Events (Kafka)

The service publishes events to notify other parts of the WMS about important state changes.

| Topic Name             | Event                               | Description                                                                                             |
| ---------------------- | ----------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `goods.received.v1`    | `DiscrepanciesReport`               | Published when a manager closes a goods receipt. Contains a full report of expected vs. actual quantities. |
| `pallet.completed.v1`  | `PalletCompletedEvent` (Example)    | Can be published when a worker finishes receiving a full pallet, triggering a put-away task.            |
