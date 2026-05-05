@tests
Feature: Business Errors File tests

  Background:
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

  Scenario:1 Uploading file with business rule errors with one of each File, Record and Custom Error
    When I browse and upload "validCBCR.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be Check your file details are correct
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 1 seconds
    And set the file status to rejected with AllErrors
    And wait for 5 seconds
    Then The Heading should be There is a problem with your file
    Then The Page should include Kit Katze Ltd
    Then The Page should include XACBC0000123778
    And The Business rule errors table should show the following errors
      | code | docRefId                                 | errorMessage                                                                                                                                 |
      | 50009  | N/A     | MessageRefId must not be the same as one used for a previously sent file. We only keep a record of the MessageRefId for successfully sent files.                  |
      | 80000  | XCCBC0000000093-005-1-disclosingDocRefId  | DocRefId has already been used in this file or a file that was previously sent. We only keep a record of these IDs for successfully sent files.                                                                                                                           |
    Then click change element
    Then The Heading should be Manage your clients
