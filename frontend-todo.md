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

## 5. Offers (Priority 4)
- [ ] Create `Offer` entity and repository
- [ ] Implement `OfferStatus`: `SUBMITTED`, `ACCEPTED`, `REJECTED`, `CANCELLED`
- [ ] Implement `OfferSource`: `MANUAL_OFFER`, `AUCTION_WINNER`
- [ ] Implement endpoints:
    - `POST /lots/{lotId}/offers` (CUSTOMER makes offer)
- [ ] Create corresponding .feature files in smoke-tests to validate offer flow (manual and auction winner)
    - `GET /users/me/offers` (CUSTOMER sees their offers)
    - `GET /admin/offers` (ADMIN/BROKER sees relevant offers)
    - `POST /admin/offers/{id}/accept`
    - `POST /admin/offers/{id}/reject` (with `rejectionReason`)

## 6. Bids & Auction Lifecycle (Priority 5)
- [ ] Update `Bid` model and statuses (`winning`, `outbid`, `won_pending_seller`, etc.)
- [ ] Create corresponding .feature files in smoke-tests to validate auction lifecycle and bid transitions
- [ ] Implement `POST /admin/lots/{lotId}/end-auction` logic:
    - Mark auction as `seller_decision`
    - Create `auction_winner` offer from highest bid
    - Update bid statuses
- [ ] Ensure Bids and Offers are handled as distinct but related entities
- [ ] Create corresponding .feature files in smoke-tests to validate auction lifecycle and bid transitions

## 7. Deals & Payments (Priority 4 & 6)
- [ ] Create `Deal` entity and repository
- [ ] Implement `DealStatus`: `CREATED`, `CONFIRMED`, `PAID`, `COMPLETED`, `CANCELLED`
- [ ] Implement `VehiclePaymentStatus`: `PENDING`, `INSTRUCTIONS_READY`, `PAID`
- [ ] Implement `ProcarFeeStatus`: `PENDING`, `PAID`
- [ ] Implement `PaymentInstructions` model
- [ ] Implement endpoints:
    - `GET /users/me/deals`
    - `GET /admin/deals`
    - `POST /admin/deals/{id}/payment-instructions`
    - `POST /admin/deals/{id}/mark-vehicle-paid`
    - `POST /admin/deals/{id}/mark-procar-fee-paid`
    - `POST /admin/deals/{id}/complete`
- [ ] Integration with payment provider (Stripe/etc.) for deposits and fees (Priority 6)
- [ ] Create corresponding .feature files in smoke-tests to validate deal creation and payment flow

## 8. Admin-Led Verification Flow (Priority 1)
- [ ] Implement `POST /users/me/verification/start` (Customer submits info)
- [ ] Implement Admin verification endpoints for Customers:
    - `POST /admin/users/{id}/verification/approve`
    - `POST /admin/users/{id}/verification/reject`
- [ ] Implement Admin verification flow for Broker Organizations:
    - Ensure Brokers are verified/activated only by ADMIN
- [ ] Enforce access rules: Only verified & deposited CUSTOMERs can bid/offer
- [ ] Create corresponding .feature files in smoke-tests to validate admin-led verification for both Customers and Brokers
