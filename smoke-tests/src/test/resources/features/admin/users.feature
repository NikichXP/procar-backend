Feature: Admin User Management
  As an admin
  I want to manage users and their roles/statuses
  So that I can control access to the platform

  Background:
    Given I am authenticated with credentials "admin" and "admin"

  Scenario: Create and delete a user (full lifecycle)
    When I request POST "/api/admin/users" with body:
      """
      {
        "username": "test_user_lifecycle",
        "role": "CUSTOMER"
      }
      """
    Then the status code should be 200
    And the field "id" is not empty
    And the field "username" is "test_user_lifecycle"
    And the field "role" is "CUSTOMER"
    And I save the field "id" as "id"

    # Update status
    When I request POST "/api/admin/users/${id}/status" with body:
      """
      {
        "status": "BLOCKED"
      }
      """
    Then the status code should be 200
    And the field "status" is "BLOCKED"

    # Patch user
    When I request PATCH "/api/admin/users/${id}" with body:
      """
      {
        "role": "BROKER",
        "companyName": "Test Broker Ltd"
      }
      """
    Then the status code should be 200
    And the field "role" is "BROKER"
    And the field "companyName" is "Test Broker Ltd"

    # Delete user
    When I request DELETE "/api/admin/users/${id}"
    Then the status code should be 204

    # Verify user is gone
    When I request GET "/api/admin/users/${id}"
    Then the status code should be 404
