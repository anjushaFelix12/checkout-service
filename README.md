
# Supermarket Checkout System

This project implements a **simplified supermarket checkout system** that supports products, shopping carts, and weekly bundle offers.

The system automatically applies applicable offers during checkout and calculates the final cart total.

---

# Features

- Product catalog
- Weekly bundle offers
- Shopping cart management
- Automatic offer application during checkout
- RESTful API design
- Layered backend architecture
- Uses Liquibase for database migrations
- Provides Swagger UI documentation

---

## Documentation

Project documentation is available in the `docs/` directory:

- [Architecture](docs/architecture.md) — Backend architecture and module structure
- [Domain Model](docs/domain-model.md) — Database entities and relationships
- [REST API Design](docs/rest-api-design.md) — API endpoints, request/response models

---

# 📖 Swagger UI

API documentation available at: http://localhost:8080/swagger-ui/index.html

---

# Running the Project

`mvn spring-boot:run`

The API will be available at:

http://localhost:8080

Swagger UI:

http://localhost:8080/swagger-ui.html

---
# Future Improvements

Possible extensions include:

- User authentication and user-owned carts
- Admin management for products and offers
- Inventory validation
- Caching for product catalog
- Observability (metrics and tracing)
- Integration tests for API endpoints

---

# Summary

This project demonstrates:

- clean REST API design
- modular backend architecture
- correct pricing logic for bundle offers
- separation of concerns
- end-to-end feature implementation

The focus of the implementation is **correct pricing logic and maintainable architecture** while keeping the system simple and extensible.
