/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services

import cats.data.EitherT
import com.google.inject.{Inject, Singleton}
import config.AppConfig
import models.submission.{Accepted, ConversationId, Rejected}
import models.xml.{fromXml, BREResponse, ValidationStatus}
import play.api.Logging
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError, NoContent}
import repositories.submission.FileDetailsRepository
import services.validation.XMLValidationService
import uk.gov.hmrc.http.{BadRequestException, HttpException, InternalServerException}
import utils.CustomAlertUtil

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.xml.*

@Singleton
class EISService @Inject() (val xmlValidationService: XMLValidationService, appConfig: AppConfig, fileDetailsRepository: FileDetailsRepository)
    extends Logging {

  def processCRS(externalResponse: NodeSeq, conversationId: String)(implicit ec: ExecutionContext): Future[Result] =
    process(externalResponse, conversationId, appConfig.eisCRSFileUploadResponseXSDFilePath)

  def processFatca(externalResponse: NodeSeq, conversationId: String)(implicit ec: ExecutionContext): Future[Result] =
    process(externalResponse, conversationId, appConfig.eisFATCAFileUploadResponseXSDFilePath)

  private def process(externalResponse: NodeSeq, conversationId: String, xsdPath: String)(implicit ec: ExecutionContext): Future[Result] = {

    val result: EitherT[Future, HttpException, Result] = for {
      xml <- EitherT.fromEither[Future](
        xmlValidationService
          .validate(externalResponse, xsdPath)
          .left
          .map(errs =>
            logger.error(s"XML validation failed: $errs")
            BadRequestException("XML Schema validation failed")
          )
      )

      response <- EitherT.fromEither[Future](
        Try(fromXml[BREResponse](xml)).toEither.left.map(e =>
          logger.error(s"Failed to read XML: ${e.getMessage}")
          BadRequestException("Failed to read XML")
        )
      )

      validResponse <- EitherT.fromEither[Future](
        Either.cond(
          response.conversationID.trim.equalsIgnoreCase(conversationId.trim),
          response,
          BadRequestException("Conversation ID does not match request header & file content")
        )
      )

      updateResult <- EitherT(
        fileDetailsRepository
          .updateStatus(ConversationId(conversationId), mapStatus(validResponse))
          .map {
            case Some(_) => Right(NoContent)
            case None =>
              logger.error("Unable to update file status: mongo error")
              Left(InternalServerException("DB update failed"))
          }
      )
    } yield updateResult

    result.value.map {
      case Right(res)                   => res
      case Left(e: BadRequestException) => BadRequest(e.getMessage)
      case Left(_)                      => InternalServerError
    }
  }

  private def mapStatus(response: BREResponse) = response.genericStatusMessage.status match {
    case ValidationStatus.accepted => Accepted
    case ValidationStatus.rejected =>
      CustomAlertUtil.alertForProblemStatus(response.genericStatusMessage.validationErrors)
      Rejected(response.genericStatusMessage.validationErrors)
  }
}
