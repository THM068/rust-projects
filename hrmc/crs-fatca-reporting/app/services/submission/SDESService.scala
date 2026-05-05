/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services.submission

import config.AppConfig
import connectors.SDESConnector
import models.error.{BackendError, SdesSubmissionError}
import models.sdes.{Audit, Checksum, File, FileTransferNotification, Property}
import models.submission.{ConversationId, MessageType, SubmissionDetails}
import models.subscription.CrfaSubscriptionDetails
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateTimeFormatUtil

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SDESService @Inject() (
  appConfig: AppConfig,
  sdesConnector: SDESConnector,
  metadataService: SDESFileMetadataService
)(implicit ec: ExecutionContext, clock: Clock)
    extends Logging {

  def sendFileNotification(
    submissionDetails: SubmissionDetails,
    subscriptionDetails: CrfaSubscriptionDetails,
    conversationId: ConversationId,
    regime: MessageType
  )(implicit hc: HeaderCarrier): Future[Either[BackendError, ConversationId]] = {

    logger.info(s"Sending large file with conversation Id [${conversationId.value}] to SDES")
    val submissionTime = DateTimeFormatUtil.zonedDateTimeNow.toLocalDateTime
    val metaData       = metadataService.compileMetaData(subscriptionDetails, conversationId, submissionTime, submissionDetails.fileName, regime)

    val fileNotificationRequest = createFileNotificationRequest(submissionDetails, conversationId, metaData)

    sdesConnector.sendFileNotification(fileNotificationRequest).map {
      case Right(_) => Right(conversationId)
      case Left(errorStatus) =>
        logger.error(s"Failed to send file with conversation Id [${conversationId.value}] to SDES. Got error status: $errorStatus")
        Left(SdesSubmissionError(errorStatus))
    }
  }

  private def createFileNotificationRequest(
    submissionDetails: SubmissionDetails,
    correlationId: ConversationId,
    metaData: Map[String, String]
  ): FileTransferNotification =
    FileTransferNotification(
      appConfig.sdesInformationType,
      File(
        Option(appConfig.sdesRecipientOrSender),
        submissionDetails.fileName,
        Option(submissionDetails.documentUrl),
        Checksum(appConfig.sdesChecksumAlgorithm, submissionDetails.checksum),
        submissionDetails.fileSize.toInt,
        mapToProperty(metaData)
      ),
      Audit(correlationId.value)
    )

  private def mapToProperty(metaData: Map[String, String]): List[Property] =
    metaData.toList.map { case (name, value) => Property(name, value) }
}
