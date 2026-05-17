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
- **Status**: Pending
- **Changes**:
    - `lane` renamed to `brand` (Values: `AUCTIONS`, `SELECT`).
    - New `saleMode`: `OFFER`, `AUCTION`.
    - `LotStatus` updated to: `DRAFT`, `PUBLISHED`, `UNPUBLISHED`, `SOLD`, `CANCELLED`.
- **Frontend Action**: Update lot filtering (query param `brand` instead of `lane`). Map new statuses to UI badges.

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
