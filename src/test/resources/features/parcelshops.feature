Feature: ParcelShop finder and filters


# Verify ParcelShops finder search works via City
  Scenario: Search by city returns EH postcodes for Edinburgh
    Given I am on the evri.com homepage
    And I select the 'Find a ParcelShop' link from the 'ParcelShops' menu
    When I search for 'Edinburgh'
    Then I see only Parcelshops and Lockers with postcodes starting with 'EH' in the list


# Verify ParcelShops filter works with 'ParcelShops'
  Scenario: Filter to ParcelShops only in Edinburgh
    Given I am on the evri.com homepage
    And I select the 'Find a ParcelShop' link from the 'ParcelShops' menu
    When I search for 'Edinburgh'
    And I select 'ParcelShops' from the filters
    Then I see only Parcelshops with postcodes starting with 'EH' in the list


# Remaining filters
  Scenario: Packageless ParcelShops in Edinburgh
    Given I am on the evri.com homepage
    And I select the 'Find a ParcelShop' link from the 'ParcelShops' menu
    When I search for 'Edinburgh'
    And I select 'Packageless ParcelShops' from the filters
    Then I see only Parcelshops with postcodes starting with 'EH' in the list


  Scenario: Lockers in Birmingham
    Given I am on the evri.com homepage
    And I select the 'Find a ParcelShop' link from the 'ParcelShops' menu
    When I search for 'Birmingham'
    And I select 'Lockers' from the filters
    Then I see only Lockers with postcodes starting with 'B' in the list


  Scenario: Print labels in Edinburgh
    Given I am on the evri.com homepage
    And I select the 'Find a ParcelShop' link from the 'ParcelShops' menu
    When I search for 'Edinburgh'
    And I select 'Print labels' from the filters
    Then I see only Parcelshops with postcodes starting with 'EH' in the list


  Scenario: Next day drop off in Edinburgh
    Given I am on the evri.com homepage
    And I select the 'Find a ParcelShop' link from the 'ParcelShops' menu
    When I search for 'Edinburgh'
    And I select 'Next day drop off' from the filters
    Then I see only Parcelshops with postcodes starting with 'EH' in the list


  Scenario: Combination of filters in Manchester
    Given I am on the evri.com homepage
    And I select the 'Find a ParcelShop' link from the 'ParcelShops' menu
    When I search for 'Manchester'
    And I select 'ParcelShops' from the filters
    And I select 'Print labels' from the filters
    And I select 'Next day drop off' from the filters
    Then I see only Parcelshops with postcodes starting with 'M' in the list