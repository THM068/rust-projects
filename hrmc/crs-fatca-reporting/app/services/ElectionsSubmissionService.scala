/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services

import connectors.CADXElectionsConnector
import models.elections.*
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class ElectionsSubmissionService @Inject() (val cadxElectionsConnector: CADXElectionsConnector) extends Logging {

  def submitElections(submittedData: ElectionsSubmissionDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ElectionsSubmissionResult] =
    cadxElectionsConnector
      .submitElections(submittedData)
      .map { _ =>
        SubmissionSuccess
      }
      .recover { case NonFatal(e) =>
        logger.error(s"Elections submission failed unexpectedly for FIID: ${submittedData.fiId}. Error: ${e.getMessage}", e)
        SubmissionError("An error occurred during submission. Please try again later.")
      }
}
