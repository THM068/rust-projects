/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.xml

import play.api.libs.json.{Json, OWrites}

import scala.xml.NodeSeq

case class BREResponse(regime: String, conversationID: String, genericStatusMessage: GenericStatusMessage)

object BREResponse {

  given XmlReads[BREResponse] with
    def read(xml: NodeSeq): BREResponse =
      val regime               = (xml \# "requestCommon" \# "regime").text
      val conversationID       = (xml \# "requestCommon" \# "conversationID").text
      val genericStatusMessage = fromXml[GenericStatusMessage](xml \# "requestDetail" \# "GenericStatusMessage")
      BREResponse(regime, conversationID, genericStatusMessage)

  given writes: OWrites[BREResponse] = Json.writes[BREResponse]
}
