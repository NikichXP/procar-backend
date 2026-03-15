Feature: Procar Auction Workflow
  As an auction administrator
  I want to manage the complete auction lifecycle
  So that vehicles can be successfully auctioned

  Background:
    Given I have access to the auction system
    And MongoDB is available for testing

  Scenario: Complete auction workflow
    Given I have admin access to create lots
    When I create a new lot with complete vehicle details
      | externalId        | title                           | description                                               | make   | model | year | vin               |
      | workflow-test-car | Integration Test Car - Workflow | A comprehensive test car for workflow integration testing | Toyota | Camry | 2023 | 1HGBH41JXMN109999 |
    Then the lot should be created successfully

    When I search for the lot using public API
    Then the lot should appear in search results

    When I retrieve the lot details
    Then I should see the complete vehicle information
    And the bid history should be empty

    When I place a bid of 16000.0 from bidder "bidder-1"
    Then the bid should be accepted

    When I place a bid of 16250.0 from bidder "bidder-2"
    Then the bid should be accepted

    When I check the bid history
    Then I should see 2 bids in chronological order

    When I remove all bids via admin endpoint
    Then all bids should be deleted

    When I delete the lot via admin endpoint
    Then the lot should be removed

    When I search for the deleted lot
    Then the lot should not appear in results
    And accessing the lot directly should return 404

  Scenario: Create lot with minimal data
    Given I have admin access to create lots
    When I create a minimal lot with required fields only
      | externalId       | title            | make  | model | year |
      | minimal-test-car | Minimal Test Car | Honda | Civic | 2022 |
    Then the lot should be created successfully
    And the lot should have default auction settings
