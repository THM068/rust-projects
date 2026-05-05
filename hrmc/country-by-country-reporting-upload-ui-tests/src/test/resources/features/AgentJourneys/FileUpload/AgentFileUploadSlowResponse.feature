@tests @ZAP
Feature: Agent Slow response File tests

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
    When I browse and upload "validCBCR.xml"
    And click Continue button
    Then wait for 5 seconds
    Then The Heading should be Check your file details are correct
    Then  The Page should include This is first reporting entity name
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 22 seconds
    Then The Heading should be We need a few minutes to check your file
    Then  The Page should include We will email you and your client if your file has passed the checks or you can sign in again later to check the results.
    Then wait for 1 seconds

  Scenario Outline: Slow - Passed and failed file -  CBC User
    And set the file status to <status> with <file>
    And I click Refresh for updates
    Then The Heading should be <results>
    And I click <view>
    Then The Heading should be <page>
   Then  The Page should include <unique content>
    And I click <link>
   Then The Heading should be <returnPage>
    Examples:
      | status   | file      | results                         | view              | page                              | link                                          | returnPage                            | unique content                                                                |
      | accepted | response  | Your file has passed our checks | Go to confirmation | File successfully sent          | Back to manage your country-by-country report | Manage your country-by-country report | ClientFirstContactEmail@cbc.com and ClientSecondContactEmail@cbc.com |
      | rejected | AllErrors | Your file has failed our checks | Check errors  | There is a problem with your file | Upload the file                      | Upload an XML file                    |   What you can do next |

  Scenario: Slow - Problem file -  CBC User
    And set the file status to problem with SchemaErrors
    And I click Refresh for updates
    Then The Heading should be Sorry, there is a problem with the service
    Then  The Page should include msb.countrybycountryreportingmailbox@hmrc.gov.uk