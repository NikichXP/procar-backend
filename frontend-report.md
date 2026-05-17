# Procar Backend API — что нужно привести в соответствие с текущим фронтом

## 0. Контекст

Текущий frontend Procar уже ушёл дальше обычного auction listing. Сейчас он является рабочим **source of truth** для продуктовой модели MVP.

Во frontend уже реализованы:

- dual-lane модель: `Auctions` и `Select`;
- роли: `buyer`, `seller_admin`, `super_admin`;
- seller companies как отдельные сущности;
- buyer account с верификацией и депозитом;
- seller dashboard;
- super-admin dashboard;
- Select lots, принадлежащие seller company;
- manual offer flow;
- auction bid flow;
- auction winner → seller decision → deal;
- deals и payment instructions;
- mock/localStorage transaction layer.

Текущий Swagger backend выглядит как generic auction API: `USER / ADMIN`, `/lots`, `/bids`, `/users/me/bids`, watchlist и базовая auth-модель. Это полезная база, но она **не покрывает текущую Procar transaction-модель**.

Главное требование: backend не должен заставлять frontend откатываться к generic auction model. Нужно привести backend contract к текущей продуктовой модели Procar.

---

## 1. Роли пользователей

Сейчас в API есть роли:

```txt
USER
ADMIN
```

Нужно привести к ролям frontend:

```txt
buyer
seller_admin
super_admin
```

### Требуемая модель User

```ts
type UserRole = "buyer" | "seller_admin" | "super_admin";
type UserStatus = "active" | "blocked";
type CompanyStatus = "not_started" | "pending" | "verified" | "rejected";
type DepositStatus = "not_paid" | "pending" | "active" | "forfeited";

interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  companyName?: string;
  country?: string;
  status: UserStatus;
  companyStatus?: CompanyStatus;
  depositStatus?: DepositStatus;
  sellerCompanyId?: string;
  createdAt: string;
  updatedAt: string;
}
```

### Правила

- `buyer` может регистрироваться сам.
- `seller_admin` не регистрируется сам — его создаёт `super_admin`.
- `seller_admin` обязательно связан с `sellerCompanyId`.
- `super_admin` видит всё.
- `seller_admin` видит только данные своей seller company.
- `blocked` user не должен иметь возможность делать offers/bids или управлять seller dashboard.

---

## 2. Seller company — отдельная сущность

Seller company — это не user и не аккаунт для входа.

Seller company — это компания-продавец / поставщик, которой принадлежат Select lots, offers и deals.

У одной seller company может быть несколько `seller_admin` users.

### Требуемая модель

```ts
type SellerCompanyStatus = "active" | "blocked";

interface SellerCompany {
  id: string;
  companyName: string;
  displayName: string;
  country: string;
  contactEmail: string;
  status: SellerCompanyStatus;
  createdAt: string;
  updatedAt: string;
}
```

### Нужные endpoints

```http
GET    /admin/seller-companies
POST   /admin/seller-companies
GET    /admin/seller-companies/{id}
PATCH  /admin/seller-companies/{id}
POST   /admin/seller-companies/{id}/block
POST   /admin/seller-companies/{id}/unblock
DELETE /admin/seller-companies/{id}
```

### Важное правило

Если seller company заблокирована:

- её published Select lots не должны показываться публично;
- её seller_admin users могут логиниться, но действия управления должны быть disabled/forbidden;
- super_admin всё равно должен видеть эту seller company и связанные данные.

---

## 3. User management для super-admin

Super-admin должен управлять пользователями.

### Нужные endpoints

```http
GET    /admin/users
POST   /admin/users
GET    /admin/users/{id}
PATCH  /admin/users/{id}
POST   /admin/users/{id}/block
POST   /admin/users/{id}/unblock
DELETE /admin/users/{id}
```

### Create user

Для создания `seller_admin` обязательно передавать `sellerCompanyId`.

```json
{
  "email": "manager@autohaus.de",
  "name": "Autohaus Manager",
  "role": "seller_admin",
  "sellerCompanyId": "seller-de",
  "status": "active"
}
```

### Self-protection

Super-admin не должен случайно удалить или заблокировать сам себя.

---

## 4. Лоты: dual lane и sale mode

Сейчас API `/lots` описывает обычные аукционные лоты. Procar требует более широкую модель.

### Ключевые понятия

```txt
lane:
- auctions
- select

saleMode:
- offer
- auction
```

`Auctions` — массовый каталог / будущие внешние интеграции.

`Select` — curated inventory от seller companies / партнёров.

### Требуемая модель Lot

