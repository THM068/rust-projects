@tests @ZAP
Feature: Agent other pages

  Background:Checking other links on CBCID invalid confirmation page
    Given CBC Agent for fileUpload logs in to access file upload page
    Then The Heading should be Manage your clients

#  Commenting this Test due to known issue raised in ticket DAC6-2965 
#  Scenario Outline: Agent unable provide correct client id
#    When I click select a client to send a country-by-country report for
#    Then The Heading should be What is the CBC ID of the client you want to act on behalf of
#    And I enter XACBC1000123778 in value
#    And click Continue button
#    When I click <link>
#    Then The Heading should be <page>
#
#    Examples:
#      | link                                    | page                                                          |
#      | Try entering your client’s CBC ID again | What is the CBC ID of the client you want to act on behalf of |

  Scenario Outline: Agent providing no client id or client id with wrong format
    When I click select a client to send a country-by-country report for
    Then The Heading should be What is the CBC ID of the client you want to act on behalf of
    And I enter <id> in value
    And click Continue button
    Then The Page should include <text>

    Examples:
    | id                          | text                                  |
    |                             | Enter your client’s CBC ID            |
    | !@£$%^&*()_+                | Your client’s CBC ID must start with an ‘X’ followed by a letter, then ‘CBC’ and then 10 numbers |

  Scenario Outline:Checking contact details links on Agent file upload landing page
    When I click select a client to send a country-by-country report for
    Then The Heading should be What is the CBC ID of the client you want to act on behalf of
    And I enter XACBC0000123778 in value
    And click Continue button
    Then The Heading should be Is this your client?
    When I select value and continue
    Then The Heading should be Manage your country-by-country report
    When I click <link>
    Then The Heading should be <page>
    Examples:
      | link                                 | page                                |
      | change your agent contact details    | Change your agent contact details    |
      | change this client’s contact details | Change your client’s contact details |

#This radio button should take to agent service page once we get an update, for time being we are staying on the same page
  Scenario:Checking agent service path
      Then The Heading should be Manage your clients
