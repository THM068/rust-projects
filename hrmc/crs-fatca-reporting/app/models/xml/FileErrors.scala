/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.xml

import play.api.libs.json.{Json, OFormat}

import scala.xml.NodeSeq

case class FileErrors(code: FileErrorCode, details: Option[String])

object FileErrors {

  given XmlReads[FileErrors] with
    def read(xml: NodeSeq): FileErrors =
      val code        = fromXml[FileErrorCode](xml \# "Code")
      val detailsText = xml \# "Details"
      val details     = Option.when(detailsText.nonEmpty)(detailsText.text.trim)
      FileErrors(code, details)

  implicit val format: OFormat[FileErrors] = Json.format[FileErrors]
}
