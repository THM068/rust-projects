/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package models.submission

import models.upscan.UploadId
import play.api.libs.json.{Json, OFormat}

final case class SubmissionDetails(
  fileName: String,
  uploadId: UploadId,
  enrolmentId: String,
  fileSize: Long,
  documentUrl: String,
  checksum: String,
  messageSpecData: MessageSpecData
)

object SubmissionDetails {
  implicit val format: OFormat[SubmissionDetails] = Json.format[SubmissionDetails]
}
