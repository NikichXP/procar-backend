Feature: Admin Lot Management
  As an admin
  I want to manage car lots and their flow
  So that they can be published and sold correctly

  Background:
    Given I am authenticated with credentials "admin" and "admin"

  Scenario: Full lot lifecycle flow (Draft -> Pending -> Active -> Confirm -> Payment)
    # 1. Create a broker first
    When I request POST "/api/admin/brokers" with body:
      """
      {
        "id": "lot-test-broker-${uuid}",
        "companyName": "Lot Test Co",
        "displayName": "Lot Test Broker",
        "contactEmail": "lot@test.com",
        "address": "123 Test St",
        "country": "Germany"
      }
      """
    Then the status code should be 200
    And I save the field "id" as "broker_id"

    # 2. Create a lot in DRAFT
    When I request POST "/api/admin/lots" with body:
      """
      {
        "brokerId": "${broker_id}",
        "warehouseId": "019d35be-83e0-72ae-9d7b-297d54aca296",
        "externalId": "flow-test-${uuid}",
        "title": "Flow Test Car",
        "description": "Description",
        "vehicle": {
          "make": "Toyota",
          "model": "GR",
          "year": 2026,
          "bodyType": "COUPE",
          "engine": { "type": "V6" },
          "transmission": "AUTOMATIC",
          "drivetrain": "RWD",
          "fuelType": "GASOLINE",
          "condition": "EXCELLENT"
        },
        "auction": {
          "currentBid": 1000,
          "startingBid": 1000,
          "bidIncrement": 100,
          "startTime": "2026-01-01T00:00:00",
          "endTime": "2027-01-01T00:00:00"
        },
        "metadata": { "tags": [], "categories": [] },
        "status": "DRAFT",
        "lotType": "AUCTION",
        "brand": "SELECT"
      }
      """
    Then the status code should be 200
    And the field "/data/status" is "DRAFT"
    And the field "/data/brand" is "SELECT"
    And I save the field "/data/id" as "lot_id"

    # 3. Move to PENDING (Unpublished)
    When I request POST "/api/admin/lots/${lot_id}/status" with body:
      """
      { "status": "PENDING" }
      """
    Then the status code should be 200
    And the field "/data/status" is "PENDING"

    # 4. Explicitly Publish
    When I request POST "/api/admin/lots/${lot_id}/publish"
    Then the status code should be 200
    And the field "/data/status" is "ACTIVE"

    # 5. Explicitly Unpublish
    When I request POST "/api/admin/lots/${lot_id}/unpublish"
    Then the status code should be 200
    And the field "/data/status" is "PENDING"

    # 6. Back to Active for finishing
    When I request POST "/api/admin/lots/${lot_id}/publish"
    Then the status code should be 200

    # 7. Move to confirmation status (Simulating end of auction or manual move)
    When I request POST "/api/admin/lots/${lot_id}/status" with body:
      """
      { "status": "AWAIT_SELLER_CONFIRMATION" }
      """
    Then the status code should be 200
    And the field "/data/status" is "AWAIT_SELLER_CONFIRMATION"

    # 8. Confirm Availability
    When I request POST "/api/admin/lots/${lot_id}/confirm-availability"
    Then the status code should be 200
    And the field "/data/status" is "AWAITING_PAYMENT"

    # 9. Final cleanup
    When I request DELETE "/api/admin/lots/${lot_id}"
    Then the status code should be 200
    When I request DELETE "/api/admin/brokers/${broker_id}"
    Then the status code should be 204
