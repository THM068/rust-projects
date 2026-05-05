/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.mongo

object ProblemStatusErrors {
  val data =
    """{
      "Rejected" : {
      |            "error" : {
      |                "fileError" : [
      |                    {
      |                        "code" : "50007",
      |                        "details" : "Duplicate message ref ID"
      |                    }
      |                ],
      |                "recordError" : [
      |                    {
      |                        "code" : "99999",
      |                        "details" : "A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both",
      |                        "docRefIDInError" : [
      |                            "Problem status error message"
      |                        ]
      |                    }
      |                ]
      |            }
      |        }
      |    },
      |        }""".stripMargin
}
