Feature: Admin User Management
  As an admin
  I want to manage users and their roles/statuses
  So that I can control access to the platform

  Background:
    Given I am authenticated with credentials "admin" and "admin"

  Scenario: Create and delete a user (full lifecycle)
    When I request POST "/admin/users" with body:
      """
      {
        "username": "test_user_lifecycle",
        "roles": ["CUSTOMER"]
      }
      """
    Then the status code should be 200
    And the field "id" is not empty
    And the field "username" is "test_user_lifecycle"
    And the field "roles/0" is "CUSTOMER"
    And I save the field "id" as "id"

    # Update status
    When I request POST "/admin/users/${id}/status" with body:
      """
      {
        "status": "BLOCKED"
      }
      """
    Then the status code should be 200
    And the field "status" is "BLOCKED"

    # Patch user
    When I request PATCH "/admin/users/${id}" with body:
      """
      {
        "roles": ["BROKER"],
        "companyName": "Test Broker Ltd"
      }
      """
    Then the status code should be 200
    And the field "roles/0" is "BROKER"
    And the field "companyName" is "Test Broker Ltd"

    # Delete user
    When I request DELETE "/admin/users/${id}"
    Then the status code should be 204

    # Verify user is gone
    When I request GET "/admin/users/${id}"
    Then the status code should be 404

  Scenario: Admin self-protection (prevent self-block and self-delete)
    When I request GET "/users/me"
    Then the status code should be 200
    And I save the field "id" as "admin_id"

    # Try to block self via status endpoint
    When I request POST "/admin/users/${admin_id}/status" with body:
      """
      {
        "status": "BLOCKED"
      }
      """
    Then the status code should be 400

    # Try to block self via patch endpoint
    When I request PATCH "/admin/users/${admin_id}" with body:
      """
      {
        "status": "BLOCKED"
      }
      """
    Then the status code should be 400

    # Try to delete self
    When I request DELETE "/admin/users/${admin_id}"
    Then the status code should be 400
