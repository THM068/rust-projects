/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models

import models.submission.MessageType
import models.submission.MessageType.{CRS, FATCA}
import models.validation.{InvalidMessageTypeError, SubmissionValidationResult}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

class DataExtraction @Inject() ()(implicit ec: ExecutionContext) {
  val reportingPeriodDateformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  def parseMessageType(xml: Elem): Future[Either[SubmissionValidationResult, MessageType]] = Future {
    val mtOpt = (xml \\ "MessageType").headOption.map(_.text.trim)

    mtOpt match {
      case Some(v) if v.equalsIgnoreCase("CRS")   => Right(CRS)
      case Some(v) if v.equalsIgnoreCase("FATCA") => Right(FATCA)
      case _                                      => Left(InvalidMessageTypeError())
    }
  }

  def extractRequiredElements(xml: Elem): Option[XmlExtractedElements] =
    for {
      messageType         <- getMessageType(xml)
      sendingCompanyIN    <- (xml \\ "MessageSpec" \ "SendingCompanyIN").headOption.map(_.text.trim)
      messageRefId        <- (xml \\ "MessageSpec" \ "MessageRefId").headOption.map(_.text.trim)
      reportingPeriodText <- (xml \\ "MessageSpec" \ "ReportingPeriod").headOption.map(_.text.trim)
      reportingFIName     <- (xml \\ "ReportingFI" \ "Name").headOption.map(_.text.trim)
    } yield XmlExtractedElements(messageType,
                                 sendingCompanyIN,
                                 messageRefId,
                                 reportingFIName,
                                 LocalDate.parse(reportingPeriodText, reportingPeriodDateformatter)
    )

  private def getMessageType(xml: Elem): Option[MessageType] = {
    val mtOpt = (xml \\ "MessageSpec" \ "MessageType").headOption.map(_.text.trim)

    mtOpt match {
      case Some(v) if v.equalsIgnoreCase("CRS")   => Some(CRS)
      case Some(v) if v.equalsIgnoreCase("FATCA") => Some(FATCA)
      case _                                      => None
    }
  }
}

case class XmlExtractedElements(messageType: MessageType, sendingCompanyIN: String, messageRefId: String, reportingFIName: String, reportingPeriod: LocalDate)
