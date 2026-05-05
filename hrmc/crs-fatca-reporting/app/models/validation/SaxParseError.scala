/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.validation

import play.api.libs.json.{Json, OFormat}

case class SaxParseError(lineNumber: Int, errorMessage: String)

object SaxParseError {
  implicit val format: OFormat[SaxParseError] = Json.format[SaxParseError]
}
