/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package models.sdes

import play.api.libs.json.{Json, OFormat}

final case class File(
  recipientOrSender: Option[String],
  name: String,
  location: Option[String],
  checksum: Checksum,
  size: Int,
  properties: List[Property]
)

object File {
  given OFormat[File] = Json.format[File]
}
