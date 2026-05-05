/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.upscan

import org.bson.types.ObjectId
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.{MongoFormats, MongoJavatimeFormats}

import java.time.Instant

case class UploadSessionDetails(
  _id: ObjectId,
  uploadId: UploadId,
  reference: Reference,
  status: UploadStatus,
  lastUpdated: Instant = Instant.now
)

object UploadSessionDetails {

  implicit val mongoDateTimeWrites: Format[Instant] = MongoJavatimeFormats.instantFormat

  implicit val objectIdFormats: Format[ObjectId] = MongoFormats.objectIdFormat

  val uploadedSuccessfullyFormat: OFormat[UploadedSuccessfully] =
    Json.format[UploadedSuccessfully]

  implicit val idFormat: OFormat[UploadId] = Json.format[UploadId]

  implicit val referenceFormat: OFormat[Reference] = Json.format[Reference]

  implicit val format: OFormat[UploadSessionDetails] =
    Json.format[UploadSessionDetails]

}
