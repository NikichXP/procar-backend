# Backend Integration Guide for Frontend

This document tracks changes in the Backend API that require updates or reintegration on the Frontend side.

## 0. Global API Changes
- **Status**: Completed
- **Changes**:
    - **Removed `/api` prefix from all endpoints.**  
    - All endpoints previously under `/api/*` are now directly under the root (e.g., `/api/lots` -> `/lots`, `/api/admin/*` -> `/admin/*`).
- **Frontend Action**: Update `baseUrl` or API client configuration to remove `/api` from all request paths.

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
    - Endpoints moved to `/admin/brokers` (standardized, `/api` prefix removed).
    - New Broker fields: `companyName`, `displayName`, `country`, `contactEmail`, `status`.
    - New endpoint: `POST /admin/brokers/{id}/status` for activation/blocking.
    - New endpoint: `PATCH /admin/brokers/{id}` for generic updates.
- **Frontend Action**: Update Broker Management dashboard to use new fields and status update pattern.

## 3. User Management
- **Status**: Completed
- **Changes**:
    - New endpoint: `POST /admin/users/{id}/status` for activation/blocking.
    - New endpoint: `PATCH /admin/users/{id}` for generic updates.
    - New endpoint: `DELETE /admin/users/{id}` for full deletion (including auth records).
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
        - `POST /admin/lots/{id}/publish`: Moves lot to `ACTIVE`.
        - `POST /admin/lots/{id}/unpublish`: Moves lot to `PENDING`.
        - `POST /admin/lots/{id}/confirm-availability`: Moves lot from `AWAIT_SELLER_CONFIRMATION` to `AWAITING_PAYMENT`.
- **Frontend Action**:
    - Update lot filtering (use `brand` query parameter).
    - Map `AWAIT_SELLER_CONFIRMATION` status in the UI (e.g., "Confirming with Seller").
    - Implement Admin buttons for "Publish", "Unpublish", and "Confirm Availability" (for Brokers).

## 5. Bids & Auction Lifecycle
- **Status**: Completed
- **Changes**:
    - `BidStatus` refined: `WINNING`, `OUTBID`, `WON`, `LOST`.
    - **Auction End**: When an auction ends, the highest bid is automatically set to `WON`, and all others to `LOST`.
    - **Buyout Logic**: Placing a bid at the `buyoutPrice` immediately sets the bid to `WON` and moves the lot to `AWAITING_PAYMENT`.
- **Frontend Action**: Update bid status badges in user profile and lot details. Handle immediate transition to "Won" state on successful buyout.

## 6. Admin-Led Verification Flow
- **Status**: Completed (Admin Side)
- **Changes**:
    - **User Verification**: Admin can verify/reject users via `PATCH /admin/users/{id}` by updating `verificationStatus` (`NOT_STARTED`, `PENDING`, `VERIFIED`, `REJECTED`) and `depositStatus` (`NOT_PAID`, `PENDING`, `ACTIVE`, `FORFEITED`).
    - **Broker Activation**: Brokers are activated/blocked via `POST /admin/brokers/{id}/status`.
- **Frontend Action**: 
    - Implement Admin UI for toggling verification and deposit statuses on the User detail page.
    - Implement Admin UI for broker status management.
    - (Upcoming) Frontend must handle restricted access for non-verified users.

---
*Note: This file is updated automatically as backend changes are implemented.*
