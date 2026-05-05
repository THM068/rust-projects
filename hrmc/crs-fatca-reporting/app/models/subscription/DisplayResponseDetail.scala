/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.subscription

import play.api.libs.json.{Json, OFormat}

case class DisplayResponseDetail(crfaSubscriptionDetails: CrfaSubscriptionDetails)

object DisplayResponseDetail:
  given format: OFormat[DisplayResponseDetail] = Json.format[DisplayResponseDetail]
