/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services

import com.google.inject.Inject
import connectors.CADXElectionsConnector
import models.Constants.EUROPE_LONDON_TIME_ZONE
import models.elections.{CheckElectionRequiredRequest, ReportingPeriod}
import models.submission.MessageType.{CRS, FATCA}
import models.validation.{SubmissionValidationResult, ViewElectionErrors}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ElectionService @Inject() (val cadxElectionsConnector: CADXElectionsConnector) extends Logging {

  def checkElectionRequired(
    checkElectionRequiredRequest: CheckElectionRequiredRequest
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Either[SubmissionValidationResult, Boolean]] =
    if (!isReportingYearValid(checkElectionRequiredRequest.reportingPeriod)) {
      Future.successful(Right(false))
    } else {
      cadxElectionsConnector
        .viewElections(checkElectionRequiredRequest.fiId)
        .map { electionList =>
          val reportingPeriod = ReportingPeriod(checkElectionRequiredRequest.reportingPeriod.toString)
          val hasMatching = electionList.exists { e =>
            e.reportingPeriod == reportingPeriod && {
              checkElectionRequiredRequest.regime match {
                case CRS   => e.crs.isDefined
                case FATCA => e.fatca.isDefined
              }
            }
          }
          Right(!hasMatching)
        }
        .recover { case ex: Throwable =>
          logger.error(s"Error retrieving election for FIID: ${checkElectionRequiredRequest.fiId}", ex)
          Left(ViewElectionErrors(ex.getMessage))
        }
    }

  private def isReportingYearValid(reportingYear: Int): Boolean = {
    val currentYear = LocalDate.now(EUROPE_LONDON_TIME_ZONE).getYear
    reportingYear >= (currentYear - 12) && reportingYear <= currentYear
  }
}
