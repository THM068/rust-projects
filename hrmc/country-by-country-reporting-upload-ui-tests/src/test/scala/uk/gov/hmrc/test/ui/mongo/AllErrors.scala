/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.mongo

object AllErrors {
  val data =
    """{
      "Rejected" : {
      |            "error" : {
      |                "fileError" : [
      |                    {
      |                        "code" : "50009",
      |                        "details" : "Please replace the MessageRefID field value with a unique value (not containing all blanks), and resubmit the file"
      |                    }
      |                ],
      |                "recordError" : [
      |                    {
      |                        "code" : "80000",
      |                        "details" : "The DocRefId has already been used in this file or a file previously received, it must be unique",
      |                        "docRefIDInError" : [
      |                            "XCCBC0000000093-005-1-disclosingDocRefId"
      |                        ]
      |                    }
      |                ]
      |            }
      |        }
      |    },
      |        }""".stripMargin
}
