@tests
Feature: Business Errors File tests

  Background:
    Given  CBC User logs in to access file upload page
    Then The Heading should be Manage your country-by-country report
    And the mongo query to drop the submission-detail
    And click Upload an XML file button
    Then The Heading should be Upload an XML file
  Scenario:1 Uploading file with business rule errors with File Errors
    When I browse and upload "validCBCR.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be Check your file details are correct
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 1 seconds
    And set the file status to rejected with FileErrors
    And wait for 5 seconds
    Then The Heading should be There is a problem with your file
    And The Business rule errors table should show the following errors
      | code | docRefId | errorMessage                                                                                             |
      | 50009  | N/A     | MessageRefId must not be the same as one used for a previously sent file. We only keep a record of the MessageRefId for successfully sent files.                  |
      | 50010  | N/A     | DocTypeIndic contains a value that indicates the file contains test data, like OECD10, OECD11, OECD12 or OECD13. Replace the test data value with a value for real data, such as OECD0, OECD1, OECD2 or OECD3. |

  Scenario:2 Uploading file with business rule errors with Record Errors
    When I browse and upload "validCBCR.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be Check your file details are correct
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 1 seconds
    And set the file status to rejected with RecordErrors
    And wait for 5 seconds
    Then The Heading should be There is a problem with your file
    And The Business rule errors table should show the following errors
      | code | docRefId                                  | errorMessage                                                                                                                                                                                                               |
      | 80000  | XCCBC0000000093-005-1-disclosingDocRefId  | DocRefId has already been used in this file or a file that was previously sent. We only keep a record of these IDs for successfully sent files.                                                                                                                           |
      | 80002  | XCCBC0000000093-005-2-disclosingDocRefId  | The CorrDocRefId provided does not match any DocRefId in our records.                                                                                                                                                           |
      | 80003  | XCCBC0000000093-005-3-disclosingDocRefId  | The CorrDocRefId provided is for information that has already been corrected or deleted.                                                                                                                                              |
      | 80004  | XCCBC0000000093-005-4-disclosingDocRefId  | CorrDocRefId must not be included in a DocSpec where the DocTypeIndic is OECD1 for new information.                                                                                                                                              |
      | 80005  | XCCBC0000000093-005-5-disclosingDocRefId  | DocTypeIndic is OECD2 or OECD3 for a correction or deletion so you must provide a CorrDocRefId.                                                                                                                                           |
      |80006   | XCCBC0000000093-005-6-disclosingDocRefId  | DocSpec must not contain a CorrMessageRefId.                                                                                                                                                                                                                                          |
      |80007   |N/A   |   MessageSpec must not contain a CorrMessageRefId.                                                                                                                                                                                                                                                                                    |
      | 80008  | XCCBC0000000093-005-8-disclosingDocRefId  | DocTypeIndic of OECD0 for resent information has been used in the CbcReports or AdditionalInfo section. OECD0 can only be used in the ReportingEntity section.                                                   |
      | 80009  | XCCBC0000000093-005-9-disclosingDocRefId  | ReportingEntity has a DocTypeIndic of OECD3 for deletion. You can only delete the ReportingEntity if you also delete all of the CbcReports and AdditionalInfo sections for this reporting period.                                                                                                                          |
      | 80011  | XCCBC0000000093-005-11-disclosingDocRefId | The same CorrDocRefId cannot be used more than once in each file.                                                                                                                                                     |
      | 80013  | XCCBC0000000093-005-13-disclosingDocRefId | ReportingEntity contains a DocTypeIndic of OECD0 to resend information sent in a previous file but we do not have a record of the DocRefId provided.                                                                              |
      | 80014  | XCCBC0000000093-005-14-disclosingDocRefId | ReportingEntity contains a DocTypeIndic of OECD0 to resend information sent in a previous file but the DocRefId provided has already been corrected or deleted. |

  Scenario:3 Uploading file with business rule errors with Custom Errors
    When I browse and upload "validCBCR.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be Check your file details are correct
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 1 seconds
    And set the file status to rejected with CustomErrors
    And wait for 5 seconds
    Then The Heading should be There is a problem with your file
    And The Business rule errors table should show the following errors
      | code | docRefId                                     | errorMessage                                                                                                                                                        |
      |  1  | N/A     | Each file must contain only one CbcBody.                        |
      |  2  | N/A     | SendingEntityIN within the MessageSpec must contain the CBC ID of the entity making the report.                                                             |
      |  3  | XCCBC0000000093-005-3-disclosingDocRefId     | The first part of each DocRefId must match the MessageRefId.                                                           |
      |  4  | N/A      |  MessageRefId must match the file name, excluding the .xml file extension. Update the MessageRefId and file name to match and send the updated file.                                                                                   |
      |  5  | N/A      |  The ReportingPeriod year as part of the MessageRefId does not match the year in the MessageSpec ReportingPeriod.                                                        |
      |  6  | N/A     |    The CBC ID within the MessageRefId must match the CBC ID in the MessageSpec SendingEntityIN.                                                                                  |
      |  7  | N/A      |    The MessageTypeIndic within the MessageRefId must match the MessageTypeIndic in the MessageSpec.                                                         |
      |  8  | N/A     |      The timestamp within the MessageRefId must match the MessageSpec Timestamp.                                                                                |
      |  9  | XCCBC0000000093-005-9-disclosingDocRefId     |     The TIN within the DocRefId must match the TIN within the ReportingEntity.                                                         |
      |  10  | XCCBC0000000093-005-10-disclosingDocRefId    | The fourth part of the DocRefId must match the DocTypeIndic within the same section’s DocSpec.                                                                                     |
      |  11  | XCCBC0000000093-005-11-disclosingDocRefId     |       The fifth part of the DocRefId must be ‘ENT’ for the ReportingEntity section, ‘REP’ for the CbcReports section or ‘ADD’ for the AdditionalInfo section.                                                    |
      |  13  | XCCBC0000000093-005-13-disclosingDocRefId     |  ReportingPeriod within the MessageSpec must match the ReportingPeriod EndDate within the ReportingEntity.                                                           |
      |  14  | N/A   |   	The ReportingEntity DocTypeIndic value must be OECD0 if you are sending another CBC401 file for this reporting period.                                                                                 |
      |  15a  | XCCBC0000000093-005-15a-disclosingDocRefId     |    The ReportingPeriod StartDate and EndDate within ReportingEntity cannot overlap the dates from another report. Change your StartDate or EndDate and upload the new file.                                                         |
      |  15b  | XCCBC0000000093-005-15b-disclosingDocRefId    |      The ReportingPeriod StartDate and EndDate within ReportingEntity cannot overlap the dates from another report. Change your StartDate or EndDate and upload the new file.                                                                                |
      |  16  | XCCBC0000000093-005-16-disclosingDocRefId     |      For the ReportingEntity ReportingPeriod, StartDate must be earlier than EndDate.                                                       |
      |  17  | N/A    |  ReportingPeriod within the MessageSpec must be 2016-01-01 or later.                                                                                    |
      |  18  | XCCBC0000000093-005-18-disclosingDocRefId     |    MessageSpec ReportingPeriod cannot contain a future date.                                                         |
      |  20  | XCCBC0000000093-005-20-disclosingDocRefId    | If the ReportingEntity TIN issuedBy is GB, the TIN must be a Unique Taxpayer Reference containing 10 numbers.                                                                                     |
      |  21a  | XCCBC0000000093-005-21a-disclosingDocRefId     |    One of the ConstEntity TIN values in the file must match the ReportingEntity TIN, with the same issuedBy for both elements. For organisations without a TIN, use ‘NOTIN’ for the TIN value.                                                        |
      |  21b  | XCCBC0000000093-005-21b-disclosingDocRefId    |  One of the ConstEntity TIN values in the report must match the ReportingEntity TIN, with the same issuedBy for both elements. For organisations without a TIN, use ‘NOTIN’ for the TIN value.                                                                                    |
      |  22a  | XCCBC0000000093-005-22a-disclosingDocRefId     |    Each ConstEntity TIN with the same issuedBy country must be unique, unless the TIN value is ‘NOTIN’.                                                        |
      |  22b  | XCCBC0000000093-005-22b-disclosingDocRefId    |    Each ConstEntity TIN with the same issuedBy country must be unique, unless the TIN value is ‘NOTIN’.                                                                                  |
      |  23a  | N/A     |       currCode value must be the same for all amounts in the report. The currCode should be the currency of the reporting multinational entity.                                                       |
      |  23b  | N/A    |     currCode value must be the same for all amounts in the file and match the currCode used throughout this reporting period.                                                                                 |
      |  25a  | XCCBC0000000093-005-25a-disclosingDocRefId     | If the ReportingEntity ReportingRole is CBC701 and one of the ConstEntities has a Role, the Role value must be CBC803. That organisation’s TIN and issuedBy must match those of the ReportingEntity.                                                          |
      |  25b  | XCCBC0000000093-005-25b-disclosingDocRefId    |  If the ReportingEntity ReportingRole is CBC701 and one of the ConstEntities has a Role, the Role value must be CBC803. That organisation’s TIN and issuedBy must match those of the ReportingEntity. This applies to all sent files for this reporting period.                                                                                  |
      |  26a  | XCCBC0000000093-005-26a-disclosingDocRefId     |   If the ReportingEntity ReportingRole is CBC702 and one of the ConstEntities has a Role value of CBC802 or CBC803, the TIN and issuedBy of the ConstEntity must match those of the ReportingEntity.                                                        |
      |  26b  | XCCBC0000000093-005-26b-disclosingDocRefId    |    If the ReportingEntity ReportingRole is CBC702 and one of the ConstEntities has a Role value of CBC802 or CBC803, the TIN and issuedBy of the ConstEntity must match those of the ReportingEntity. This applies to all sent files for this reporting period.                                                                                 |
      |  27a  | XCCBC0000000093-005-27a-disclosingDocRefId     |   If the ReportingEntity ReportingRole is CBC702 and one of the ConstEntities has a Role value of CBC801, the TIN and issuedBy of the ConstEntity must not match those of the ReportingEntity.                                                          |
      |  27b  | XCCBC0000000093-005-27b-disclosingDocRefId    |        If the ReportingEntity ReportingRole is CBC702 and one of the ConstEntities has a Role value of CBC801, the TIN and issuedBy of the ConstEntity must not match those of the ReportingEntity. This applies to all sent files for this reporting period.                                                                              |
      |  28  | XCCBC0000000093-005-28-disclosingDocRefId     | One or more of the required elements in the file contains only whitespace.                                                            |
      |  29  | XCCBC0000000093-005-29-disclosingDocRefId    |  As BizActivities is CBC513 for one or more ConstEntities, provide OtherEntityInfo for those ConstEntities.                                                                                    |
      |  30  | XCCBC0000000093-005-30-disclosingDocRefId     |    NbEmployees cannot be less than 0.                                                         |
      |  31  | XCCBC0000000093-005-31-disclosingDocRefId    |      NbEmployees must be 10,000,000 or less.                                                                                |
      |  32  | N/A     | The first file for the reporting period must contain one or more CbcReports elements. Any other CBC401 files must contain at least one CbcReport or AdditionalInfo element.                                                |
      |  33  | XCCBC0000000093-005-33-disclosingDocRefId    |       If the ReportingEntity DocTypeIndic is OECD0, the DocSpec should not contain a CorrDocRefId.                                                                               |
      |34    |    N/A                                          | As the MessageTypeIndic is CBC402 for corrections or deletions, the DocTypeIndic for the ReportingEntity can be OECD0 for resent information, OECD2 for corrections or OECD3 for deletions. All other DocTypeIndic values must be OECD2, OECD3 or a combination of OECD2 and OECD3.|
      |  35  | XCCBC0000000093-005-35-disclosingDocRefId    | If the MessageTypeIndic is CBC401, the DocTypeIndic for the ReportingEntity must be OECD0 or OECD1. All other DocTypeIndic values outside of the ReportingEntity must be OECD1.                      |
      |  37  | XCCBC0000000093-005-37-disclosingDocRefId    |    A TIN must have an issuedBy attribute, unless the TIN value is ‘NOTIN’.                                                                                 |
      |  38  | N/A    |     SendingEntityIN is required and must contain the CBC ID of the entity making the report.                                                        |
      |  39a  | XCCBC0000000093-005-39a-disclosingDocRefId    |     If the ReportingEntity ReportingRole is CBC703 or CBC704, the ConstEntities Role values cannot be CBC803.                                                                                 |
      |  39b  | XCCBC0000000093-005-39b-disclosingDocRefId     |  If the ReportingEntity ReportingRole is CBC703 or CBC704, the ConstEntities Role values cannot be CBC803. This applies to all sent files for this reporting period.                                                           |
      |  40a  | XCCBC0000000093-005-40a-disclosingDocRefId    |   If the ReportingEntity ReportingRole is CBC703 or CBC704 and one of the ConstEntities has a Role value of CBC802, the TIN and issuedBy of the ConstEntity must match those of the ReportingEntity.                                                                                   |
      |  40b  | XCCBC0000000093-005-40b-disclosingDocRefId     |   If the ReportingEntity ReportingRole is CBC703 or CBC704 and one of the ConstEntities has a Role value of CBC802, the TIN and issuedBy of the ConstEntity must match those of the ReportingEntity. This applies to all sent files for this reporting period.                                                          |
      |  42  | N/A   |  ReportingRole within the ReportingEntity does not match the previously sent value for this reporting period. To change the ReportingRole, use a ReportingEntity DocTypeIndic of OECD2 instead of OECD0.                                                                                    |
      |  43  | N/A     |    ReportingPeriod within the ReportingEntity does not match the previously sent value for this reporting period. To change the ReportingPeriod, use a ReportingEntity DocTypeIndic of OECD2 instead of OECD0.                                                         |


  Scenario:4 Uploading file with business rule errors with one of each File, Record and Custom Error
    When I browse and upload "validCBCR.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be Check your file details are correct
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 1 seconds
    And set the file status to rejected with AllErrors
    And wait for 5 seconds
    Then The Heading should be There is a problem with your file
    And The Business rule errors table should show the following errors
      | code | docRefId                                 | errorMessage                                                                                                                                 |
      | 50009  | N/A     | MessageRefId must not be the same as one used for a previously sent file. We only keep a record of the MessageRefId for successfully sent files.                  |
      | 80000  | XCCBC0000000093-005-1-disclosingDocRefId  | DocRefId has already been used in this file or a file that was previously sent. We only keep a record of these IDs for successfully sent files.                                                                                                                           |

  Scenario:5 Business rule Custom error code with new error message or no error details should go to Sorry, there is a problem with the service Page
    When I browse and upload "validCBCR.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be Check your file details are correct
    And click Continue button
    Then The Heading should be Send your file
    And click Send button
    Then wait for 1 seconds
    And set the file status to rejected with CustomErrorsWithNewMessageOrNoErrorDetail
    And wait for 5 seconds
    Then The Heading should be Sorry, there is a problem with the service
