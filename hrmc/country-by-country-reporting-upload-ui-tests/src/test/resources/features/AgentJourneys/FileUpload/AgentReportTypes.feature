@tests
Feature: Agent Report Type Check

  Scenario Outline: Agent Report Type check Feature
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
    When I browse and upload <File Name>
    And click Continue button
    Then wait for 5 seconds
    Then The Heading should be Check your file details are correct
    Then The Page should include <Report Type>
    Then The Page should include Client (ReportingEntity Name)
    And click Continue button
    Then The Heading should be Send your file
    Then The Page should include <Warning>
    And click Send button
    Then wait for 1 seconds
    And set the file status to accepted with response
    Then wait for 5 seconds
    Then The Heading should be File successfully sent
    And The Page should include ClientFirstContactEmail@cbc.com and ClientSecondContactEmail@cbc.com
    Then The Page should include <Report Type>
    Then The Page should include Client (ReportingEntity Name)
  And I click Back to manage your country-by-country report
    Then The Heading should be Manage your country-by-country report
    Examples:
      | File Name | Report Type | Warning |
      | "New information for the reporting period 1.xml" | New information for the reporting period | complete to the best of your knowledge |
      | "Deletion of all previously reported information for this reporting period 1.xml"| 	Deletion of all previously reported information for this reporting period | This will permanently delete all information for the reporting period. |
      | "New information for an existing report 1.xml" | 	New information for an existing report | complete to the best of your knowledge |
      | "Corrections for an existing report 1.xml"   |	Corrections for an existing report      | This will permanently change your reported information. |
      | "Deletions for an existing report 1.xml" |	Deletions for an existing report            |This will permanently delete any previously reported information you have marked for deletion. |
      | "Corrections and deletions for an existing report 1.xml" | 	Corrections and deletions for an existing report | This will permanently change reported information marked as a correction. It will also permanently delete any previously reported information you have marked for deletion. |
      | "Correction for the ReportingEntity.xml" | Correction for the ReportingEntity | This will permanently change the ReportingEntity information. |

  Scenario: Agent Report Type check Feature for Test
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
    When I browse and upload "FastResponseRejected.xml"
    And click Continue button
    Then wait for 5 seconds
    Then The Heading should be Check your file details are correct
    Then The Page should include Test data
    Then The Page should include Client (ReportingEntity Name)
    And click Continue button
    Then The Heading should be Send your file
    Then The Page should include We cannot complete all checks on test data or accept the file.
    And click Send button
    Then wait for 7 seconds
    Then The Heading should be There is a problem with your file
    And I click Upload the file
    Then The Heading should be Upload an XML file
