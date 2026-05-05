/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services.upscan

import models.upscan._
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpScanCallbackDispatcher @Inject() (sessionStorage: UploadProgressTracker)(implicit
  val ec: ExecutionContext
) extends Logging {

  def handleCallback(callback: CallbackBody): Future[Boolean] = {
    val uploadStatus: UploadStatus = callback match {

      case s: ReadyCallbackBody =>
        UploadedSuccessfully(
          s.uploadDetails.fileName,
          s.uploadDetails.fileMimeType,
          s.downloadUrl,
          Option(s.uploadDetails.size),
          Option(s.uploadDetails.checksum)
        )

      case q: FailedCallbackBody if q.failureDetails.failureReason == "QUARANTINE" =>
        logger.warn(s"FailedCallbackBody, QUARANTINE: ${q.reference.value}")
        Quarantined

      case r: FailedCallbackBody if r.failureDetails.failureReason == "REJECTED" =>
        logger.warn(s"FailedCallbackBody, REJECTED: ${r.reference.value}")
        UploadRejected(r.failureDetails)

      case f: FailedCallbackBody =>
        logger.warn(s"FailedCallbackBody: ${f.reference.value}")
        Failed
    }

    sessionStorage.registerUploadResult(callback.reference, uploadStatus)
  }
}
