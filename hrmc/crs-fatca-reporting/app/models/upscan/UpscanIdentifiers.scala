/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.upscan

import play.api.libs.json.{Json, OFormat}

case class UpscanIdentifiers(uploadId: UploadId, fileReference: Reference)

object UpscanIdentifiers {
  implicit val format: OFormat[UpscanIdentifiers] = Json.format[UpscanIdentifiers]
}
