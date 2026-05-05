/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.mongo

object PendingStatus {
  val data: List[String] =
    List(
      """{
        "_id" : "796a6f16-579d-4ac6-bb19-bca83563e2c7",
 |  "subscriptionId" : "XACBC0000123778",
 |  "messageRefId" : "GBXACBC1234567",
 |  "reportingEntityName" : "Reporting Entity",
 |  "reportType" : "TEST_DATA",
 |"status" : {
 |        "Pending" : {}
 |    },
 |    "name" : "Submissionfile1.xml",
 |    "submitted" : ISODate("2022-02-23T13:11:19.079Z"),
 |    "lastUpdated" : ISODate("2022-02-23T13:11:19.079Z")
        |        }""".stripMargin
    )
}
