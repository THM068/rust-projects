/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package models.subscription

import play.api.libs.json.{Json, OFormat}

case class CrfaSubscriptionDetails(crfaReference: String,
                                   tradingName: Option[String],
                                   gbUser: Boolean,
                                   primaryContact: ContactInformation,
                                   secondaryContact: Option[ContactInformation]
)

object CrfaSubscriptionDetails:
  given format: OFormat[CrfaSubscriptionDetails] = Json.format[CrfaSubscriptionDetails]
