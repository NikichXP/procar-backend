Feature: Health Check
  As an administrator
  I want to verify that the system is running
  So that I can ensure the application is healthy

  Scenario: Gateway health check
    Given the backend is up on "https://api.pc-dev.nikichxp.xyz/"
    When I request GET "/actuator/health"
    Then the status code should be 200
    And the field "status" is "UP"
