@tests @ZAP
Feature: Fast response File tests

  Scenario:1 Passed confirmation - Individual
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
    Then wait for 1 seconds
    And set the file status to accepted with response
    Then wait for 5 seconds
    Then The Heading should be File successfully sent
    Then The Page should include GBXACBC0000123778-This is my message ref id
    And The Page should include ClientSingleContactEmail@chocolate.com
    And I click Back to manage your country-by-country report
    Then The Heading should be Manage your country-by-country report

  Scenario:2 Passed confirmation - Organisation with 2 contacts
    Given Existing User with 2 contact logs in to access file upload page
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
    Then wait for 1 seconds
    And set the file status to accepted with response
    Then wait for 5 seconds
    Then The Heading should be File successfully sent
    Then The Page should include GBXACBC0000123778-This is my message ref id
    And The Page should include ClientFirstContactEmail@cbc.com and ClientSecondContactEmail@cbc.com
    And I click Back to manage your country-by-country report
    Then The Heading should be Manage your country-by-country report

  Scenario:3 Failed confirmation - Individual
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
    Then wait for 1 seconds
    And set the file status to rejected with AllErrors
    Then wait for 5 seconds
    Then The Heading should be There is a problem with your file

  Scenario:4 Problem confirmation - Individual
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
    Then wait for 1 seconds
    And set the file status to problem with SchemaErrors
    Then wait for 5 seconds
    Then The Heading should be Sorry, there is a problem with the service

  Scenario:5 Passed confirmation - Organisation with 2 contacts Browser Back Check
    Given Existing User with 2 contact logs in to access file upload page
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
    Then wait for 1 seconds
    And set the file status to accepted with response
    Then wait for 5 seconds
    Then The Heading should be File successfully sent
    Then The Page should include GBXACBC0000123778-This is my message ref id
    And The Page should include ClientFirstContactEmail@cbc.com and ClientSecondContactEmail@cbc.com
    When I Browser Back
    Then The Heading should be You have already sent this information
    And I click Back to manage your country-by-country report
    Then The Heading should be Manage your country-by-country report
