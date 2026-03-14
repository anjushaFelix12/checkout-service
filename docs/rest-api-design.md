
# REST API Technical Design

This document describes the REST API design for the **Supermarket Checkout System**.

## Goals

The API supports:

- product catalog retrieval
- active weekly offer retrieval
- cart creation and management
- checkout price calculation

---

## Base Path

`/api/v1`

---

## Design Principles

- REST-style resource naming
- JSON request/response format
- separation between cart management and checkout calculation
- weekly offers applied dynamically during checkout
- cart stores only products and quantities

---

## Resources

### Product

Represents a purchasable product.

Example:

```json
{
  "id": "3c6eb911-dbb1-4f93-9c90-3f7138ff71d0",
  "code": "APPLE",
  "name": "Apple",
  "unitPrice": 0.30
}
```

### Offer

Represents an active weekly bundle offer.

Example:

```json
{
  "id": "ee8bc32d-bf4e-4a89-ac8d-cb2b9b77f76a",
  "productCode": "APPLE",
  "quantity": 2,
  "bundlePrice": 0.45,
  "description": "2 for €0.45"
}
```

### Cart

Represents a shopping cart.

Example:

```json
{
  "id": "c1f7f8e4-5f24-4c9e-a2f2-4fdb0b8c0c7b",
  "status": "OPEN",
  "items": [
    {
      "productCode": "APPLE",
      "quantity": 3
    }
  ]
}
```

---

## Endpoints

### Get Products

**Request**

Full product list with embedded active offer

```
GET /api/v1/products
```

**Response**

```json
[
  {
    "id": "3c6eb911-dbb1-4f93-9c90-3f7138ff71d0",
    "code": "APPLE",
    "name": "Apple",
    "unit": "piece",
    "unitPrice": 0.30,
    "activeOffer": {
      "quantity": 2,
      "bundlePrice": 0.45,
      "description": "2 for €0.45"
    }
  },
  {
    "id": "d0756cf2-5693-4bf2-a6f2-0cf550814f31",
    "code": "BANANA",
    "name": "Banana",
    "unit": "piece",
    "unitPrice": 0.50,
    "activeOffer": null
  }
]
```

---

### Get Active Offers

All active offers as a standalone list

**Request**

```
GET /api/v1/offers
```

**Response**

```json
[
  {
    "id": "ee8bc32d-bf4e-4a89-ac8d-cb2b9b77f76a",
    "productCode": "APPLE",
    "quantity": 2,
    "bundlePrice": 0.45
  }
]
```

---

### Create Cart

**Request**

```
POST /api/v1/carts
```

**Response**

```json
{
  "id": "c1f7f8e4-5f24-4c9e-a2f2-4fdb0b8c0c7b",
  "status": "OPEN",
  "items": []
}
```

---

### Get Cart

**Request**

```
GET /api/v1/carts/{cartId}
```

**Response**

```json
{
  "id": "c1f7f8e4-5f24-4c9e-a2f2-4fdb0b8c0c7b",
  "status": "OPEN",
  "items": [
    {
      "productCode": "APPLE",
      "productName": "Apple",
      "unit": "piece",
      "unitPrice": 0.30,
      "quantity": 3,
      "lineTotal": 0.90
    },
    {
      "productCode": "MILK",
      "productName": "Milk",
      "unit": "liter",
      "unitPrice": 1.20,
      "quantity": 1,
      "lineTotal": 1.20
    }
  ],
  "itemCount": 2,
  "totalItems": 4,
  "subtotal": 2.10
}
```

---

### Add Item to Cart

**Request**

```
POST /api/v1/carts/{cartId}/items
```

**Body**

```json
{
  "productCode": "APPLE",
  "quantity": 3
}
```

---

### Update Cart Item

**Request**

```
PUT /api/v1/carts/{cartId}/items/{productCode}
```

**Body**

```json
{
  "quantity": 5
}
```

---

### Remove Item from Cart

**Request**

```
DELETE /api/v1/carts/{cartId}/items/{productCode}
```

**Response**

```
204 No Content
```

---

### Checkout Cart

**Request**

```
POST /api/v1/carts/{cartId}/checkout
```

**Response**

```json
{
  "cartId": "c1f7f8e4-5f24-4c9e-a2f2-4fdb0b8c0c7b",
  "currency": "EUR",
  "items": [
    {
      "productCode": "APPLE",
      "productName": "Apple",
      "quantity": 3,
      "unitPrice": 0.30,
      "subtotal": 0.90
    }
  ],
  "subtotal": 0.90,
  "discounts": [
    {
      "productCode": "APPLE",
      "description": "2 for 0.45",
      "amount": 0.15
    }
  ],
  "totalDiscount": 0.15,
  "total": 0.75
}
```

---

## Validation Rules

- `productCode` must reference an existing product
- `quantity` must be greater than `0`
- cart must be in `OPEN` state for modification
- checkout can only be performed on an `OPEN` cart

---

## Notes

- All monetary values should use **BigDecimal** in the backend
- Weekly offers are applied during checkout
- Cart persistence stores only products and quantities
