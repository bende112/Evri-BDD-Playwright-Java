Feature: Prohibited items validation


# Verify prohibited items cannot be sent
  Scenario: Enter prohibited content in 'What's inside your parcel'
    Given I am on the evri.com homepage
    And I fill in some send parcel details and progress to the "What's inside your parcel" screen
    When I enter 'gun' into the 'Parcel contents' field
    And I enter '100' into the 'How much is it worth' field
    Then I see an error message is displayed under the 'Parcel contents' field with text 'Parcel contents are prohibited
    And the 'Continue' button is disabled