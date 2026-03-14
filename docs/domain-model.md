# Supermarket Checkout System — Domain Model

This document describes the domain and database model for the **Supermarket Checkout System**.

The system supports:
- product catalog
- shopping cart
- weekly bundle offers
- checkout price calculation

---

# Domain Entities

## Product

Represents a purchasable item in the product catalog.

Fields:

- `id` (PK)
- `code`
- `name`
- `unit_price`

Example:


APPLE → €0.30
BANANA → €0.50


---

## Offer

Represents a weekly bundle discount for a product.

Fields:

- `id` (PK)
- `product_id` (FK → products.id)
- `quantity`
- `bundle_price`
- `created_at`
- `valid_until`

Example:


Product: Apple
Offer: 2 for €0.45


Meaning:


1 Apple → €0.30
2 Apples → €0.45
3 Apples → €0.75


---

## Cart

Represents a shopping cart created by a user/session.

Fields:

- `id` (PK)
- `status`
- `created_at`
- `updated_at`

Cart status lifecycle:


OPEN → CHECKED_OUT
OPEN → ABANDONED


---

## CartItem

Represents a product added to a cart.

Fields:

- `id` (PK)
- `cart_id` (FK → carts.id)
- `product_id` (FK → products.id)
- `quantity`

Example:


Cart
├─ Apple x3
└─ Banana x2


---

# Entity Relationships Summary:

- A **Product** can have **zero or one active Offer**
- A **Cart** can contain **multiple CartItems**
- Each **CartItem** references exactly **one Product**

---