```ts
type LotLane = "auctions" | "select";
type LotStatus = "draft" | "published" | "unpublished" | "sold" | "cancelled";
type SaleMode = "offer" | "auction";

type AuctionStatus =
  | "upcoming"
  | "live"
  | "ended"
  | "seller_decision"
  | "accepted"
  | "rejected";

interface Lot {
  id: string;
  lane: LotLane;
  sellerCompanyId?: string;

  title: string;
  description: string;
  status: LotStatus;
  saleMode: SaleMode;

  guidePrice?: number;

  vehicle: VehicleInfo;
  location: LocationInfo;
  images: LotImage[];
  documents?: LotDocument[];
  condition?: ConditionInfo;
  damage?: DamageInfo[];
  fees?: FeeInfo[];
  shipping?: ShippingInfo;
  sellerNotes?: string;

  auction?: AuctionInfo;

  createdAt: string;
  updatedAt: string;
}
```

### AuctionInfo

```ts
type AuctionResultStatus =
  | "none"
  | "pending_seller_decision"
  | "accepted"
  | "rejected";

interface AuctionInfo {
  startingBid: number;
  currentBid: number;
  bidIncrement: number;
  auctionStartsAt: string;
  auctionEndsAt: string;
  auctionStatus: AuctionStatus;
  auctionEndedAt?: string;
  sellerDecisionDeadline?: string;
  auctionResultStatus?: AuctionResultStatus;
  totalBids: number;
}
```

### Public lot endpoints

```http
GET /lots?lane=auctions|select
GET /lots/{id}
```

### Admin lot endpoints

```http
GET   /admin/lots
POST  /admin/lots
GET   /admin/lots/{id}
PATCH /admin/lots/{id}
POST  /admin/lots/{id}/publish
POST  /admin/lots/{id}/unpublish
POST  /admin/lots/{id}/end-auction
```

### Visibility rules

Public `/select` должен показывать только:

```txt
lane = select
status = published
sellerCompany.status = active
```

Seller-admin должен видеть только лоты своей seller company.

Super-admin должен видеть все лоты.

---

## 5. Offers — обязательная часть Procar

Сейчас в API нет offers, но во frontend это центральная часть сделки.

### Offer не равен Bid

```txt
Manual offer = покупатель сам предложил цену.
Auction winner offer = победная ставка после завершения аукциона.
```

### Требуемая модель

```ts
type OfferStatus = "submitted" | "accepted" | "rejected" | "cancelled";
type OfferSource = "manual_offer" | "auction_winner";

interface Offer {
  id: string;
  lotId: string;
  sellerCompanyId: string;
  buyerId: string;

  amount: number;
  message?: string;
  status: OfferStatus;
  offerSource: OfferSource;

  auctionId?: string;
  winningBidId?: string;
  sellerDecisionDeadline?: string;
  rejectionReason?: string;

  createdAt: string;
  updatedAt: string;
}
```

### Нужные endpoints

```http
POST /lots/{lotId}/offers
GET  /users/me/offers
GET  /admin/offers
POST /admin/offers/{id}/accept
POST /admin/offers/{id}/reject
```

### Rules

- Buyer видит только свои offers.
- Seller-admin видит offers только по своему `sellerCompanyId`.
- Super-admin видит все offers.
- `auction_winner` offer создаётся системой после `end-auction`.
- `auction_winner` offer должен иметь badge/тип `Auction winner`.
- Для rejection auction winner желательно требовать `rejectionReason`.

---

## 6. Bids — отдельно от offers

Ставки уже есть в API, но важно не смешивать bids и offers.

### Главное правило

```txt
Bid ≠ Offer
```

Пока аукцион live — покупатели делают bids.

Offer появляется только после завершения аукциона, когда highest bid становится `auction_winner` offer.

### Требуемая модель Bid

```ts
type BidStatus =
  | "winning"
  | "outbid"
  | "won_pending_seller"
  | "confirmed_deal"
  | "seller_rejected"
  | "lost";

interface Bid {
  id: string;
  lotId: string;
  bidderId: string;
  amount: number;
  status: BidStatus;
  isWinning: boolean;
  placedAt: string;
}
```

### Нужные endpoints

```http
POST /lots/{lotId}/bids
GET  /lots/{lotId}/bids
GET  /users/me/bids
POST /admin/lots/{lotId}/end-auction
```

### End auction logic

`POST /admin/lots/{lotId}/end-auction` должен:

1. Завершить auction.
2. Найти highest bid.
3. Пометить auction как `seller_decision`.
4. Создать `Offer`:

```json
{
  "offerSource": "auction_winner",
  "winningBidId": "...",
  "sellerDecisionDeadline": "..."
}
```

5. Пометить winning buyer bid как `won_pending_seller`.
6. Оставить outbid buyers в статусе `outbid` или `lost`.

---

## 7. Deals

Сейчас deals отсутствуют, но frontend уже построен вокруг deal flow.

### Требуемая модель

```ts
type DealStatus = "created" | "confirmed" | "paid" | "completed" | "cancelled";
type VehiclePaymentStatus = "pending" | "instructions_ready" | "paid";
type ProcarFeeStatus = "pending" | "paid";

interface Deal {
  id: string;
  lotId: string;
  sellerCompanyId: string;
  buyerId: string;
  offerId: string;
  amount: number;

  status: DealStatus;
  vehiclePaymentStatus: VehiclePaymentStatus;
  procarFeeStatus: ProcarFeeStatus;

  paymentInstructions?: PaymentInstructions;

  createdAt: string;
  updatedAt: string;
}
```

