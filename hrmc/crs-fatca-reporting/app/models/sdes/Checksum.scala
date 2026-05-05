/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package models.sdes

import play.api.libs.json.{Json, OFormat}

final case class Checksum(algorithm: Algorithm, value: String)

object Checksum {
  given OFormat[Checksum] = Json.format[Checksum]
}
