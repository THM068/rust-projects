/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.mongo

object CustomErrorsWithNewMessageOrNoErrorDetail {
  val data =
    """{
      "Rejected" : {
      |            "error" : {
      |                "recordError" : [
      |                   {
      |                        "code" : "99999",
      |                        "details" : "This is came with new error",
      |                        "docRefIDInError" : [
      |                            "this is with new error details"
      |                        ]
      |                    },
      |                    {
      |                        "code" : "99999",
      |                        "docRefIDInError" : [
      |                            "no details from backend"
      |                        ]
      |                    }
      |                ]
      |            }
      |        }
      |    },
      |        }""".stripMargin
}
