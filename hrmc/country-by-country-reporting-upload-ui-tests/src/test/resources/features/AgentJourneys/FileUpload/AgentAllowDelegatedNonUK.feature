@tests @ZAP
Feature: Agent allowing delegated Non-UK enrolment

  Scenario:1 Fast File Upload with Stub and delegated Non-UK enrolment: Passed
    Given CBC Agent for Non-UK fileUpload logs in to access file upload page
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
    When I browse and upload "FastResponseAccepted.xml"
    And click Continue button
    Then wait for 5 seconds
    Then The Heading should be Check your file details are correct
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 15 seconds
    Then The Heading should be File successfully sent
    And I click Back to manage your country-by-country report
    Then The Heading should be Manage your country-by-country report
