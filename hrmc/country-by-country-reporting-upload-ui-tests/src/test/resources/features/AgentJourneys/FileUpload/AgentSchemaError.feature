@tests @ZAP
Feature: Schema Errors File tests

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

  Scenario:1 Uploading file with schema errors - CBC_OECD Tag is missing
    When I browse and upload "SchemaErrorCBC_OECD1.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with the formatting of your file
    Then The Subheading should be We cannot accept the file SchemaErrorCBC_OECD1.xml because there is a problem with its formatting.

  Scenario:2 Uploading file with schema errors - CBC_OECD first name space is missing
    When I browse and upload "SchemaErrorCBC_OECD2.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with your file
    Then The Page should include Kit Katze Ltd
    Then The Page should include XACBC0000123778
    Then The Subheading should be We cannot accept the file SchemaErrorCBC_OECD2.xml because it does not meet the CBC data requirements.
    And The error table should show the following errors
      | line | error                                                                                     |
      | 2    | The CBC_OECD field must have an XML namespace (xmlns), which must be urn:oecd:ties:cbc:v2 |
    Then click change element
    Then The Heading should be Manage your clients