### PaymentInstructions

```ts
interface PaymentInstructions {
  type: "seller_bank_transfer" | "source_bank_transfer" | "other";
  amount: number;
  payeeDisplayName: string;
  internalPayeeName?: string;
  ibanOrBankDetails: string;
  invoiceUrl?: string;
  buyerNote?: string;
  createdAt: string;
}
```

### Нужные endpoints

```http
GET  /users/me/deals
GET  /admin/deals
POST /admin/deals/{id}/payment-instructions
POST /admin/deals/{id}/mark-vehicle-paid
POST /admin/deals/{id}/mark-procar-fee-paid
POST /admin/deals/{id}/complete
POST /admin/deals/{id}/cancel
```

### Rules

- Deal создаётся после accepted offer.
- Payment instructions видны buyer только после confirmed/created deal и после того, как seller/admin их добавил.
- Vehicle payment идёт продавцу/source.
- Procar fee — отдельный платёж Procar.
- Procar не должен обязательно принимать полную стоимость автомобиля на себя.

---

## 8. Buyer verification и deposit

Buyer flow во frontend:

```txt
register → company verification → deposit → offers/bids allowed
```

### Нужные endpoints

```http
POST /users/me/verification/start
POST /admin/users/{id}/verification/approve
POST /admin/users/{id}/verification/reject
POST /users/me/deposit/pay
GET  /users/me
```

### Access rule

Buyer может делать manual offers и bids только если:

```txt
user.status = active
companyStatus = verified
depositStatus = active
```

---

## 9. Auth

Текущая auth-модель с accessToken / refreshToken подходит, но `/users/me` должен возвращать полный Procar profile.

### `/users/me` должен возвращать

```json
{
  "id": "user-ready-buyer",
  "email": "ready@demo-motors.example",
  "name": "Ready Buyer",
  "role": "buyer",
  "status": "active",
  "companyName": "Demo Motors UAB",
  "country": "Lithuania",
  "companyStatus": "verified",
  "depositStatus": "active"
}
```

Для seller_admin:

```json
{
  "id": "seller-de-user",
  "email": "seller-de@procar.example",
  "name": "Seller DE Manager",
  "role": "seller_admin",
  "status": "active",
  "sellerCompanyId": "seller-de",
  "sellerCompany": {
    "id": "seller-de",
    "displayName": "Autohaus DE",
    "status": "active"
  }
}
```

---

## 10. Current API pieces that can stay

Из текущего Swagger можно сохранить как базу:

- auth token flow;
- `/lots`;
- `/lots/{lotId}`;
- `/lots/{lotId}/bids`;
- `/users/me/bids`;
- watchlist;
- catalog brand/model;
- часть vehicle/auction/location/document/fee schemas.

Но это нужно расширить и переименовать под Procar domain.

---

## 11. Critical missing API areas

Сейчас критически отсутствует:

```txt
seller companies
actions/permissions by role
manual offers
auction winner offers
deals
payment instructions
buyer verification/deposit
seller decision after auction
seller-owned Select lots
super-admin user management
```

Без этого frontend нельзя нормально подключить к backend без отката продуктовой логики.

---

## 12. Приоритет реализации backend

### Priority 1 — Auth + User profile

```txt
/auth/login
/auth/register
/auth/logout
/users/me
roles buyer/seller_admin/super_admin
companyStatus/depositStatus
sellerCompanyId
```

### Priority 2 — Seller companies + admin users

```txt
/admin/users
/admin/seller-companies
block/unblock
seller_admin → sellerCompanyId
```

### Priority 3 — Select lots

```txt
/admin/lots
/lots?lane=select
/lots/{id}
saleMode offer/auction
sellerCompanyId
published visibility rules
```

### Priority 4 — Offers + deals

```txt
/lots/{id}/offers
/users/me/offers
/admin/offers
/users/me/deals
/admin/deals
payment instructions
```

### Priority 5 — Auction lifecycle

```txt
/lots/{id}/bids
/users/me/bids
/admin/lots/{id}/end-auction
auction winner offer
seller decision
confirmed/rejected result
```

### Priority 6 — Payments / Stripe

```txt
deposit payment
Procar service fee
payment status webhooks
vehicle payment instructions
```

---

## 13. Короткий вывод

Текущий backend API — хорошая база для generic auctions, но он не соответствует текущему Procar frontend.

Нужно расширить его до Procar transaction-layer:

```txt
buyer / seller_admin / super_admin
seller companies
Select seller-owned lots
manual offers
auction bids
auction winner → seller decision offer
deals
payment instructions
verification/deposit
```

Frontend сейчас первичен по продуктовой логике. Backend нужно подтянуть под эту модель, а не упрощать frontend до текущего Swagger.
