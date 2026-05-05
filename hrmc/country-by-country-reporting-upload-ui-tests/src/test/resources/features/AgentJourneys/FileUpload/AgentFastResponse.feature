@tests @ZAP
Feature: Fast response File tests

  Background: File status
    Given CBC Agent for fileUpload logs in to access file upload page
    Then The Heading should be Manage your clients
    When I click select a client to send a country-by-country report for
    Then The Heading should be What is the CBC ID of the client you want to act on behalf of
    And I enter XACBC0000123778 in value
    And click Continue button
    Then The Heading should be Is this your client?
    When I select value and continue
    Then The Heading should be Manage your country-by-country report
    And the mongo query to drop the submission-detail
    And click Upload an XML file button
    Then The Heading should be Upload an XML file
    When I browse and upload "validCBCR.xml"
    And click Continue button
    Then wait for 5 seconds
    Then The Heading should be Check your file details are correct
    Then  The Page should include This is first reporting entity name
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 1 seconds

  Scenario:1 Passed confirmation - Agent
    And set the file status to accepted with response
    Then wait for 5 seconds
    Then The Heading should be File successfully sent
    Then The Page should include GBXACBC0000123778-This is my message ref id
    And The Page should include AgentFirstContactEmail@cbc.com and AgentSecondContactEmail@cbc.com
    And The Page should include ClientFirstContactEmail@cbc.com and ClientSecondContactEmail@cbc.com
    And I click Back to manage your country-by-country report
    Then The Heading should be Manage your country-by-country report

  Scenario:2 Failed confirmation - Agent
    And set the file status to rejected with AllErrors
    Then wait for 5 seconds
    Then The Heading should be There is a problem with your file

  Scenario:3 Problem confirmation - Agent
    And set the file status to problem with SchemaErrors
    Then wait for 5 seconds
    Then The Heading should be Sorry, there is a problem with the service

  Scenario Outline:4 Fast File Upload with Stub: Failed and Passed
    Given CBC Agent for fileUpload logs in to access file upload page
    Then The Heading should be Manage your clients
    When I click select a client to send a country-by-country report for
    Then The Heading should be What is the CBC ID of the client you want to act on behalf of
    And I enter XACBC0000123778 in value
    And click Continue button
    Then The Heading should be Is this your client?
    When I select value and continue
    Then The Heading should be Manage your country-by-country report
    And the mongo query to drop the submission-detail
    And click Upload an XML file button
    Then The Heading should be Upload an XML file
    When I browse and upload <File>
    And click Continue button
    Then wait for 5 seconds
    Then The Heading should be Check your file details are correct
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 10 seconds
    Then The Heading should be <Heading>
    And I click Back to manage your country-by-country report
    Then The Heading should be Manage your country-by-country report
    Examples:
      | File | Heading |
      | "FastResponseRejected.xml" | There is a problem with your file |
      | "FastResponseAccepted.xml" | File successfully sent |