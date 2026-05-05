/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services

import connectors.FinancialInstitutionsConnector
import models.financialinstitutions.FIDetail
import play.api.Logging
import play.api.libs.json.{JsResult, JsResultException, JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class FinancialInstitutionsService @Inject() (connector: FinancialInstitutionsConnector) extends Logging {

  def getListOfFinancialInstitutions(subscriptionId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Seq[FIDetail]] =
    connector
      .viewFis(subscriptionId)
      .map {
        case res if is2xx(res.status)                                                                             => extractList(res.body)
        case res if res.status == 422 && (Json.parse(res.body) \ "errorDetail" \ "errorCode").as[String] == "001" => Seq.empty
        case res =>
          logger.error(s"Failed to retrieve financial institutions. Error status:${res.status}")
          throw new RuntimeException(res.body)
      }
      .recover { case NonFatal(e) =>
        if (e.getMessage.contains("001") || e.getMessage.contains("No matching records found")) {
          logger.warn(s"Financial institutions not found for subscriptionId: $subscriptionId")
          Seq.empty
        } else {
          logger.error(s"An error occurred while retrieving financial institutions: ${e.getMessage}", e)
          throw e
        }
      }

  private def extractList(body: String): Seq[FIDetail] = {
    val json: JsValue                        = Json.parse(body)
    val listsResult: JsResult[Seq[FIDetail]] = (json \ "ViewFIDetails" \ "ResponseDetails" \ "FIDetails").validate[Seq[FIDetail]]
    listsResult.fold(
      errors => throw JsResultException(errors),
      value => value
    )
  }

  def updateFiWithGiin(subscriptionId: String, fiid: String, newGiin: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Unit] =
    for {
      viewResponse <- connector.viewFi(subscriptionId, fiid)
      _            <- validateViewResponse(viewResponse, fiid)
      fiDetails = extractList(viewResponse.body)
      fiDetail <- findMatchingFi(fiDetails, subscriptionId, fiid)
      updated = fiDetail.copy(GIIN = Some(newGiin))
      updateResp <- connector.updateFi(updated)
      _          <- validateUpdateResponse(updateResp, fiid)
    } yield ()

  private def validateViewResponse(response: HttpResponse, fiid: String): Future[Unit] =
    if (is2xx(response.status)) Future.successful(())
    else {
      logger.error(
        s"Failed to retrieve FI details for $fiid. " +
          s"Status: ${response.status}, Body: ${response.body}"
      )
      Future.failed(
        new RuntimeException(s"Failed to view FI details. Status: ${response.status}")
      )
    }

  private def findMatchingFi(fiDetails: Seq[FIDetail], subscriptionId: String, fiid: String): Future[FIDetail] =
    fiDetails.find(_.FIID == fiid) match {
      case Some(detail) => Future.successful(detail)
      case None =>
        logger.warn(
          s"No FI found for FIID $fiid in FI-Management response for subscriptionId $subscriptionId"
        )
        Future.failed(
          new RuntimeException(s"No FI found for $fiid in FI-Management response")
        )
    }

  private def validateUpdateResponse(response: HttpResponse, fiid: String): Future[Unit] =
    if (is2xx(response.status)) Future.successful(())
    else {
      logger.error(
        s"Update FI failed for $fiid. " +
          s"Status: ${response.status}, Body: ${response.body}"
      )
      Future.failed(
        new RuntimeException(s"Failed to update FI details. Status: ${response.status}")
      )
    }
}
