@tests @ZAP
Feature: Slow response File tests

  Scenario Outline: Slow - Passed and failed file -  CBC User
    Given CBC User logs in to access file upload page
    Then The Heading should be Manage your country-by-country report
    And the mongo query to drop the submission-detail
    And click Upload an XML file button
    Then The Heading should be Upload an XML file
    When I browse and upload "validCBCR.xml"
    And click Continue button
    Then wait for 5 seconds
    Then The Heading should be Check your file details are correct
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 20 seconds
    Then The Heading should be We need a few minutes to check your file
    And set the file status to <status> with <file>
    And I click Refresh for updates
    Then The Heading should be <results>
    And I click <view>
    Then The Heading should be <page>
    And I click <link>
    Then The Heading should be <returnPage>
    Examples:
      | status   | file      | results                         | view              | page                              | link                                          | returnPage                            |
      | accepted | response  | Your file has passed our checks | Go to confirmation | File successfully sent          | Back to manage your country-by-country report | Manage your country-by-country report |
      | rejected | AllErrors | Your file has failed our checks | Check errors  | There is a problem with your file | Upload the file                      | Upload an XML file                    |

  Scenario: Slow - Problem file -  CBC User
    Given  CBC User logs in to access file upload page
    Then The Heading should be Manage your country-by-country report
    And the mongo query to drop the submission-detail
    And click Upload an XML file button
    Then The Heading should be Upload an XML file
    When I browse and upload "validCBCR.xml"
    And click Continue button
    Then wait for 5 seconds
    Then The Heading should be Check your file details are correct
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 20 seconds
    Then The Heading should be We need a few minutes to check your file
    And set the file status to problem with SchemaErrors
    And I click Refresh for updates
    Then The Heading should be Sorry, there is a problem with the service
    Then  The Page should include msb.countrybycountryreportingmailbox@hmrc.gov.uk

  Scenario Outline: Slow File Upload with Stub: Failed and Passed
    Given Existing User with 2 contact logs in to access file upload page
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
    Then wait for 20 seconds
    Then The Heading should be We need a few minutes to check your file
    Then wait for 2 seconds
    Then I click Refresh for updates
    Then The Heading should be <Check>
    Then I click <View>
    Then The Heading should be <Heading>
    And I click Back to manage your country-by-country report
    Then The Heading should be Manage your country-by-country report
    Examples:
      | File | Check | View | Heading |
      | "SlowResponseRejected.xml" | Your file has failed our checks | Check errors | There is a problem with your file |
      | "SlowResponseAccepted.xml" | Your file has passed our checks | Go to confirmation | File successfully sent|
