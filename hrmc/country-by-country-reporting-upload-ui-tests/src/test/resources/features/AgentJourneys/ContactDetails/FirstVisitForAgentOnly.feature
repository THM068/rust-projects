@tests @ZAP
Feature:First visit for agent but client already Updated
  Scenario:Agent details
    Given CBC New Agent with Updated Client logs in to access file upload page
    Then The Heading should be We need your agent contact details
    And click Continue button
    #Agent first contact details
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
     #Agent second contact details
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
    And I enter XACBC0000123778 in value
    And click Continue button
    Then The Heading should be Is this your client?
    When I select value and continue
    Then The Heading should be Manage your country-by-country report