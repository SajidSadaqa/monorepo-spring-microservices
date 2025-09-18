# Monorepo Spring Microservices

This repository demonstrates a production-grade microservices architecture built with Spring Boot, designed for clarity, modularity, and scalability. It follows strict layered architecture and clean coding principles to model a real-world enterprise system with modern observability, caching, and file management capabilities.

---

## Overview

The system is composed of three core modules:
- **user-service**: Responsible for user management, authentication, token issuance, and file operations with Redis caching.
- **admin-service**: Provides administrative operations and proxies requests to user-service using service-to-service (S2S) authentication.
- **key-generator**: Utility module for generating cryptographic keys and certificates for secure service communication.

Both services are packaged as independent Spring Boot applications, secured with TLS, and communicate via HTTPS and signed JWT tokens.

---

## Architecture

### Key Characteristics
- **Domain-Driven Design (DDD) Inspired**: Code is organized into `application`, `domain`, `infrastructure` and `interfaces` layers.
- **Service-to-Service Security**: Admin-service authenticates itself to user-service using minted JWT tokens signed by a trusted keypair.
- **TLS Everywhere**: Each service runs with its own keystore and truststore. Mutual TLS is supported in development and production.
- **Stateless**: Sessions are disabled. Authentication is purely token-based.
- **Localization**: Built-in i18n with message bundles in English and Arabic.
- **Event-Driven Architecture**: Comprehensive observability with custom events for file operations and system monitoring.
- **Distributed Caching**: Redis integration for performance optimization and session management.
- **File Management**: MinIO integration for scalable object storage and file manipulation.

---

## Technologies Used

| Technology | Purpose |
|------------|---------|
| **Spring Boot 3.3.3** | Core application framework |
| **Spring Web (MVC)** | RESTful API endpoints |
| **Spring Security** | Authentication and authorization with JWT |
| **Spring Data JPA + Hibernate** | Persistence layer |
| **Spring Cloud 2023.0.3** | Microservices patterns and configuration |
| **PostgreSQL** | Primary database engine |
| **Redis** | Distributed caching and session storage |
| **MinIO** | Object storage for file management |
| **Flyway** | Database migrations |
| **Feign (OpenFeign)** | Inter-service HTTP client |
| **MapStruct 1.5.5** | Compile-time DTO to entity mapping |
| **Lombok** | Boilerplate reduction |
| **Jakarta Validation** | Input validation with localized error messages |
| **SpringDoc / Swagger 2.6.0** | OpenAPI documentation and interactive UI |
| **Micrometer Observation** | Observability and metrics collection |
| **Spring AOP** | Aspect-oriented programming for cross-cutting concerns |
| **Logback + SLF4J** | Comprehensive logging with event tracking |
| **Testcontainers** | Integration testing with real services |
| **Docker** | Containerization and deployment |

---

## Modules and Directories

### user-service
- **application/**
  Contains use-case logic, DTOs, service implementations, and event handling.
- **domain/**
  JPA entities, aggregates, and domain events representing core business data.
- **infrastructure/**
  Database repositories, Redis configuration, MinIO clients, and external integrations.
- **web/**
  REST controllers exposing the API to clients with comprehensive file upload/download endpoints.
- **security/**
  JWT filters, S2S authorization filters, and Spring Security configuration.
- **exceptions/**
  Global exception handler returning structured `ProblemDetail` responses.
- **observability/**
  Custom aspects and event publishers for system monitoring.

### admin-service
- **application/**
  S2S communication layer with Feign clients, DTOs, and proxy services.
- **security/**
  JWT minting for S2S calls and TLS configuration for outbound requests.
- **web/**
  REST controllers exposing administrative APIs that proxy to user-service.
- **exceptions/**
  Global exception handler aligned with user-service for consistent error models.

### key-generator
- **Utility module**
  Generates RSA keypairs, certificates, and manages cryptographic materials for secure service communication.

---

## New Features & Enhancements

### Observability & Monitoring
- **Event-Driven Observability**: Custom observation events for file operations, user actions, and system health monitoring.
- **Micrometer Integration**: Comprehensive metrics collection with custom observation handlers.
- **AOP-Based Logging**: Aspect-oriented programming for cross-cutting concerns like audit trails and performance monitoring.

### Caching & Performance
- **Redis Integration**: Distributed caching layer with connection pooling for improved performance.
- **Spring Cache Abstraction**: Annotation-driven caching for frequently accessed data.
- **Connection Optimization**: Apache Commons Pool2 for efficient Redis connection management.

### File Management System
- **MinIO Integration**: Scalable object storage solution for file uploads, downloads, and manipulation.
- **Event-Driven File Processing**: Asynchronous file manipulation with comprehensive event tracking.
- **Multi-File Operations**: Support for bulk file operations with transactional consistency.

### Enhanced Testing
- **Testcontainers**: Real database and Redis testing with containerized services.
- **REST Assured**: API testing framework for comprehensive endpoint validation.
- **WireMock**: Service virtualization for testing inter-service communication.

---

## Security Model

1. **TLS Setup**
   - Each service owns a **keystore** (`*-keystore.p12`) containing its private key and certificate.
   - Each service trusts the other's public certificate via a **truststore** (`*-truststore.p12`).
   - Certificate generation automated through key-generator module.

2. **JWT Authentication**
   - End-user authentication uses `JwtAuthFilter` in user-service.
   - Admin-service mints **service-to-service JWT tokens** with audience, roles, and type claims (`typ=s2s`).
   - User-service validates incoming S2S tokens with `S2SAuthorizationFilter`.

3. **Authorization**
   - Endpoints are secured with role-based access checks.
   - `/api/auth/**` endpoints are public for login, signup, refresh.
   - `/internal/**` endpoints are reserved for S2S traffic.
   - File operations require appropriate permissions and audit logging.

---

## Internationalization (i18n)

- Message bundles are located in `src/main/resources/i18n/messages.properties`.
- Available locales:
  - `messages_en.properties` (English)
  - `messages_ar.properties` (Arabic, UTF-8)
- Error messages and validation feedback are localized based on `Accept-Language` HTTP headers.
- File operation responses support internationalization.

---

## API Documentation

Each service exposes OpenAPI documentation via SpringDoc:
- **User Service**: https://localhost:8445/swagger-ui.html
- **Admin Service**: https://localhost:8446/swagger-ui.html

---

## Development Setup

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 12+
- Redis 6+
- MinIO Server (for file operations)
- OpenSSL (for certificate generation)
- Docker & Docker Compose (recommended)
- IntelliJ IDEA recommended

### Database Setup
Databases must be created manually before running migrations:
