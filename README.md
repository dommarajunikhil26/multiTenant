# Multi-Tenant Project Management API

A robust, multi-tenant Spring Boot SaaS backend designed using a **Shared Database, Shared Schema** isolation strategy. The application securely handles stateless user authentication using JSON Web Tokens (JWT) and guarantees strict tenant data isolation across all HTTP requests using a custom servlet filter pipeline and a thread-local execution context.

---

## 🚀 What the Project Does

This project serves as the core backend for a multi-tenant Software-as-a-Service (SaaS) application. It allows multiple independent organizations (tenants), such as *Acme* and *Globex*, to manage their internal projects on a shared infrastructure.

### Key Guarantees:
*   **Complete Data Isolation:** Users belonging to Tenant A can never view, mutate, or interact with data belonging to Tenant B.
*   **Identity & Scope Awareness:** Every API request is checked for valid authentication credentials and mapped automatically to its corresponding tenant scope before hitting database repositories.

---

## 📐 Filter Chain Architecture Diagram

The application intercepts every inbound HTTP request through a structured Spring Security filter pipeline to extract authentication tokens and assign tenant context before reaching the REST Controller.

```text
       [ Inbound HTTP Request ]
                  │
                  ▼
        ┌───────────────────┐
        │   JwtAuthFilter   │ ◄── Validates "Authorization: Bearer <token>"
        └─────────┬─────────┘
                  │ (If Valid)
                  ▼
        ┌───────────────────┐
        │  SecurityContext  │ ◄── Sets Authentication (Principal, Roles)
        └─────────┬─────────┘
                  │
                  ▼
        ┌───────────────────┐
        │   TenantFilter    │ ◄── Extracts Tenant ID from JWT Claims
        └─────────┬─────────┘
                  │
                  ▼
   ┌─────────────────────────────┐
   │ TenantContext (ThreadLocal) │ ◄── Binds active Tenant ID to current thread
   └──────────────┬──────────────┘
                  │
                  ▼
        ┌───────────────────┐
        │  REST Controller  │ ◄── Scopes database queries using TenantContext
        └─────────┬─────────┘
                  │
                  ▼
  ┌───────────────────────────────┐
  │ Finally Block: TenantContext  │ ◄── Clears ThreadLocal to prevent leaks
  └───────────────────────────────┘