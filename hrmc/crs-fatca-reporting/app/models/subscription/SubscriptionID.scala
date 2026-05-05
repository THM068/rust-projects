/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.subscription

import play.api.libs.json.{Json, OFormat}

case class SubscriptionID(value: String)

object SubscriptionID:
  given format: OFormat[SubscriptionID] = Json.format[SubscriptionID]
