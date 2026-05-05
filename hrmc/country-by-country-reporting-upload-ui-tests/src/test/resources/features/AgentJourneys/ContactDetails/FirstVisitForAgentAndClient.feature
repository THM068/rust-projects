@tests @ZAP
Feature: First time access to the service for agent and client
  Background:
    Given CBC New Agent with New Client logs in to access file upload page
    Then The Heading should be We need your agent contact details
    And click Continue button
    #Agent first contact
    Then The Heading should be What is the name of the person or team we should contact?
    And I enter First ContactName in value
    And click Continue button
    Then The Heading should be What is the email address for First ContactName?
    And I enter AgentFirstContactEmail@cbc.com in value
    And click Continue button
    Then The Heading should be Can we contact First ContactName by telephone?
    When I select value and continue
    Then The Heading should be What is the telephone number for First ContactName?
    And I enter 011 11111111 in value
    And click Continue button
    #Agent second contact
    Then The Heading should be Is there someone else we can contact if First ContactName is not available?
    When I select value and continue
    Then The Heading should be What is the name of the person or team we should contact?
    And I enter Second ContactName in value
    And click Continue button
    Then The Heading should be What is the email address for Second ContactName?
    And I enter AgentSecondContactEmail@cbc.com in value
    And click Continue button
    Then The Heading should be Can we contact Second ContactName by telephone?
    When I select value and continue
    Then The Heading should be What is the telephone number for Second ContactName?
    And I enter 022 22222222 in value
    And click Continue button
    Then The Heading should be Check your agent contact details
    When click Confirm and send
    Then The Heading should be Agent contact details saved
    #Selecting Client
    Then I click select a client
    And I enter XACBC0000123777 in value
    And click Continue button
    Then The Heading should be Is this your client?
    When I select value and continue

  Scenario: Agent and client details - Answers No to /review-client-contact-details navigates to /first-contact-name
    Then The Heading should be We need the latest contact details for your client
    And click Continue button
    Then The Heading should be Are these the right contact details for your client?
    And I select value-no and continue
    #Client first contact
    Then The Heading should be What is the name of the first contact at your client’s organisation?
    And I enter First ContactName in value
    And click Continue button
    Then The Heading should be What is the email address for First ContactName?
    And I enter FirstContactEmail@gmail.com in value
    And click Continue button
    Then The Heading should be Can we contact First ContactName by telephone?
    When I select value and continue
    Then The Heading should be What is the telephone number for First ContactName?
    And I enter 011 11111111 in value
    And click Continue button
    #Client second contact
    Then The Heading should be Is there someone else we can contact if First ContactName is not available?
    When I select value and continue
    Then The Heading should be What is the name of the second contact at your client’s organisation?
    And I enter Second ContactName in value
    And click Continue button
    Then The Heading should be What is the email address for Second ContactName?
    And I enter SecondContactEmail@gmail.com in value
    And click Continue button
    Then The Heading should be Can we contact Second ContactName by telephone?
    When I select value and continue
    Then The Heading should be What is the telephone number for Second ContactName?
    And I enter 022 22222222 in value
    And click Continue button
    Then The Heading should be Check your client’s contact details
    When click Confirm and send
    Then The Heading should be Client contact details saved
    Then I click send a CBC report
    Then The Heading should be Manage your country-by-country report

  Scenario: Agent and client details - Answers Yes to /review-client-contact-details navigates to /have-second-contact
    Then The Heading should be We need the latest contact details for your client
    And click Continue button
    Then The Heading should be Are these the right contact details for your client?
    And I select value and continue
    #Client second contact
    Then The Heading should be Is there someone else we can contact if Kit Katze Ltd is not available?
    When I select value and continue
    Then The Heading should be What is the name of the second contact at your client’s organisation?
    And I enter Second ContactName in value
    And click Continue button
    Then The Heading should be What is the email address for Second ContactName?
    And I enter SecondContactEmail@gmail.com in value
    And click Continue button
    Then The Heading should be Can we contact Second ContactName by telephone?
    When I select value and continue
    Then The Heading should be What is the telephone number for Second ContactName?
    And I enter 022 22222222 in value
    And click Continue button
    Then The Heading should be Check your client’s contact details
    When click Confirm and send
    Then The Heading should be Client contact details saved
    Then I click send a CBC report
    Then The Heading should be Manage your country-by-country report

  Scenario: Agent and client details - Validate client details
    Then The Heading should be We need the latest contact details for your client
    And click Continue button
    Then The Heading should be Are these the right contact details for your client?
    And The Page should include Kit Katze Ltd
    And The Page should include test@test.com



