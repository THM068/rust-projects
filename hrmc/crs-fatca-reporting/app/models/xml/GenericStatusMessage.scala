/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.xml

import play.api.libs.json.{Json, OWrites}

import scala.xml.NodeSeq

case class GenericStatusMessage(validationErrors: ValidationErrors, status: ValidationStatus.Value)

object GenericStatusMessage {
  given XmlReads[GenericStatusMessage] with
    def read(xml: NodeSeq): GenericStatusMessage =
      val validationErrors    = fromXml[ValidationErrors](xml \# "ValidationErrors")
      val validationResultTxt = (xml \# "ValidationResult" \ "Status").text
      val validationResult    = ValidationStatus.withName(validationResultTxt)
      GenericStatusMessage(validationErrors, validationResult)

  given writes: OWrites[GenericStatusMessage] = Json.writes[GenericStatusMessage]
}
