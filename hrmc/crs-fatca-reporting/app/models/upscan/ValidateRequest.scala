/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.upscan

import play.api.libs.json.{Json, OFormat}

case class ValidateRequest(url: String, conversationId: String, subscriptionId: String, fileReferenceId: String)

object ValidateRequest {
  implicit val formats: OFormat[ValidateRequest] = Json.format[ValidateRequest]
}
