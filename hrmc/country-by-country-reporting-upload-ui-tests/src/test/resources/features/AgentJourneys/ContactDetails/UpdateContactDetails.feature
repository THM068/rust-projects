@tests @ZAP
Feature:Updating existing Agent Contact Details

  Scenario Outline:
    Given CBC Agent for fileUpload logs in to access file upload page
    Then The Heading should be Manage your clients
    Then I click change your country-by-country agent contact details
    Then The Heading should be Change your agent contact details
    Then The Page should include Back to manage your clients
    Then click contact-name element
    Then The Heading should be What is the name of the person or team we should contact?
    And I enter Primary Agent contact name updated in value
    And click Continue button
    Then The Heading should be What is the email address for Primary Agent contact name updated?
    And I enter AgentFirstContactEmailUpdated@cbc.com in value
    And click Continue button
    Then The Heading should be Can we contact Primary Agent contact name updated by telephone?
    When I select value and continue
    Then The Heading should be What is the telephone number for Primary Agent contact name updated?
    And I enter 01632 960 001 in value
    And click Continue button
    Then The Heading should be Check your agent contact details
    Then The Page should include Back to manage your clients
    Then The Page should include Primary Agent contact name updated
    Then The Page should include AgentFirstContactEmailUpdated@cbc.com
    Then The Page should include  01632 960 001
    And The Page should include Confirm and send
    # removing additional contact
    Then click second-contact element
    Then The Heading should be Is there someone else we can contact if Primary Agent contact name updated is not available?
    When I select value-no and continue
    Then The Heading should be Check your agent contact details
    Then The Page should include No
    When click Confirm and send
    Then The Heading should be Agent contact details updated
    Then I click <link>
    Then The Heading should be <page>
    Examples:
      | link                                           | page                                                           |
      | select a client                                | What is the CBC ID of the client you want to act on behalf of? |
      | add CBC clients in your agent services account | Agent contact details updated                                  |

  Scenario:
    Given CBC Agent for fileUpload logs in to access file upload page
    Then The Heading should be Manage your clients
    When I click select a client to send a country-by-country report for
    Then The Heading should be What is the CBC ID of the client you want to act on behalf of?
    When I enter XACBC0000123778 in value
    And click Continue button
    Then The Heading should be Is this your client?
    When I select value and continue
    Then The Heading should be Manage your country-by-country report
    When I click change your agent contact details
    Then The Heading should be Change your agent contact details
    Then The Page should include Back to manage your country-by-country report
    Then click contact-name element
    Then The Heading should be What is the name of the person or team we should contact?
    And I enter Primary Agent contact name updated in value
    And click Continue button
    Then The Heading should be What is the email address for Primary Agent contact name updated?
    And I enter AgentFirstContactEmailUpdated@cbc.com in value
    And click Continue button
    Then The Heading should be Can we contact Primary Agent contact name updated by telephone?
    When I select value and continue
    Then The Heading should be What is the telephone number for Primary Agent contact name updated?
    And I enter 01632 960 001 in value
    And click Continue button
    Then The Heading should be Check your agent contact details
    Then The Page should include Primary Agent contact name updated
    Then The Page should include AgentFirstContactEmailUpdated@cbc.com
    Then The Page should include  01632 960 001
    # removing additional contact
    Then click second-contact element
    Then The Heading should be Is there someone else we can contact if Primary Agent contact name updated is not available?
    When I select value-no and continue
    Then The Heading should be Check your agent contact details
    Then The Page should include No
    Then The Page should include Back to manage your country-by-country report
    When click Confirm and send
    Then The Heading should be Agent contact details updated
    Then I click Back to manage your country-by-country report
    Then The Heading should be Manage your country-by-country report

  Scenario Outline:
    Given CBC Agent for fileUpload logs in to access file upload page
    Then The Heading should be Manage your clients
    Then I click change your country-by-country agent contact details
    Then The Heading should be Change your agent contact details
    When click <Change link> element
    Then The URL should include <Url text>
    Then I click Back
    Then The Heading should be Change your agent contact details
    Examples:
    | Change link           | Url text                         |
    | contact-name          | change-first-contact-name        |
    | contact-email         | change-first-contact-email       |
    | contact-phone         | change-first-contact-have-phone  |
    | second-contact        | change-have-second-contact       |
    | snd-contact-name      | change-second-contact-name       |
    | snd-contact-email     | change-second-contact-email      |
    | snd-contact-phone     | change-second-contact-have-phone |
