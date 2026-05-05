@tests @ZAP
Feature: Schema Errors File tests

  Background:
    Given  CBC User logs in to access file upload page
    Then The Heading should be Manage your country-by-country report
    And click Upload an XML file button
    Then The Heading should be Upload an XML file

  Scenario:1 Uploading file with schema errors - CBC_OECD Tag is missing
    When I browse and upload "SchemaErrorCBC_OECD1.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with the formatting of your file
    Then The Subheading should be We cannot accept the file SchemaErrorCBC_OECD1.xml because there is a problem with its formatting.

  Scenario:2 Uploading file with schema errors - CBC_OECD first name space is missing
    When I browse and upload "SchemaErrorCBC_OECD2.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with your file
    Then The Subheading should be We cannot accept the file SchemaErrorCBC_OECD2.xml because it does not meet the CBC data requirements.
    And The error table should show the following errors
      | line | error                                                                                     |
      | 2    | The CBC_OECD field must have an XML namespace (xmlns), which must be urn:oecd:ties:cbc:v2 |

  Scenario:3 Uploading file with schema errors - CBC_OECD whole name space is missing
    When I browse and upload "SchemaErrorCBC_OECD3.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with the formatting of your file
    Then The Subheading should be We cannot accept the file SchemaErrorCBC_OECD3.xml because there is a problem with its formatting.

  Scenario:4 Uploading file with schema errors - Message Spec Errors
    When I browse and upload "SchemaErrorCBC_MessageSpec.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with your file
    Then The Subheading should be We cannot accept the file SchemaErrorCBC_MessageSpec.xml because it does not meet the CBC data requirements.
    And The error table should show the following errors
      | line | error                                                                                   |
      | 5    | SendingEntityIN must be the CBC ID of the reporting entity                           |
      | 6    | Value is missing between TransmittingCountry tags                              |
      | 8    | ReceivingCountry value must be GB                                    |
      | 9    | MessageType value must be CBC                                  |
      | 11   | Language element must contain an ISO language code                    |
      | 13   | Value is missing between optional Warning tags                |
      | 16   | MessageRefId must be 100 characters or less                                    |
      | 17   | MessageTypeIndic is not one of the allowed values                                       |
      | 19   | CorrMessageRefId must not be provided                             |
      | 20   | 	MessageSpec ReportingPeriod must be in the format YYYY-MM-DD |
      | 22   | MessageSpec is missing one or more elements, including Timestamp                        |

  Scenario:5 Uploading file with schema errors - Some parent tags missing
    When I browse and upload "SchemaErrorCBC_ParentTagsMissing.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with your file
    Then The Subheading should be We cannot accept the file SchemaErrorCBC_ParentTagsMissing.xml because it does not meet the CBC data requirements.
    And The error table should show the following errors
      | line | error                                                                                       |
      | 5    | MessageSpec is missing                                                     |
      | 36   | Add an Address                                                                              |
      | 62   | ReportingPeriod is missing one or more elements, including StartDate |
      | 77   | DocSpec is missing                                                         |
      | 87   | Revenues is missing                                             |
      | 103  | ConstEntity is missing                                                      |
      | 148  | DocSpec is missing                                                    |

  Scenario:6 Uploading file with schema errors - some optional and mandatory tags validation errors
    When I browse and upload "SchemaErrorCBC_CbcBody.xml"
    And click Continue button
    And wait for 5 seconds
    Then The Heading should be There is a problem with your file
    Then The Subheading should be We cannot accept the file SchemaErrorCBC_CbcBody.xml because it does not meet the CBC data requirements.
    And The error table should show the following errors
      | line | error                                                                              |
      | 28   | ResCountryCode element must contain an ISO country code           |
      | 29   | 	TIN issuedBy must contain an ISO country code            |
      | 31   | 	Value is missing between optional IN tags                 |
      | 33   | Name must be 200 characters or less                                     |
      | 35   | Address legalAddressType is not one of the allowed values                          |
      | 36   | 	CountryCode element must contain an ISO country code             |
      | 39   | 	Street must be 200 characters or less                                 |
      | 41   | 	Value is missing between optional BuildingIdentifier tags |
      | 43   | SuiteIdentifier must be 200 characters or less                          |
      | 45   | 	Value is missing between optional FloorIdentifier tags    |
      | 47   | 	DistrictName must be 200 characters or less                              |
      | 49   | POB must be 200 characters or less                                      |
      | 51   | 	Value is missing between optional PostCode tags          |
      | 54   | 	City element is missing                                          |
      | 59   | NameMNEGroup must be 200 characters or less                             |
      | 60   | 	ReportingRole is not one of the allowed values                                     |
      | 62   | 	StartDate value is missing. This value must be in the format YYYY-MM-DD         |
      | 63   | EndDate must include a real date                                    |
      | 66   | 	DocTypeIndic is not one of the allowed values                                     |
      | 67   | DocRefId must be 164 characters or less                                |
      | 69   | CorrMessageRefId must not be provided  |
      | 71   | 	CorrDocRefId must be 164 characters or less                         |
      | 77   | DocTypeIndic is not one of the allowed values                                     |
      | 78   | DocRefId must be 164 characters or less                                |
      | 80   | 	CorrMessageRefId must not be provided  |
      | 82   | CorrDocRefId must be 164 characters or less                         |
      | 84   | ResCountryCode element must contain an ISO country code          |
      | 87   | 	Unrelated currCode must contain an ISO country code     |
      | 88   | 	Related must be a whole number                                                    |
      | 89   | 	Total must be a whole number                                                       |
      | 91   | 	Value is missing between ProfitOrLoss tags                               |
      | 92   | 	TaxPaid currCode must contain an ISO country code      |
      | 93   | Value is missing for TaxAccrued currCode. The value must be an ISO currency code            |
      | 94   | 	Capital must be a whole number                                                    |
      | 95   |	Value is missing between Earnings tags                                   |
      | 96   | 	NbEmployees must be a whole number                                                |
      | 98   | 	Summary is missing one or more elements, including Assets                         |
      | 103  | ResCountryCode element must contain an ISO country code           |
      | 106  | 	Value is missing between optional issuedBy tags          |
      | 108  | Name must be 200 characters or less                                     |
      | 110  |	Address legalAddressType is not one of the allowed values                          |
      | 111  | 	Value is missing between CountryCode tags                                |
      | 114  | 	Value is missing between optional Street tags             |
      | 116  | BuildingIdentifier must be 200 characters or less                     |
      | 118  | Value is missing between optional SuiteIdentifier tags    |
      | 120  | 	FloorIdentifier must be 200 characters or less                          |
      | 122  | 	Value is missing between optional DistrictName tags      |
      | 124  | 	POB must be 200 characters or less                                      |
      | 126  | Value is missing between optional PostCode tags           |
      | 129  | 	City element is missing                                          |
      | 136  | Role is not one of the allowed values                                              |
      | 138  | Value is missing between optional IncorpCountryCode tags  |
      | 140  | BizActivities is not one of the allowed values                                     |
      | 148  | DocTypeIndic is not one of the allowed values                                      |
      | 149  | DocRefId must be 164 characters or less                                 |
      | 151  | CorrMessageRefId must not be provided  |
      | 153  | CorrDocRefId must be 164 characters or less                            |
      | 156  | Value is missing between OtherInfo tags                                   |
      | 158  | ResCountryCode element must contain an ISO country code          |

