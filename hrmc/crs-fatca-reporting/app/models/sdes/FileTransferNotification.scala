/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package models.sdes

import play.api.libs.json.{Json, OFormat}

final case class FileTransferNotification(
  informationType: String,
  file: File,
  audit: Audit
)

object FileTransferNotification {
  given OFormat[FileTransferNotification] = Json.format[FileTransferNotification]
}
