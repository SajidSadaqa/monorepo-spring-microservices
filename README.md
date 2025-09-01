# Monorepo Spring Microservices

This repository demonstrates a production-grade microservices architecture built with Spring Boot, designed for clarity, modularity, and scalability. It follows strict layered architecture and clean coding principles to model a real-world enterprise system.

---

## Overview

The system is composed of two core services:

- **user-service**: Responsible for user management, authentication, and token issuance.
- **admin-service**: Provides administrative operations and proxies requests to user-service using service-to-service (S2S) authentication.

Both services are packaged as independent Spring Boot applications, secured with TLS, and communicate via HTTPS and signed JWT tokens.

---

## Architecture

### Key Characteristics
- **Domain-Driven Design (DDD) Inspired**: Code is organized into `application`, `domain`, `infrastructure`, and `web` layers.
- **Service-to-Service Security**: Admin-service authenticates itself to user-service using minted JWT tokens signed by a trusted keypair.
- **TLS Everywhere**: Each service runs with its own keystore and truststore. Mutual TLS is supported in development and production.
- **Stateless**: Sessions are disabled. Authentication is purely token-based.
- **Localization**: Built-in i18n with message bundles in English and Arabic.

---

## Technologies Used

| Technology | Purpose |
|------------|---------|
| **Spring Boot 3.x** | Core application framework |
| **Spring Web (MVC)** | RESTful API endpoints |
| **Spring Security** | Authentication and authorization with JWT |
| **Spring Data JPA + Hibernate** | Persistence layer |
| **PostgreSQL** | Database engine |
| **Flyway** | Database migrations |
| **Feign (OpenFeign)** | Inter-service HTTP client |
| **MapStruct** | Compile-time DTO to entity mapping |
| **Lombok** | Boilerplate reduction |
| **Jakarta Validation** | Input validation with localized error messages |
| **SpringDoc / Swagger** | OpenAPI documentation and interactive UI |
| **Logback + SLF4J** | Logging |
| **Docker** | Containerization (future deployment) |

---

## Modules and Directories

### user-service
- **application/**  
  Contains use-case logic, DTOs, and service implementations.
- **domain/**  
  JPA entities and aggregates representing core business data.
- **infrastructure/**  
  Database repositories, persistence configuration, and external integrations.
- **web/**  
  REST controllers exposing the API to clients.
- **security/**  
  JWT filters, S2S authorization filters, and Spring Security configuration.
- **exceptions/**  
  Global exception handler returning structured `ProblemDetail` responses.

### admin-service
- **application/**  
  S2S communication layer with Feign clients, DTOs, and proxy services.
- **security/**  
  JWT minting for S2S calls and TLS configuration for outbound requests.
- **web/**  
  REST controllers exposing administrative APIs that proxy to user-service.
- **exceptions/**  
  Global exception handler aligned with user-service for consistent error models.

---

## Security Model

1. **TLS Setup**  
   - Each service owns a **keystore** (`*-keystore.p12`) containing its private key and certificate.
   - Each service trusts the other’s public certificate via a **truststore** (`*-truststore.p12`).

2. **JWT Authentication**  
   - End-user authentication uses `JwtAuthFilter` in user-service.
   - Admin-service mints **service-to-service JWT tokens** with audience, roles, and type claims (`typ=s2s`).
   - User-service validates incoming S2S tokens with `S2SAuthorizationFilter`.

3. **Authorization**  
   - Endpoints are secured with role-based access checks.
   - `/api/auth/**` endpoints are public for login, signup, refresh.
   - `/internal/**` endpoints are reserved for S2S traffic.
   - All other endpoints require authentication.

---

## Internationalization (i18n)

- Message bundles are located in `src/main/resources/i18n/messages.properties`.
- Available locales:
  - `messages_en.properties` (English)
  - `messages_ar.properties` (Arabic, UTF-8)
- Error messages and validation feedback are localized based on `Accept-Language` HTTP headers.

---

## API Documentation

Each service exposes OpenAPI documentation via SpringDoc:

- **User Service**:  
  https://localhost:8445/swagger-ui.html  

- **Admin Service**:  
  https://localhost:8446/swagger-ui.html  

---

## Development Setup

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL
- OpenSSL (for certificate generation)
- IntelliJ IDEA recommended

### Database
Databases must be created manually before running migrations:

```sql
CREATE DATABASE userdb;
CREATE DATABASE admindb;
