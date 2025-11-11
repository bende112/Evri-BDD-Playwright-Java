Feature: Send widget


# Verify 'Parcel size and weight' link on Send Widget
  Scenario: Verify 'Parcel size and weight' link on Send Widget
    Given I am on the evri.com homepage
    When I select the 'Parcel size and weight' link from the Send widget
    Then the heading changes to 'Evri parcel size and weight guide'
    And the three information panels 'Evri parcel weight limits', 'Postable parcel & large letter delivery' and 'Sending large parcels' are displayed below.
