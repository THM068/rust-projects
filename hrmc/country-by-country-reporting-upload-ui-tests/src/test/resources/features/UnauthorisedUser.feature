@tests @ZAP
Feature: Unauthorised user login
  
  Scenario:1 - Individual user login
    Given Individual User logs in to access file upload page
    Then The Heading should be You’re unable to use this service
    And I click sign in with your organisation’s Government Gateway details
    Then The Heading should be Authority Wizard

  Scenario:2 - Agent user trying to access with no agent enrolment
    Given Org User with agent affinity logs in to access file upload page
    Then The Heading should be You must use agent services to send a country-by-country report
    And I click create an agent services account to send country-by-country reports.
    And The Heading should be Create an agent services account