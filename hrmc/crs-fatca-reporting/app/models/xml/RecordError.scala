/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.xml

import play.api.libs.json.{Json, OFormat}

import scala.xml.NodeSeq

case class RecordError(code: RecordErrorCode, details: Option[String], docRefIDInError: Option[Seq[String]])

object RecordError {

  given XmlReads[RecordError] with
    def read(xml: NodeSeq): RecordError =
      val code            = fromXml[RecordErrorCode](xml \# "Code")
      val detailsText     = xml \# "Details"
      val details         = Option.when(detailsText.nonEmpty)(detailsText.text.trim)
      val docRefIDs       = (xml \# "DocRefIDInError").map(_.text.trim).filter(_.nonEmpty)
      val docRefIDInError = if docRefIDs.nonEmpty then Some(docRefIDs) else None
      RecordError(code, details, docRefIDInError)

  given format: OFormat[RecordError] = Json.format[RecordError]
}
