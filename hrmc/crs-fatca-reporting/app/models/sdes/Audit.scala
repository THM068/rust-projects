/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package models.sdes

import play.api.libs.json.{Json, OFormat}

final case class Audit(correlationID: String)

object Audit {
  given OFormat[Audit] = Json.format[Audit]
}
