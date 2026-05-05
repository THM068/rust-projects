/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package services.submission

import cats.data.EitherT
import com.google.inject.Inject
import connectors.SubmissionConnector
import models.Constants.EUROPE_LONDON_TIME_ZONE
import models.error.{ApiError, BackendError, RepositoryError, SubmissionServiceError}
import models.submission.*
import models.submission.MessageType.*
import models.subscription.{DisplayResponseDetail, SubscriptionID}
import models.xml.XmlHandler
import play.api.Logging
import repositories.submission.FileDetailsRepository
import services.SubscriptionService
import services.transform.{CrsTransformService, FatcaTransformService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpErrorFunctions.{is2xx, is5xx}

import java.time.{Clock, LocalDateTime, ZonedDateTime}
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}
import scala.xml.{Elem, NodeSeq}

@Singleton
class SubmissionService @Inject() (
  subscriptionService: SubscriptionService,
  sdesService: SDESService,
  fileDetailsRepository: FileDetailsRepository,
  xmlHandler: XmlHandler,
  crsTransformService: CrsTransformService,
  fatcaTransformService: FatcaTransformService,
  submissionConnector: SubmissionConnector
)(implicit val clock: Clock, ec: ExecutionContext)
    extends Logging {

  def submitLargeFile(submissionDetails: SubmissionDetails)(implicit hc: HeaderCarrier): Future[Either[BackendError, ConversationId]] = {

    val conversationId = ConversationId.fromUploadId(submissionDetails.uploadId)

    subscriptionService.subscription(SubscriptionID(submissionDetails.enrolmentId)).flatMap { subscriptionResponse =>

      val crfaDetails = subscriptionResponse.success.crfaSubscriptionDetails

      sdesService
        .sendFileNotification(
          submissionDetails,
          crfaDetails,
          conversationId,
          submissionDetails.messageSpecData.messageType
        )
        .flatMap {

          case Right(_) =>
            val fileDetails = FileDetails(
              _id = conversationId,
              enrolmentId = submissionDetails.enrolmentId,
              messageRefId = submissionDetails.messageSpecData.messageRefId,
              reportingEntityName = submissionDetails.messageSpecData.reportingFIName,
              status = Pending,
              name = submissionDetails.fileName,
              submitted = LocalDateTime.now(clock),
              lastUpdated = LocalDateTime.now(clock),
              reportingPeriod = submissionDetails.messageSpecData.reportingPeriod,
              messageType = submissionDetails.messageSpecData.messageType,
              fileType = LargeFile
            )

            fileDetailsRepository
              .insert(fileDetails)
              .map { _ =>
                Right(conversationId)
              }
              .recover { case NonFatal(e) =>
                Left(RepositoryError(s"Failed to persist details for file with conversation Id [${conversationId.value}]: ${e.getMessage}"))
              }

          case Left(sdesError) =>
            Future.successful(Left(sdesError))
        }
    }
  }

  def submitNormalFile(
    submissionDetails: SubmissionDetails
  )(implicit hc: HeaderCarrier): Future[Either[BackendError, ConversationId]] = {

    val conversationId = ConversationId.fromUploadId(submissionDetails.uploadId)
    val documentUrl    = submissionDetails.documentUrl

    val result = xmlHandler.load(documentUrl) match {
      case Success(rootNode) =>
        val xmlElement     = Elem.apply(rootNode.prefix, rootNode.label, rootNode.attributes, rootNode.scope, true, rootNode.child: _*)
        val conversationId = ConversationId.fromUploadId(submissionDetails.uploadId)

        for {
          subscriptionResponse <- EitherT(getSubscriptionDetails(submissionDetails.enrolmentId))
          submissionMetaData = SubmissionMetaData(zonedDateTimeNow, conversationId, Some(submissionDetails.fileName))
          crfaDetails        = subscriptionResponse.crfaSubscriptionDetails
          fileDetails = createFilePendingDetails(
            conversationId,
            submissionDetails,
            LocalDateTime.now(clock),
            NormalFile
          )
          _ <- EitherT(
            createEISXmlRequestAndSubmission(
              submissionDetails,
              xmlElement,
              submissionMetaData,
              subscriptionResponse
            )
          )
          _ <- EitherT(persistFileDetails(fileDetails))

        } yield conversationId

      case Failure(exception) =>
        val errorMessage = s"Error loading xml file [$documentUrl] with conversation Id [${conversationId.value}]: ${exception.getMessage}"
        logger.error(errorMessage, exception)
        EitherT.left(Future.successful(SubmissionServiceError(errorMessage)))
    }
    result.value
  }

  private def getSubscriptionDetails(enrolmentId: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[BackendError, DisplayResponseDetail]] =
    subscriptionService
      .subscription(SubscriptionID(enrolmentId))
      .map { res =>
        Right(res.success)
      }
      .recover { case NonFatal(e) =>
        val errorMessage = s"Failed to retrieve subscription details for enrolment Id [$enrolmentId]"
        logger.error(s"$errorMessage", e)
        Left(ApiError(errorMessage))
      }
  private def zonedDateTimeNow(implicit clock: Clock): ZonedDateTime = ZonedDateTime.now(clock.withZone(EUROPE_LONDON_TIME_ZONE))

  private def createFilePendingDetails(
    conversationId: ConversationId,
    submissionDetails: SubmissionDetails,
    submissionTime: LocalDateTime,
    fileType: FileType
  ) =
    FileDetails(
      _id = conversationId,
      enrolmentId = submissionDetails.enrolmentId,
      messageRefId = submissionDetails.messageSpecData.messageRefId,
      reportingEntityName = submissionDetails.messageSpecData.reportingFIName,
      status = Pending,
      name = submissionDetails.fileName,
      submitted = submissionTime,
      lastUpdated = submissionTime,
      reportingPeriod = submissionDetails.messageSpecData.reportingPeriod,
      messageType = submissionDetails.messageSpecData.messageType,
      fileType = fileType
    )

  private def persistFileDetails(fileDetails: FileDetails): Future[Either[BackendError, Unit]] =
    fileDetailsRepository
      .insert(fileDetails)
      .map {
        Right(_)
      }
      .recover { case NonFatal(e) =>
        val errorMessage = s"Failed to persist details for file with conversation Id [${fileDetails._id.value}]"
        logger.error(s"ERROR: $errorMessage", e)
        Left(RepositoryError(errorMessage))
      }

  private def createEISXmlRequestAndSubmission(
    submissionDetails: SubmissionDetails,
    uploadedXml: NodeSeq,
    submissionMetaData: SubmissionMetaData,
    displayResponseDetail: DisplayResponseDetail
  )(implicit hc: HeaderCarrier): Future[Either[BackendError, ConversationId]] = {
    val conversationId = submissionMetaData.conversationId
    val messageType    = submissionDetails.messageSpecData.messageType

    val submissionXmlRequestEither = messageType match {
      case CRS   => crsTransformService.transformAndValidate(uploadedXml, submissionMetaData, displayResponseDetail.crfaSubscriptionDetails)
      case FATCA => fatcaTransformService.transformAndValidate(uploadedXml, submissionMetaData, displayResponseDetail.crfaSubscriptionDetails)
    }

    submissionXmlRequestEither match {
      case Left(parseErrors) =>
        Future.successful(
          Left(SubmissionServiceError(s"Xml parse error file with conversation Id [${conversationId.value}]: ${parseErrors.mkString(", ")}"))
        )
      case Right(submissionXml) =>
        messageType match {
          case CRS =>
            submissionConnector
              .submitCRS(submissionXml, conversationId)
              .flatMap { httpResponse =>
                submissionResult(submissionMetaData, conversationId, httpResponse)
              }
              .recover { case NonFatal(e) =>
                val errorMessage = s"Failed to submit CRS file with conversation Id [${conversationId.value}]: ${e.getMessage}"
                logger.error(errorMessage)
                Left(SubmissionServiceError(errorMessage))
              }

          case FATCA =>
            submissionConnector
              .submitFatca(submissionXml, conversationId)
              .flatMap { httpResponse =>
                submissionResult(submissionMetaData, conversationId, httpResponse)
              }
              .recover { case NonFatal(e) =>
                val errorMessage = s"Failed to submit FATCA file with conversation Id [${conversationId.value}]: ${e.getMessage}"
                logger.error(errorMessage)
                Left(SubmissionServiceError(errorMessage))
              }
        }
    }
  }

  private def submissionResult(submissionMetaData: SubmissionMetaData, conversationId: ConversationId, httpResponse: HttpResponse) = {
    val statusCode = httpResponse.status
    if (is2xx(statusCode)) {
      Future.successful(Right(submissionMetaData.conversationId))
    } else {
      val errorMessage = s"Failed to submit file with conversation Id [${conversationId.value}]. Got status: $statusCode"
      if (is5xx(statusCode)) {
        logger.error(errorMessage)
      } else {
        logger.warn(errorMessage)
      }
      Future.successful(
        Left(SubmissionServiceError(s"Failed to submit file with conversation Id [${conversationId.value}]. Got status: $statusCode"))
      )
    }
  }
}
