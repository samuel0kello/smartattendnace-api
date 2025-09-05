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

## 2. Proposed Refinements

While the current structure is functional, refinements can make the codebase more maintainable, testable, and aligned with common DDD (Domain-Driven Design) and Clean Architecture practices.

### Refinement Goals

* **Better separation of concerns**: Distinguish *domain*, *infrastructure*, and *application/web* clearly.
* **Feature modularization**: Group by business capability (auth, courses, attendance, etc.).
* **Clear layering**: Domain logic isolated from infrastructure (e.g., Exposed, email clients, DI frameworks).

### Proposed Structure

```
src/main/kotlin/com/example
│
├── application
│   ├── web
│   │   ├── routes (API endpoints)
│   │   └── Application.kt / Module.kt
│   └── config (app configuration, DI wiring)
│
├── domain
│   ├── auth
│   ├── courses
│   ├── attendance
│   ├── model (entities, value objects)
│   └── services (pure business logic)
│
├── infrastructure
│   ├── database
│   │   ├── entity (Exposed tables)
│   │   └── DatabaseProvider.kt
│   ├── email
│   ├── security
│   └── plugins
│
└── shared
    ├── util
    └── common DTOs / helpers
```

---

### Benefits of Proposed Refinement

* **Domain-driven clarity**: Domain logic is framework-agnostic, easier to test and reason about.
* **Infrastructure isolation**: Exposed, email sending, and plugins are adapters that can be swapped out.
* **API under `web`**: Routes become part of the application layer, keeping entry points consistent.
* **Scalability**: Prepares the project for possible future microservice extraction.

---

### Future Enhancements

* Introduce **CQRS/Use Cases** (application services) for more complex workflows.
* Add **audit logging middleware** in the `web` layer to record every user/admin action transparently.
* Support **multi-tenant design** if the system grows beyond a single institution.

---
