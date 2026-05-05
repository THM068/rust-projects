@tests @ZAP
Feature:Status page
@solo
  Scenario Outline:All status check
    Given the mongo query to drop the submission-detail
    And the mongo query is run to insert collections for Submission-file with All Status
    Given Existing User with 2 contact logs in to access file upload page
    Then The Heading should be Manage your country-by-country report
    And I click view results of checks on your recently sent files
    Then The Heading should be Result of automatic checks
    Then  The Page should include Pending
    Then  The Page should include Passed
    Then  The Page should include Failed
    Then  The Page should include Problem
    And I click <Next Steps>
    Then The Heading should be <page>
    Examples:
      | Next Steps        | page                                       |
#      | Go to confirmation | File successfully sent                   |
      | Check errors       | There is a problem with your file          |
      | Contact us        | Sorry, there is a problem with the service |
