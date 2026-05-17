# TODO: Bringing Backend API in sync with Frontend

This document outlines the tasks required to align the Procar Backend API with the current frontend product model.

## 1. User Roles & Profile (Priority 1)
- [ ] Update `UserRole` enum: `CUSTOMER`, `BROKER`, `ADMIN`
- [ ] Implement `UserStatus`: `ACTIVE`, `BLOCKED`
- [ ] Implement `CompanyStatus`: `NOT_STARTED`, `PENDING`, `VERIFIED`, `REJECTED`
- [ ] Implement `DepositStatus`: `NOT_PAID`, `PENDING`, `ACTIVE`, `FORFEITED`
- [ ] Update `User` entity/DTO with new fields:
    - `role`, `companyName`, `country`, `status`, `companyStatus`, `depositStatus`, `brokerId` (for BROKER role)
- [ ] Update `GET /users/me` to return full Procar profile
- [ ] Update registration/auth logic for new roles
- [ ] Create corresponding .feature files in smoke-tests to validate user roles and profiles

## 2. Broker Organizations (Priority 2)
- [ ] Enhance existing `Broker` model:
    - Add `status`: `ACTIVE`, `BLOCKED`
    - Add `displayName`, `country`, `contactEmail` fields
    - Ensure `name` is used as `companyName`
- [ ] Implement status update endpoint:
    - `POST /admin/brokers/{id}/status` (accepting `BrokerStatus` enum: `ACTIVE`, `BLOCKED`)
- [ ] Change `PUT /admin/brokers/{id}` to `PATCH /admin/brokers/{id}`
- [ ] Create corresponding .feature files in smoke-tests to validate broker organization management
- [ ] Implement logic to hide `SELECT` lots when a broker is blocked

## 3. User Management (Priority 2)
- [ ] Implement missing Admin endpoints:
    - `DELETE /admin/users/{id}` (Missing in current API)
    - `POST /admin/users/{id}/status` (accepting `UserStatus` enum: `ACTIVE`, `BLOCKED`)
    - `PATCH /admin/users/{id}` (Generic update instead of multiple specific endpoints)
- [ ] Update `POST /admin/users`:
    - Ensure it handles `brokerId` for `BROKER` role
- [ ] Create corresponding .feature files in smoke-tests to validate admin user management
    - Set initial `companyStatus` and `depositStatus`
- [ ] Add self-protection for `ADMIN` (prevent self-block/delete)

## 4. Lots: Dual Lane & Sale Mode (Priority 3)
- [ ] Update `Lot` model with `brand` (`AUCTIONS`, `SELECT`) and `saleMode` (`OFFER`, `AUCTION`)
- [ ] Implement `LotStatus`: `DRAFT`, `PUBLISHED`, `UNPUBLISHED`, `SOLD`, `CANCELLED`
- [ ] Implement `AuctionStatus` (e.g., `UPCOMING`, `LIVE`, `ENDED`, `SELLER_DECISION`)
- [ ] Update Public endpoints:
    - `GET /lots?brand=...` (filtering by brand)
    - Apply visibility rules (only published lots from active brokers)
- [ ] Update Admin endpoints:
- [ ] Create corresponding .feature files in smoke-tests to validate lot management and visibility
    - `POST /admin/lots/{id}/publish`
    - `POST /admin/lots/{id}/unpublish`
    - `POST /admin/lots/{id}/end-auction`

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
