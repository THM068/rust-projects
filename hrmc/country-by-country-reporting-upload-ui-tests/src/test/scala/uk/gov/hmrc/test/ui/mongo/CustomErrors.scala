/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.mongo

object CustomErrors {
  val data =
    """{
      "Rejected" : {
      |            "error" : {
      |                "recordError" : [
      |                 {
      |                        "code" : "CBC Error Code 1",
      |                        "details" : "Only one record (CbcBody) is allowed per XML submission."
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 2",
      |                        "details" : "The (MessageSpec) (SendingEntityIN) must match the relevant CbCid provided by HMRC."
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 3",
      |                        "details" : "The first part of all DocRefIds must match the MessageRefId of the file.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-3-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 4",
      |                        "details" : "MessageRefID must match filename.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 5",
      |                        "details" : "The ReportingPeriod element of the MessageRefId does not match the Year of the Reporting Period field in the XML.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 6",
      |                        "details" : "The CbC ID within the MessageRefId does not match the content of the SendingEntityIN field.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 7",
      |                        "details" : "The MessageTypeIndic within the MessageRefId does not match the content of the MessageTypeIndic field.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 8",
      |                        "details" : "The XML Creation TimeStamp within the MessageRefId does not match the content of the TimeStamp field.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 9",
      |                        "details" : "The TIN within the DocRefId does not match the content of the Reporting Entity TIN.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-9-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 10",
      |                        "details" : "The DocTypeIndic within the DocRefId does not match the content of the related (DocSpec) (DocTypeIndic).",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-10-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 11",
      |                        "details" : "The XML Parent Group Element within the DocRefId does not match the type of the XML Parent Group Element.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-11-disclosingDocRefId"
      |                        ]
      |                    }
      |                    {
      |                        "code" : "CBC Error Code 13",
      |                        "details" : "The MessageSpec ReportingPeriod must match Reporting Period EndDate under ReportingEntity.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-13-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 14",
      |                        "details" : "Multiple Initial (CBC401) files for the same MessageSpec ReportingPeriod are not allowable.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 15a",
      |                        "details" : "The Reporting Period Start / End Dates cannot overlap those from a previous submission.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-15a-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 15b",
      |                        "details" : "The Reporting Period Start / End Dates cannot overlap those from a previous submission.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-15b-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 16",
      |                        "details" : "The StartDate element must be earlier than the EndDate element.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-16-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 17",
      |                        "details" : "The earliest reporting period you can submit is 2016.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 18",
      |                        "details" : "A Reporting Period End Date cannot contain a future date.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-18-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 20",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC701, CBC703 or CBC704 and where Reporting Entity TIN issuedBy=\"GB\" the value of the element must be ten numeric characters.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-20-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 21a",
      |                        "details" : "There must be a ConstEntity TIN/IssuedBy combination that matches the Reporting Entity TIN/IssuedBy combination (except where Reporting Entity NOTIN has been used). ",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-21a-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 21b",
      |                        "details" : "There must be a ConstEntity TIN/IssuedBy combination that matches the Reporting Entity TIN/IssuedBy combination (except wher Reporting Entity NOTIN has been used). ",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-21b-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 22a",
      |                        "details" : "There must be no duplication of ConstEntity TIN/IssuedBy combination  within the CbCReports (excluding NOTIN).",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-22a-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 22b",
      |                        "details" : "There must be no duplication of ConstEntity TIN/IssuedBy combination  within the CbCReports (excluding NOTIN).",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-22b-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 23a",
      |                        "details" : "All amounts provided in the Country-by-Country Report should be reported in one and the same currency, being the currency of the Reporting MNE.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 23b",
      |                        "details" : "All amounts provided in the Country-by-Country Report should be reported in one and the same currency, being the currency of the Reporting MNE.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 25a",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC701 and a ConstEntity Role is present it must be CBC803 and it's TIN/IssuedBy combination must match that of the Reporting Entity.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-25a-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 25b",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC701 and a ConstEntity Role is present it must be CBC803 and it's TIN/IssuedBy combination must match that of the Reporting Entity.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-25b-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 26a",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC702 and a ConstEntity Role of CBC802 or CBC803 is present, it must match the TIN/IssuedBy combination of the Reporting Entity.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-26a-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 26b",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC702 and a ConstEntity Role of CBC802 or CBC803 is present, it must match the TIN/IssuedBy combination of the Reporting Entity.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-26b-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 27a",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC702 and a ConstEntity Role of CBC801 is present, it must not match the TIN/IssuedBy combination of the Reporting Entity.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-27a-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 27b",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC702 and a ConstEntity Role of CBC801 is present, it must not match the TIN/IssuedBy combination of the Reporting Entity.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-27b-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 28",
      |                        "details" : "The referenced file failed validation against the CbC XML Schema.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-28-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 29",
      |                        "details" : "If BizActivities is CBC513 the OtherEntityInfo element must be present within the same ConstEntities.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-29-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 30",
      |                        "details" : "A negative number of employees is not allowable.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-30-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 31",
      |                        "details" : "Your number of employees cannot exceed ten million.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-31-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 32",
      |                        "details" : "An Initial must contain at least one CbCReport.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 33",
      |                        "details" : "With OECD0, the CorrDocRef must not be present.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-33-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 34",
      |                        "details" : "If MessageTypeIndic is CBC402 and Reporting Entity DocTypeIndic is OECD0, the remaining submission can contain either new records (OECD1) or corrections (OECD2 and OECD3), but must not contain a mixture of both.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 35",
      |                        "details" : "If MessageTypeIndic is CBC401, DocTypeIndic can only be OECD1.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-35-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 36",
      |                        "details" : "The root element CBC_OECD version attribute must be present and must be set to the current schema version.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 37",
      |                        "details" : "If a TIN has been provided, the IssuedBy attribute must be completed, except where NOTIN is provided as a value.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-37-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 38",
      |                        "details" : "SendingEntityIN is a mandatory field.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 39a",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC703 or CBC704 only ConstEntity Role of CBC801 or CBC802 can be present (CBC803 is not allowed).",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-39a-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 39b",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC703 or CBC704 only ConstEntity Role of CBC801 or CBC802 can be present (CBC803 is not allowed).",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-39b-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 40a",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC703 or CBC704 and a ConstEntity Role of CBC802 is present, the TIN/IssuedBy combination must match that of the Reporting Entity.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-40a-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 40b",
      |                        "details" : "If the Reporting Entity ReportingRole is CBC703 or CBC704 and a ConstEntity Role of CBC802 is present, the TIN/IssuedBy combination must match that of the Reporting Entity.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-40b-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 42",
      |                        "details" : "The ReportingRole in the OECD0 is not the same as the one previously supplied.",
      |                    },
      |                    {
      |                        "code" : "CBC Error Code 43",
      |                        "details" : "The Reportingperiod in the OECD0 is not the same as the one previously supplied.",
      |                    }
      |
      |                ]
      |            }
      |        }
      |        }""".stripMargin
}
