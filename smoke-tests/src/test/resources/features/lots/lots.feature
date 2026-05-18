Feature: Lot Browsing
  As a visitor
  I want to browse and search for car lots
  So that I can find cars I'm interested in

  Background:

  Scenario: List lots with default filters
    When I request GET "/api/lots"
    Then the status code should be 200
    And the field "content" is a list
    And the field "totalElements" is a number

  Scenario: Get lot details
    # We first get a lot ID from the list
    When I request GET "/api/lots"
    And I save the first "content" ID as "firstLotId"
    When I request GET "/api/lots/${firstLotId}"
    Then the status code should be 200
    And the field "/data/id" is "${firstLotId}"
    And the field "/data/car/brandName" is not empty
    And the field "/data/car/modelName" is not empty
