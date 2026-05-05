@tests
Feature:Agent Invalid File tests

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

  @ZAP
  Scenario:1 Uploading invalid xml
    When I browse and upload "rejected.UnexpectedContent.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with the formatting of your file
    Then The Subheading should be We cannot accept the file rejected.UnexpectedContent.xml because there is a problem with its formatting.
    Then The Page should include Kit Katze Ltd
    Then The Page should include XACBC0000123778
    And I click Upload a different file
    Then The Heading should be Upload an XML file

  @ZAP
  Scenario:3 Uploading invalid xml and clicking the tech guide for xml link
    When I browse and upload "rejected.UnexpectedContent.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with the formatting of your file
    Then The Subheading should be We cannot accept the file rejected.UnexpectedContent.xml because there is a problem with its formatting.
    And I click refer to the CBC technical guidance for XML files
#    Then the user should be on the new window with title "Cross-border tax arrangements schema and supporting documents" page

