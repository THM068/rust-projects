/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.upscan

import play.api.libs.json._

sealed trait UploadStatus
case object NotStarted extends UploadStatus
case object InProgress extends UploadStatus
case object Failed extends UploadStatus
case object Quarantined extends UploadStatus

case class UploadedSuccessfully(
  name: String,
  mimeType: String,
  downloadUrl: String,
  size: Option[Long],
  checksum: Option[String]
) extends UploadStatus

case class UploadRejected(details: ErrorDetails) extends UploadStatus

object UploadStatus {

  implicit val uploadedSuccessfullyFormat: OFormat[UploadedSuccessfully] =
    Json.format[UploadedSuccessfully]

  implicit val uploadRejectedFormat: OFormat[UploadRejected] =
    Json.format[UploadRejected]

  implicit val read: Reads[UploadStatus] = new Reads[UploadStatus] {

    override def reads(json: JsValue): JsResult[UploadStatus] = {
      val jsObject = json.asInstanceOf[JsObject]
      jsObject.value.get("_type") match {
        case Some(JsString("NotStarted"))  => JsSuccess(NotStarted)
        case Some(JsString("InProgress"))  => JsSuccess(InProgress)
        case Some(JsString("Failed"))      => JsSuccess(Failed)
        case Some(JsString("Quarantined")) => JsSuccess(Quarantined)
        case Some(JsString("UploadedSuccessfully")) =>
          Json.fromJson[UploadedSuccessfully](jsObject)(
            uploadedSuccessfullyFormat
          )
        case Some(JsString("UploadRejected")) =>
          Json.fromJson[UploadRejected](jsObject)(uploadRejectedFormat)
        case Some(value) => JsError(s"Unexpected value of _type: $value")
        case None        => JsError("Missing _type field")
      }
    }
  }

  implicit val write: Writes[UploadStatus] = new Writes[UploadStatus] {

    override def writes(p: UploadStatus): JsValue =
      p match {
        case NotStarted  => JsObject(Map("_type" -> JsString("NotStarted")))
        case InProgress  => JsObject(Map("_type" -> JsString("InProgress")))
        case Failed      => JsObject(Map("_type" -> JsString("Failed")))
        case Quarantined => JsObject(Map("_type" -> JsString("Quarantined")))
        case s: UploadRejected =>
          Json
            .toJson(s)(uploadRejectedFormat)
            .as[JsObject] + ("_type" -> JsString("UploadRejected"))
        case s: UploadedSuccessfully =>
          Json
            .toJson(s)(uploadedSuccessfullyFormat)
            .as[JsObject] + ("_type" -> JsString("UploadedSuccessfully"))
      }
  }
}
