# TODO: Bringing Backend API in sync with Frontend

This document outlines the tasks required to align the Procar Backend API with the current frontend product model.

## 1. User Roles & Profile (Priority 1)
- [x] Update `UserRole` enum: `CUSTOMER`, `BROKER`, `ADMIN`
- [x] Implement `UserStatus`: `ACTIVE`, `BLOCKED`
- [x] Implement `CompanyStatus`: `NOT_STARTED`, `PENDING`, `VERIFIED`, `REJECTED`
- [x] Implement `DepositStatus`: `NOT_PAID`, `PENDING`, `ACTIVE`, `FORFEITED`
- [x] Update `User` entity/DTO with new fields:
    - `roles`, `companyName`, `country`, `status`, `companyStatus`, `depositStatus`, `brokerId` (for BROKER role)
- [x] Update `GET /users/me` to return full Procar profile
- [ ] Update registration/auth logic for new roles
- [x] Create corresponding .feature files in smoke-tests to validate user roles and profiles

## 2. Broker Organizations (Priority 2)
- [x] Enhance existing `Broker` model:
    - Add `status`: `ACTIVE`, `BLOCKED`
    - Add `displayName`, `country`, `contactEmail` fields
    - Ensure `name` is used as `companyName`
- [x] Implement status update endpoint:
    `POST /admin/brokers/{id}/status` (accepting `BrokerStatus` enum: `ACTIVE`, `BLOCKED`)
- [x] Change `PUT /admin/brokers/{id}` to `PATCH /admin/brokers/{id}`
- [x] Create corresponding .feature files in smoke-tests to validate broker organization management

## 3. User Management (Priority 2)
- [x] Implement missing Admin endpoints:
    `DELETE /admin/users/{id}` (Missing in current API)
    `POST /admin/users/{id}/status` (accepting `UserStatus` enum: `ACTIVE`, `BLOCKED`)
    `PATCH /admin/users/{id}` (Generic update instead of multiple specific endpoints)
- [x] Update `POST /admin/users`:
    Ensure it handles `brokerId` for `BROKER` role
- [x] Create corresponding .feature files in smoke-tests to validate admin user management
    - Set initial `companyStatus` and `depositStatus`
- [x] Add self-protection for `ADMIN` (prevent self-block/delete)

## 4. Lots: Dual Lane & Sale Mode (Priority 3)
- [x] Update `Lot` model:
    - Add `brand` (`AUCTIONS`, `SELECT`)
    - Expand `LotStatus` with `AWAIT_SELLER_CONFIRMATION`
- [x] Implement explicit Admin endpoints for flow control:
    - `POST /admin/lots/{id}/publish` (sets `ACTIVE`)
    - `POST /admin/lots/{id}/unpublish` (sets `PENDING`)
    - `POST /admin/lots/{id}/confirm-availability` (moves from `AWAIT_SELLER_CONFIRMATION` to `AWAITING_PAYMENT`)
- [x] Update `FinishAuctionTask` logic:
    - Change transition: `ACTIVE` -> `AWAIT_SELLER_CONFIRMATION` (instead of direct `AWAITING_PAYMENT`)
- [x] Write to frontend-integration.md how statuses work
- [x] Update Public endpoints:
    - `GET /lots?brand=...` (filtering by brand)
    - Apply visibility rules (only published lots from active brokers)
- [x] Update Admin endpoints - verify we have all the endpoints for statuses handling

## 5. Bids & Auction Lifecycle (Priority 4)
- [x] Refine `BidStatus`: `WINNING`, `OUTBID`, `WON`, `LOST`
- [x] Update `FinishAuctionTask` to trigger bid finalization:
    - Sets highest bid to `WON`
    - Sets all other bids to `LOST`
- [x] Implement Buyout bid logic:
    - Immediately sets bid to `WON`
    - Triggers lot transition to `AWAITING_PAYMENT` (as price is pre-agreed)
- [x] Create corresponding .feature files in smoke-tests to validate auction lifecycle and bid transitions

## 6. Admin-Led Verification Flow (Priority 1)
- [ ] Implement `POST /users/me/verification/start` (Customer submits info/documents)
- [x] Implement Admin verification endpoints (Handled via generic `PATCH /admin/users/{id}` or `PATCH /admin/brokers/{id}`)
- [ ] Implement access rule enforcement in Gateway (Check `VERIFIED` + `ACTIVE` deposit before allowing Bids/Offers)
- [ ] Create corresponding .feature files in smoke-tests to validate the full verification lifecycle (Start -> Admin Approve -> Bid allowed)
