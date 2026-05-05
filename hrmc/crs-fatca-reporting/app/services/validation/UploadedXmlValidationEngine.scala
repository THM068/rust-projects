/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services.validation

import cats.data.EitherT
import cats.implicits._
import config.AppConfig
import models.financialinstitutions.FIInfo
import models.Constants.EUROPE_LONDON_TIME_ZONE
import models.elections.CheckElectionRequiredRequest
import models.submission.{MessageSpecData, MessageType}
import models.validation._
import models.{DataExtraction, XmlExtractedElements, XmlSchemaPathSelector}
import play.api.Logging
import services.{ElectionService, FinancialInstitutionsService}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq}

class UploadedXmlValidationEngine @Inject() (xmlValidationService: XMLValidationService,
                                             dataExtraction: DataExtraction,
                                             xmlSchemaPathSelector: XmlSchemaPathSelector,
                                             financialInstitutionsService: FinancialInstitutionsService,
                                             appConfig: AppConfig,
                                             checkElectionRequiredService: ElectionService
)(implicit ec: ExecutionContext)
    extends Logging {

  def validateUploadSubmission(upScanUrl: String, subscriptionId: String)(implicit headerCarrier: HeaderCarrier): Future[SubmissionValidationResult] = (for {
    xml         <- EitherT(xmlValidationService.loadXml(upScanUrl))
    messageType <- EitherT(dataExtraction.parseMessageType(xml))
    xmlSchemaPath = xmlSchemaPathSelector.selectSchema(messageType)
    extractedElements <- EitherT(performXmlValidation(xml, xmlSchemaPath, messageType))
    _                 <- EitherT(checkReportingPeriodIsValid(extractedElements))
    fiInfo            <- EitherT(checkFIIDMatchesSendCompanyInAndReturnGiinIfPresent(subscriptionId, extractedElements.sendingCompanyIN))
    requiresElection <- EitherT(
      checkElectionRequiredService.checkElectionRequired(
        CheckElectionRequiredRequest.of(messageType, extractedElements.sendingCompanyIN, extractedElements.reportingPeriod.getYear)
      )
    )
    messageSpecData = MessageSpecData.from(extractedElements, fiInfo, requiresElection)
  } yield SubmissionValidationSuccess(messageSpecData)).merge

  private def performXmlValidation(xmlNode: Elem,
                                   xmlSchemaPath: String,
                                   messageType: MessageType
  ): Future[Either[SubmissionValidationResult, XmlExtractedElements]] =
    Future {
      val nodeSeq: NodeSeq                               = Seq(xmlNode)
      val xmlOrErrors: Either[List[SaxParseError], Elem] = xmlValidationService.validate(nodeSeq, xmlSchemaPath)

      xmlOrErrors match {
        case Right(xml) =>
          dataExtraction.extractRequiredElements(xml) match {
            case Some(msd) => Right(msd)
            case None =>
              val errorMessage = "Could not retrieve messageSpec information from the submission"
              logger.warn(errorMessage)
              Left(InvalidXmlError(errorMessage))
          }
        case Left(list) =>
          // Todo - proper error mapping will be added (including tests) in ticket https://jira.tools.tax.service.gov.uk/browse/DAC6-3911
          // using CBC xmlErrorMessageHelper.generateErrorMessages(list) as an example
          val errors: Seq[GenericError] = List(GenericError(1, Message("Some XML validation errors")))
          Left(SubmissionValidationFailure(ValidationErrors(errors), messageType.toString))
      }
    }

  private def checkReportingPeriodIsValid(extractedElements: XmlExtractedElements): Future[Either[SubmissionValidationResult, Unit]] = Future {
    val isBeforeOrEqualToCurrentYear = extractedElements.reportingPeriod.getYear <= LocalDate.now(EUROPE_LONDON_TIME_ZONE).getYear
    val isAfterEarliestDate = extractedElements.reportingPeriod.isAfter(appConfig.reportingPeriodEarliestDate) || extractedElements.reportingPeriod.isEqual(
      appConfig.reportingPeriodEarliestDate
    )
    extractedElements.reportingPeriod.isEqual(appConfig.reportingPeriodEarliestDate)

    if (isAfterEarliestDate && isBeforeOrEqualToCurrentYear)
      Right(())
    else
      Left(InvalidReportingPeriodError(s"The reporting period must be after ${appConfig.reportingPeriodEarliestDate} or in the current year"))
  }

  private def checkFIIDMatchesSendCompanyInAndReturnGiinIfPresent(subscriptionId: String, sendCompanyIn: String)(implicit
    headerCarrier: HeaderCarrier
  ): Future[Either[SubmissionValidationResult, FIInfo]] =
    financialInstitutionsService.getListOfFinancialInstitutions(subscriptionId).map { fiDetails =>
      fiDetails.find(_.FIID == sendCompanyIn) match {
        case Some(fi) => Right(FIInfo(fi.GIIN, fi.FIName))
        case None =>
          Left(FIIDDoesNotMatchSendCompanyInError("The FI ID in your file does not match any financial institutions in the service"))
      }
    }
}
