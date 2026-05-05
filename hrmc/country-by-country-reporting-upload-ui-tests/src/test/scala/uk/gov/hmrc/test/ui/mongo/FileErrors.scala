/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.mongo

object FileErrors {
  val data =
    """{
      "Rejected" : {
      |            "error" : {
      |                "fileError" : [
      |                    {
      |                        "code" : "50009",
      |                        "details" : "Please replace the MessageRefID field value with a unique value (not containing all blanks), and resubmit the file"
      |                    },
      |                    {
      |                        "code" : "50010",
      |                        "details" : "We cannot accept test data so each DocTypeIndic must have a value of either OECD0, OECD1, OECD2 or OECD3"
      |                        }
      |                ]
      |            }
      |        }
      |    },
      |        }""".stripMargin
}
