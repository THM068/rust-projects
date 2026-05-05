/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.mongo

object ProblemStatusOrganisation {
  val data: List[String] =
    List(
      """{
        "_id" : "898a6f16-579d-4ac6-bb29-bca83563e2c9",
        |  "subscriptionId" : "XACBC0009234568",
        |  "messageRefId" : "GBXACBC1234567",
        |  "reportingEntityName" : "Reporting Entity",
        |  "reportType" : "TEST_DATA",
        |"status" : {
        |"Rejected" : {
        |            "error" : {
        |                "fileError" : [
        |                    {
        |                        "code" : "80001",
        |                        "details" : "Duplicate message ref ID"
        |                    }
        |                ],
        |                "recordError" : [
        |                    {
        |                        "code" : "80010",
        |                        "details" : "A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both",
        |                        "docRefIDInError" : [
        |                            "asjdhjjhjssjhdjshdAJGSJJS"
        |                        ]
        |                    }
        |                ]
        |            }
        |        }
        |    },
        |    "name" : "Submissionfile1.xml",
        |    "submitted" : ISODate("2022-02-23T13:11:19.079Z"),
        |    "lastUpdated" : ISODate("2022-02-23T13:11:19.079Z")
        |        }""".stripMargin
    )
}
