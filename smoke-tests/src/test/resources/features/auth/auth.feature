Feature: Authentication
  As a user
  I want to login to the system
  So that I can access protected resources

  Scenario: Successful login with admin credentials
    When I request POST "/auth/login" with body:
      """
      {
        "username": "admin",
        "password": "admin"
      }
      """
    Then the status code should be 200
    And the field "/accessToken/token" is not empty
    And the field "refreshToken" is not empty

  Scenario: Failed login with incorrect credentials
    When I request POST "/auth/login" with body:
      """
      {
        "username": "admin",
        "password": "wrong_password"
      }
      """
    Then the status code should be 401

  Scenario: Successful login using the specialized step
    Given I am authenticated with credentials "admin" and "admin"
    And I request GET "/api/users/me"
    Then the status code should be 200
