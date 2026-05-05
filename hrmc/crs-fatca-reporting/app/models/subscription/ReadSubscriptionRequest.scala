/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.subscription

import play.api.libs.json.{Json, OFormat}

case class ReadSubscriptionRequest(idNumber: String)

object ReadSubscriptionRequest:
  given format: OFormat[ReadSubscriptionRequest] = Json.format[ReadSubscriptionRequest]
