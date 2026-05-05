/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.update

import play.api.libs.json.{Json, OFormat}

case class GiinUpdateRequest(
  subscriptionId: String,
  fiid: String,
  giin: String
)

object GiinUpdateRequest:
  given OFormat[GiinUpdateRequest] = Json.format[GiinUpdateRequest]
