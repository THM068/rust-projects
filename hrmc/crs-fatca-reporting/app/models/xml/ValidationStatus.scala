/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.xml

import play.api.libs.json.{JsString, Writes}

object ValidationStatus extends Enumeration {
  type ValidationStatus = Value

  val accepted: Value = Value("Accepted")

  val rejected: Value = Value("Rejected")

  given writes: Writes[ValidationStatus] = Writes[ValidationStatus] { v =>
    JsString(v.toString)
  }

}
