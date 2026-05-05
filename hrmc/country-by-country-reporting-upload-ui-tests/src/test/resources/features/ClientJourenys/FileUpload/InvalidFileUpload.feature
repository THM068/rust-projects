@tests
Feature:Invalid File tests

  Background:
    Given CBC User logs in to access file upload page
    Then The Heading should be Manage your country-by-country report
    And click Upload an XML file button
    Then The Heading should be Upload an XML file

  @ZAP
  Scenario:1 Uploading invalid xml
    When I browse and upload "rejected.UnexpectedContent.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with the formatting of your file
    Then The Subheading should be We cannot accept the file rejected.UnexpectedContent.xml because there is a problem with its formatting.
    And I click Upload a different file
    Then The Heading should be Upload an XML file

#  Scenario:2 Uploading >100MB xml (Commented this scenario as we are unable to add 100MB file in git)
#    When I browse and upload "TooLarge.xml"
#    And click Continue button
#    And wait for 5 seconds
#    Then The Heading should be There is a problem with the size of your file
#    Then The Subheading should be We cannot accept this file as it must be 100MB or less in size.
#    And I click Upload a different file
#    Then The Heading should be Upload an XML file

  @ZAP
  Scenario:3 Uploading invalid xml and clicking the tech guide for xml link
    When I browse and upload "rejected.UnexpectedContent.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with the formatting of your file
    Then The Subheading should be We cannot accept the file rejected.UnexpectedContent.xml because there is a problem with its formatting.
    And I click refer to the CBC technical guidance for XML files
#    Then the user should be on the new window with title "Cross-border tax arrangements schema and supporting documents" page

  @ZAP
  Scenario:4 Uploading virus xml
    When I browse and upload "Virus.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a virus in your file
    Then The Subheading should be We cannot accept this file as it contains a virus.
    And I click Upload a different file
    Then The Heading should be Upload an XML

  Scenario:5 Invalid file Type
    When I browse and upload "invalid.pdf"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with the file type
    Then The Subheading should be We cannot accept this file as it must be an XML file.
    And I click Upload a different file
    Then The Heading should be Upload an XML

