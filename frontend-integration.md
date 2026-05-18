# Backend Integration Guide for Frontend

This document tracks changes in the Backend API that require updates or reintegration on the Frontend side.

## 1. Auth & User Profile
- **Status**: Completed
- **Changes**:
    - `UserRole` updated: `CUSTOMER`, `BROKER`, `ADMIN` (Replaces `USER`, `ADMIN`)
    - New User fields: `status` (ACTIVE/BLOCKED), `verificationStatus`, `depositStatus`, `brokerId`
    - `GET /users/me`: Now returns full Procar profile including the above fields.
- **Frontend Action**: Update Auth provider to handle new roles and statuses. Update User profile interfaces.

## 2. Broker Organizations
- **Status**: Completed
- **Changes**:
    - Endpoints moved from `/api/admin/brokers` to `/api/admin/brokers` (standardized).
    - New Broker fields: `companyName`, `displayName`, `country`, `contactEmail`, `status`.
    - New endpoint: `POST /admin/brokers/{id}/status` for activation/blocking.
    - New endpoint: `PATCH /admin/brokers/{id}` for generic updates.
- **Frontend Action**: Update Broker Management dashboard to use new fields and status update pattern.

## 3. User Management
- **Status**: Completed
- **Changes**:
    - New endpoint: `POST /api/admin/users/{id}/status` for activation/blocking.
    - New endpoint: `PATCH /api/admin/users/{id}` for generic updates.
    - New endpoint: `DELETE /api/admin/users/{id}` for full deletion (including auth records).
- **Frontend Action**: Update User Management dashboard to use new fields, status update pattern, and deletion.

## 4. Lots & Catalog
- **Status**: Completed
- **Changes**:
    - **Lot Brands**: `lane` field is replaced by `brand`. Possible values: `AUCTIONS`, `SELECT`.
    - **Lot Statuses**:
        - `DRAFT`: Initial state.
        - `PENDING`: Ready but not yet visible to buyers (corresponds to `UNPUBLISHED`).
        - `ACTIVE`: visible and open for bidding/offers (corresponds to `PUBLISHED`).
        - `AWAIT_SELLER_CONFIRMATION`: Intermediate state after auction/offer ends, waiting for broker confirmation.
        - `AWAITING_PAYMENT`: Winner confirmed, waiting for transaction.
        - `AWAITING_SHIPMENT`, `IN_TRANSIT`, `COMPLETED`: Delivery tracking statuses.
        - `SOLD`: Final successful state.
        - `CANCELLED`: Deal aborted.
    - **New Admin Flow Endpoints**:
        - `POST /api/admin/lots/{id}/publish`: Moves lot to `ACTIVE`.
        - `POST /api/admin/lots/{id}/unpublish`: Moves lot to `PENDING`.
        - `POST /api/admin/lots/{id}/confirm-availability`: Moves lot from `AWAIT_SELLER_CONFIRMATION` to `AWAITING_PAYMENT`.
- **Frontend Action**:
    - Update lot filtering (use `brand` query parameter).
    - Map `AWAIT_SELLER_CONFIRMATION` status in the UI (e.g., "Confirming with Seller").
    - Implement Admin buttons for "Publish", "Unpublish", and "Confirm Availability" (for Brokers).

## 5. Offers
- **Status**: Pending
- **Changes**:
    - New entity `Offer` (distinct from `Bid`).
    - Endpoints: `POST /lots/{id}/offers`, `GET /users/me/offers`.
- **Frontend Action**: Implement manual offer flow using the new endpoints.

## 6. Deals & Payments
- **Status**: Pending
- **Changes**:
    - New entity `Deal` created after Accept Offer.
    - Fields for tracking `VehiclePaymentStatus` and `ProcarFeeStatus`.
    - `PaymentInstructions` object available in Deal details.
- **Frontend Action**: Implement Deal details page and payment instruction visualization.

---
*Note: This file is updated automatically as backend changes are implemented.*
