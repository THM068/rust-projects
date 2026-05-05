/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package models.sdes

import play.api.libs.json.*

final case class Property(name: String, value: String)

object Property {
  given OFormat[Property] = Json.format[Property]
}
