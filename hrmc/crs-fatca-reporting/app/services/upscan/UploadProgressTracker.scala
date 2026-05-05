/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services.upscan

import models.upscan.{InProgress, Reference, UploadId, UploadSessionDetails, UploadStatus}
import org.bson.types.ObjectId
import play.api.Logging
import repositories.upscan.UpScanSessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait UploadProgressTracker {

  def requestUpload(
    uploadId: UploadId,
    fileReference: Reference
  ): Future[Boolean]

  def registerUploadResult(
    reference: Reference,
    uploadStatus: UploadStatus
  ): Future[Boolean]

  def getUploadResult(id: UploadId): Future[Option[UploadStatus]]

}

class MongoBackedUploadProgressTracker @Inject() (
  repository: UpScanSessionRepository
)(implicit ec: ExecutionContext)
    extends UploadProgressTracker
    with Logging {

  override def requestUpload(
    uploadId: UploadId,
    fileReference: Reference
  ): Future[Boolean] =
    repository.insert(
      UploadSessionDetails(ObjectId.get(), uploadId, fileReference, InProgress)
    )

  override def registerUploadResult(
    fileReference: Reference,
    uploadStatus: UploadStatus
  ): Future[Boolean] =
    repository.updateStatus(fileReference, uploadStatus)

  override def getUploadResult(id: UploadId): Future[Option[UploadStatus]] =
    for (result <- repository.findByUploadId(id)) yield result map { x =>
      x.status
    }

}
