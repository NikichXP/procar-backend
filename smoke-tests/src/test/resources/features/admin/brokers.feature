Feature: Admin Broker Management
  As an admin
  I want to manage broker organizations
  So that they can list their cars on the platform

  Background:
    Given I am authenticated with credentials "admin" and "admin"

  Scenario: Create and manage a broker organization
    When I request POST "/api/admin/brokers" with body:
      """
      {
        "id": "test-broker-${uuid}",
        "companyName": "Test Broker Co",
        "displayName": "Test Broker",
        "contactEmail": "test@broker.com",
        "address": "123 Test St",
        "country": "Germany"
      }
      """
    Then the status code should be 200
    And the field "companyName" is "Test Broker Co"
    And the field "status" is "ACTIVE"
    And I save the field "id" as "broker_id"

    # Update broker status
    When I request POST "/api/admin/brokers/${broker_id}/status" with body:
      """
      {
        "status": "BLOCKED"
      }
      """
    Then the status code should be 200
    And the field "status" is "BLOCKED"

    # Patch broker
    When I request PATCH "/api/admin/brokers/${broker_id}" with body:
      """
      {
        "displayName": "Updated Test Broker",
        "country": "Poland"
      }
      """
    Then the status code should be 200
    And the field "displayName" is "Updated Test Broker"
    And the field "country" is "Poland"

    # Delete broker
    When I request DELETE "/api/admin/brokers/${broker_id}"
    Then the status code should be 204

    # Verify broker is gone
    When I request GET "/api/admin/brokers/${broker_id}"
    Then the status code should be 404
