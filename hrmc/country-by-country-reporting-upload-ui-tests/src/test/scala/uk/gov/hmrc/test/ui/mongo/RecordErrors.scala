/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.mongo

object RecordErrors {
  val data =
    """{
      "Rejected" : {
      |            "error" : {
      |                "recordError" : [
      |                    {
      |                        "code" : "80000",
      |                        "details" : "The DocRefId has already been used in this file or a file previously received, it must be unique",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-1-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "80002",
      |                        "details" : "The structure of the DocRefID is not in the correct format, as set out in the User Guide.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-2-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "80003",
      |                        "details" : "The corrected record is no longer valid (invalidated or outdated by a previous correction message). As a consequence, no further information should have been received on this version of the record.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-3-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "80004",
      |                        "details" : "The initial element specifies a CorrDocRefId.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-4-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "80005",
      |                        "details" : "The corrected element does not specify any CorrDocRefId",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-5-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "80006",
      |                        "details" : "The CorrMessageRefID is forbidden within the DocSpec_Type.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-6-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "80007",
      |                        "details" : "The CorrMessageRefID is forbidden within the Message Header.",
      |                    },
      |                    {
      |                        "code" : "80008",
      |                        "details" : "Resend option may only be used for the Disclosing element.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-8-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "80009",
      |                        "details" : "The Parent Record cannot be deleted without deleting all related child records (either in same message or in previous messages).",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-9-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "80010",
      |                        "details" : "A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but should not contain a mixture of both",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-10-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "80011",
      |                        "details" : "The same DocRefID cannot be corrected or deleted twice in the same message.",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-11-disclosingDocRefId"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "80013",
      |                        "details" : "An unknown DocRefID was specified for the Resend option (OECD0).",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-13-disclosingDocRefId"
      |                        ]
      |                    }, {
      |                        "code" : "80014",
      |                        "details" : "The Disclosing DocTypeIndic of OECD0 shows this section contains resent information, but the DocRefId is for information that has since been corrected or deleted. Provide the DocRefId of the section you want to correct",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-14-disclosingDocRefId"
      |                        ]
      |                    }
      |                ]
      |            }
      |        }
      |    },
      |        }""".stripMargin
}
