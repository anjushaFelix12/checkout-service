
# Backend Architecture

## Overview

The backend is implemented as a **modular monolith using layered architecture**.
The application runs as a single Spring Boot service but is organized by **business features (modules)** to maintain clear boundaries and allow future extensibility.

This approach keeps the system simple for the current scope while ensuring maintainability and scalability.

---

## Architecture Style

The system follows two key architectural principles:

### 1. Modular Monolith

The application is split into logical modules based on business capabilities:

- product
- offer
- cart
- checkout
- pricing

Each module encapsulates its own controllers, services, repositories, domain models, and DTOs.

This structure keeps related code together and improves maintainability.

### 2. Layered Architecture

Inside each module the application follows a standard layered architecture:

```
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Each layer has a clear responsibility.

| Layer | Responsibility |
|------|------|
| Controller | Handles HTTP requests and responses |
| Service | Contains business logic and orchestration |
| Repository | Handles database access |
| Domain | Represents core business entities |

---

## Project Structure

```
src/main/java/com/backend/checkout_service

product
  controller
  service
  repository
  domain
  dto

offer
  controller
  service
  repository
  domain
  dto

cart
  controller
  service
  repository
  domain
  dto

checkout
  controller
  service
  dto

pricing
  service
  dto
  rule

common
  config
  exception
```

---

## Module Responsibilities

### Product Module

Responsible for managing the product catalog.

Endpoint:

```
GET /api/products
```

Responsibilities:

- Retrieve available products
- Provide product price and metadata

---

### Offer Module

Responsible for retrieving active weekly bundle offers.

Endpoint:

```
GET /api/offers
```

Responsibilities:

- Provide offer rules
- Associate offers with products

---

### Cart Module

Responsible for managing shopping carts and cart items.

Endpoints:

```
POST /api/carts
GET /api/carts/{cartId}
POST /api/carts/{cartId}/items
PUT /api/carts/{cartId}/items/{productCode}
DELETE /api/carts/{cartId}/items/{productCode}
```

Responsibilities:

- Create carts
- Add or remove items
- Maintain cart state

---

### Checkout Module

Responsible for calculating the final cart total.

Endpoint:

```
POST /api/carts/{cartId}/checkout
```

Responsibilities:

- Retrieve cart data
- Apply pricing rules
- Return final totals and discounts

---

### Pricing Module

The pricing module contains the **core business logic for applying offers and calculating totals**.

Responsibilities:

- Apply bundle pricing rules
- Calculate subtotal
- Calculate discounts
- Produce final checkout totals

Separating pricing logic improves **testability and maintainability**.

#### Open / Closed Principle

The pricing engine follows the **Open/Closed Principle**:

> Software entities should be **open for extension but closed for modification**.

New pricing rules can be added without modifying existing code.

For example, new rules could be implemented such as:

- Buy X Get Y Free
- Percentage discounts
- Member discounts
- Seasonal promotions

To introduce a new rule, simply create a new implementation of `PricingRule`:

---

## Request Flow Example (Checkout)

```
Client
  ↓
CheckoutController
  ↓
CheckoutService
  ↓
CartRepository
ProductRepository
OfferRepository
  ↓
PricingService
  ↓
CheckoutResponse
```

---

## Design Decisions

### Modular Monolith

The system is implemented as a single deployable application because the problem scope is relatively small.
Splitting into microservices would introduce unnecessary operational complexity.

However, the modular structure allows the system to evolve into separate services if needed.

### Separation of Pricing Logic

Pricing and discount calculation are implemented in a dedicated `PricingService` to keep checkout orchestration clean and maintainable.

### DTO-based API

The API exposes **DTOs rather than database entities** to avoid leaking persistence models and to provide stable API contracts.

### Monetary Calculations

All monetary values are represented using **BigDecimal** to avoid floating-point precision issues.

#### Algorithm Used

The algorithm works like this:

```
bundleCount = quantity / offerQuantity
remainder   = quantity % offerQuantity

```

Then calculate:

```
bundleTotal   = bundleCount × bundlePrice
remainderCost = remainder × unitPrice

total = bundleTotal + remainderCost
```



---

## Future Improvements

Potential improvements for a production system include:

- User authentication and user-owned carts
- Admin management for products and offers
- Inventory validation
- Observability (metrics, tracing, structured logging)
- Caching for product catalog
- Rate limiting and API security

---

## Summary

The backend architecture prioritizes:

- simplicity
- maintainability
- clear module boundaries
- separation of concerns

Using a **modular monolith with layered architecture** provides a solid foundation for future system evolution while keeping the current implementation straightforward.
