@tests @ZAP
Feature: Other tests

  Background:
    Given  CBC User logs in to access file upload page
    Then The Heading should be Manage your country-by-country report
    And click Upload an XML file button
    Then The Heading should be Upload an XML file

  Scenario:1 Get help with a technical problem
    And I click Is this page not working properly? (opens in new tab)
    Then the user should be on the new window with title "Get help with a technical problem" page

  Scenario:2 When user clicks on sign out they should be redirected to Feedback frontend
    And I click Sign out
    Then The Heading should be Give feedback

  Scenario:3 When user clicks on back link on page they should be redirected to the previous page
    And I click Back
    Then The Heading should be Manage your country-by-country report

  Scenario:4 User clicks on accessibility statement link
    And I click Accessibility statement
    Then The Heading should be Accessibility statement for Send a country-by-country report service