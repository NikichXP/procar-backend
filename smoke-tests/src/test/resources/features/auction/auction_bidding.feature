Feature: Auction Bidding Lifecycle
  As a bidder
  I want to place bids and see their status
  So that I can compete for the lot and know if I won

  Background:
    Given I am authenticated with credentials "admin" and "admin"

  Scenario: Bidding war and auction end
    # 1. Register two bidders
    When I request POST "/auth/register" with body:
      """
      {
        "username": "bidder1-${uuid}",
        "password": "password"
      }
      """
    Then the status code should be 200
    And I save the field "username" as "user1_name"

    When I request POST "/auth/register" with body:
      """
      {
        "username": "bidder2-${uuid}",
        "password": "password"
      }
      """
    Then the status code should be 200
    And I save the field "username" as "user2_name"

    # 2. Create an ACTIVE lot as admin
    When I request POST "/api/admin/lots" with body:
      """
      {
        "warehouseId": "019d35be-83e0-72ae-9d7b-297d54aca296",
        "externalId": "bid-test-${uuid}",
        "title": "Bidding Test Car",
        "description": "Bidding war test",
        "vehicle": {
          "make": "BMW",
          "model": "M3",
          "year": 2025,
          "bodyType": "SEDAN",
          "engine": { "type": "I6" },
          "transmission": "AUTOMATIC",
          "drivetrain": "AWD",
          "fuelType": "GASOLINE",
          "condition": "EXCELLENT"
        },
        "auction": {
          "currentBid": 5000,
          "startingBid": 5000,
          "bidIncrement": 500,
          "startTime": "2024-01-01T00:00:00",
          "endTime": "2030-01-01T00:00:00"
        },
        "metadata": { "tags": [], "categories": [] },
        "status": "ACTIVE",
        "lotType": "AUCTION",
        "brand": "PARTNER"
      }
      """
    Then the status code should be 200
    And I save the field "/data/id" as "lot_id"

    # 3. First bidder places a bid
    When I switch to user "${user1_name}" with password "password"
    And I request POST "/api/lots/${lot_id}/bids" with body:
      """
      { "amount": 6000 }
      """
    Then the status code should be 200
    And the field "/data/bid/status" is "WINNING"
    And the field "/data/bid/isWinning" is boolean "true"

    # 4. Second bidder outbids
    When I switch to user "${user2_name}" with password "password"
    And I request POST "/api/lots/${lot_id}/bids" with body:
      """
      { "amount": 7000 }
      """
    Then the status code should be 200
    And the field "/data/bid/status" is "WINNING"
    And the field "/data/bid/isWinning" is boolean "true"

    # 5. Check first bidder's history - should be OUTBID
    When I switch to user "${user1_name}" with password "password"
    And I request GET "/api/users/me/bids"
    Then the status code should be 200
    And the field "/data/content/0/bidStatus" is "OUTBID"
    And the field "/data/content/0/isWinning" is boolean "false"

    # 6. Admin ends the auction
    When I switch to user "admin" with password "admin"
    And I request POST "/api/admin/lots/${lot_id}/end-auction"
    Then the status code should be 200
    And the field "/data/status" is "AWAIT_SELLER_CONFIRMATION"

    # 7. Check final bid statuses
    When I switch to user "${user2_name}" with password "password"
    And I request GET "/api/users/me/bids"
    Then the status code should be 200
    And the field "/data/content/0/bidStatus" is "WON"
    And the field "/data/content/0/isWinning" is boolean "true"

    When I switch to user "${user1_name}" with password "password"
    And I request GET "/api/users/me/bids"
    Then the status code should be 200
    And the field "/data/content/0/bidStatus" is "LOST"
    And the field "/data/content/0/isWinning" is boolean "false"

    # Cleanup
    When I switch to user "admin" with password "admin"
    And I request DELETE "/api/admin/lots/${lot_id}"
    Then the status code should be 200

  Scenario: Instant Buyout
    # 1. Create a HYBRID lot as admin
    Given I am authenticated with credentials "admin" and "admin"
    When I request POST "/api/admin/lots" with body:
      """
      {
        "warehouseId": "019d35be-83e0-72ae-9d7b-297d54aca296",
        "externalId": "buyout-test-${uuid}",
        "title": "Buyout Test Car",
        "description": "Buyout test",
        "vehicle": {
          "make": "Audi",
          "model": "RS6",
          "year": 2025,
          "bodyType": "WAGON",
          "engine": { "type": "V8" },
          "transmission": "AUTOMATIC",
          "drivetrain": "AWD",
          "fuelType": "GASOLINE",
          "condition": "EXCELLENT"
        },
        "auction": {
          "currentBid": 10000,
          "startingBid": 10000,
          "bidIncrement": 1000,
          "startTime": "2024-01-01T00:00:00",
          "endTime": "2030-01-01T00:00:00"
        },
        "metadata": { "tags": [], "categories": [] },
        "status": "ACTIVE",
        "lotType": "HYBRID",
        "buyoutPrice": 15000,
        "brand": "PARTNER"
      }
      """
    Then the status code should be 200
    And I save the field "/data/id" as "buyout_lot_id"

    # 2. Register a buyer
    When I request POST "/auth/register" with body:
      """
      {
        "username": "buyer-${uuid}",
        "password": "password"
      }
      """
    Then the status code should be 200
    And I save the field "username" as "buyer_name"

    # 3. Buyer performs buyout
    When I switch to user "${buyer_name}" with password "password"
    And I request POST "/api/lots/${buyout_lot_id}/buyout"
    Then the status code should be 200
    And the field "/data/lotId" is "${buyout_lot_id}"

    # 4. Check lot status - should be AWAITING_PAYMENT
    When I request GET "/api/lots/${buyout_lot_id}"
    Then the status code should be 200
    And the field "/data/status" is "AWAITING_PAYMENT"

    # 5. Check bid status - should be WON
    When I request GET "/api/users/me/bids"
    Then the status code should be 200
    And the field "/data/content/0/bidStatus" is "WON"
    And the field "/data/content/0/isWinning" is boolean "true"

    # Cleanup
    When I switch to user "admin" with password "admin"
    And I request DELETE "/api/admin/lots/${buyout_lot_id}"
    Then the status code should be 200

  Scenario: Bidding after buyout fails
    # 1. Create a lot
    Given I am authenticated with credentials "admin" and "admin"
    When I request POST "/api/admin/lots" with body:
      """
      {
        "warehouseId": "019d35be-83e0-72ae-9d7b-297d54aca296",
        "externalId": "after-buyout-test-${uuid}",
        "title": "After Buyout Test Car",
        "description": "After buyout test",
        "vehicle": {
          "make": "Porsche",
          "model": "911",
          "year": 2025,
          "bodyType": "COUPE",
          "engine": { "type": "H6" },
          "transmission": "MANUAL",
          "drivetrain": "RWD",
          "fuelType": "GASOLINE",
          "condition": "EXCELLENT"
        },
        "auction": {
          "currentBid": 20000,
          "startingBid": 20000,
          "bidIncrement": 2000,
          "startTime": "2024-01-01T00:00:00",
          "endTime": "2030-01-01T00:00:00"
        },
        "metadata": { "tags": [], "categories": [] },
        "status": "ACTIVE",
        "lotType": "HYBRID",
        "buyoutPrice": 30000,
        "brand": "PARTNER"
      }
      """
    Then the status code should be 200
    And I save the field "/data/id" as "after_buyout_lot_id"

    # 2. Register users
    When I request POST "/auth/register" with body:
      """
      {
        "username": "buyer1-${uuid}",
        "password": "password"
      }
      """
    Then the status code should be 200
    And I save the field "username" as "user1"

    When I request POST "/auth/register" with body:
      """
      {
        "username": "buyer2-${uuid}",
        "password": "password"
      }
      """
    Then the status code should be 200
    And I save the field "username" as "user2"

    # 3. User 1 performs buyout
    When I switch to user "${user1}" with password "password"
    And I request POST "/api/lots/${after_buyout_lot_id}/buyout"
    Then the status code should be 200

    # 4. User 2 tries to place a higher bid - should fail
    When I switch to user "${user2}" with password "password"
    And I request POST "/api/lots/${after_buyout_lot_id}/bids" with body:
      """
      { "amount": 40000 }
      """
    Then the status code should be 200
    And the field "/data/status" is "REJECTED"
    And the field "/data/message" is "Lot is not active for bidding"

    # Cleanup
    When I switch to user "admin" with password "admin"
    And I request DELETE "/api/admin/lots/${after_buyout_lot_id}"
    Then the status code should be 200
