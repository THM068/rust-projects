/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.subscription

import play.api.libs.json.{Json, OFormat}

case class DisplaySubscriptionResponse(success: DisplayResponseDetail)

object DisplaySubscriptionResponse:
  given format: OFormat[DisplaySubscriptionResponse] = Json.format[DisplaySubscriptionResponse]
