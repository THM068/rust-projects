/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.submission

import models.financialinstitutions.FIInfo
import models.XmlExtractedElements
import play.api.libs.json._
import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class MessageSpecData(messageType: MessageType,
                           sendingCompanyIN: String,
                           messageRefId: String,
                           reportingFIName: String,
                           reportingPeriod: LocalDate,
                           giin: Option[String] = None,
                           fiNameFromFim: String,
                           electionsRequired: Boolean
)

object MessageSpecData {
  implicit val localDateFormat: Format[LocalDate] = Format(
    Reads.localDateReads("yyyy-MM-dd"),
    Writes.temporalWrites[LocalDate, DateTimeFormatter](DateTimeFormatter.ISO_LOCAL_DATE)
  )
  implicit val format: OFormat[MessageSpecData] = Json.format[MessageSpecData]

  def from(xmlExtractedElements: XmlExtractedElements, fiInfo: FIInfo, requiresElection: Boolean): MessageSpecData = MessageSpecData(
    xmlExtractedElements.messageType,
    xmlExtractedElements.sendingCompanyIN,
    xmlExtractedElements.messageRefId,
    xmlExtractedElements.reportingFIName,
    xmlExtractedElements.reportingPeriod,
    fiInfo.giin,
    fiInfo.fiName,
    requiresElection
  )
}
