/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.upscan

import play.api.libs.json.{Json, OFormat}

case class ErrorDetails(failureReason: String, message: String)

object ErrorDetails {

  implicit val errorDetailsFormat: OFormat[ErrorDetails] =
    Json.format[ErrorDetails]
}
