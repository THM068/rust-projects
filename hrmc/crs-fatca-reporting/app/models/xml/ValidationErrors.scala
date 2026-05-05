/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.xml

import play.api.libs.json.{Json, OFormat}

import scala.xml.NodeSeq

case class ValidationErrors(fileError: Option[Seq[FileErrors]], recordError: Option[Seq[RecordError]])

object ValidationErrors {

  given XmlReads[ValidationErrors] with
    def read(xml: NodeSeq): ValidationErrors =
      val fileError   = fromXml[Option[Seq[FileErrors]]](xml \# "FileError")
      val recordError = fromXml[Option[Seq[RecordError]]](xml \# "RecordError")
      ValidationErrors(fileError, recordError)

  implicit val format: OFormat[ValidationErrors] = Json.format[ValidationErrors]
}
