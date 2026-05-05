/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.mongo

object ReceivedStatus {
  val data: List[String] =
    List(
      """{
        "_id" : "796a6f16-579d-4ac6-bb19-bca83563e2c8",
 |  "subscriptionId" : "XACBC0000123778",
 |  "messageRefId" : "GBXACBC0000123778-This is my message ref id",
 |  "reportingEntityName" : "Reporting Entity",
 |  "reportType" : "TEST_DATA",
 |"status" : {
 |        "Accepted" : {}
 |    },
 |    "name" : "Submissionfile1.xml",
 |    "submitted" : ISODate("2022-02-23T13:11:19.079Z"),
 |    "lastUpdated" : ISODate("2022-02-23T13:11:19.079Z")
        |        }""".stripMargin
    )
}
