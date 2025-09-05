# System Architecture – SmartAttendance

This document describes the current architecture of the **SmartAttendance** project and proposes refinements for future maintainability and scalability.

---

## 1. Current Architecture (As-Is)

The system is implemented as a **modularized monolith**, structured around feature-based packages.

### High-Level Layers
- **API Layer (`api`)**  
  Contains HTTP route handlers and request/response DTOs. Serves as the entry point for incoming client requests.

- **Configuration (`config`)**  
  Provides configuration utilities, including:
  - `database/DatabaseProvider.kt` for initializing the Exposed ORM and database connection.
  - `entity/` for Exposed table/entity definitions.

- **Dependency Injection (`di`)**  
  Houses DI modules, wiring up services, repositories, and other application components.

- **Domain Models (`model`)**  
  Contains domain-level models and data classes used across services and APIs.

- **Plugins (`plugins`)**  
  Encapsulates integrations and additional application extensions (e.g., logging, monitoring, authentication providers).

- **Services (`services`)**  
  Business logic layer. Organized by features:
  - `auth` – authentication and user management
  - `courses` – course management
  - `email` – email-related services
  - (extendable for other domains)

- **Utilities (`util`)**  
  Shared helper functions and reusable utilities.

- **Web (`web`)**  
  Application bootstrap and Ktor configuration:
  - `Application.kt` – main application entry point.
  - `Module.kt` – sets up routes, middleware, and DI modules.

---

### Request Flow

```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Service
    participant Database

    Client->>API: HTTP Request
    API->>Service: Invoke business logic
    Service->>Database: Query/Command via Exposed
    Database-->>Service: Data
    Service-->>API: Domain/DTO
    API-->>Client: HTTP Response
````

---


