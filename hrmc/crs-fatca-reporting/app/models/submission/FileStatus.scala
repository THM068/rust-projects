/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.submission

import models.xml.ValidationErrors
import play.api.libs.json.*

sealed trait FileStatus

case object Pending extends FileStatus
case object Accepted extends FileStatus
case object RejectedSDES extends FileStatus
case object RejectedSDESVirus extends FileStatus

case class Rejected(error: ValidationErrors) extends FileStatus {
  override def toString: String = "Rejected"
}

object FileStatus {

  val values: Seq[FileStatus] = Seq(
    Pending,
    Accepted,
    RejectedSDES,
    RejectedSDESVirus,
    Rejected(ValidationErrors(None, None))
  )

  implicit val format: Format[FileStatus] = Format(
    Reads {
      case JsString("Pending")           => JsSuccess(Pending)
      case JsString("Accepted")          => JsSuccess(Accepted)
      case JsString("RejectedSDES")      => JsSuccess(RejectedSDES)
      case JsString("RejectedSDESVirus") => JsSuccess(RejectedSDESVirus)

      case obj: JsObject if obj.keys == Set("Rejected") =>
        (obj("Rejected") \ "error")
          .validate[ValidationErrors]
          .map(Rejected.apply)

      case other => JsError(s"Invalid FileStatus JSON: $other")
    },
    Writes {
      case Pending           => JsString("Pending")
      case Accepted          => JsString("Accepted")
      case RejectedSDES      => JsString("RejectedSDES")
      case RejectedSDESVirus => JsString("RejectedSDESVirus")

      case Rejected(err) =>
        Json.obj("Rejected" -> Json.obj("error" -> Json.toJson(err)))
    }
  )
}
