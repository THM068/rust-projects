/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services.upscan

import base.SpecBase
import models.upscan._
import org.mockito.Mockito.{reset, when}
import org.scalatest.matchers.must.Matchers._
import play.api.Application
import play.api.inject.bind

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpScanCallbackDispatcherSpec extends SpecBase {

  private val mockUploadProgressTracker: UploadProgressTracker =
    mock[UploadProgressTracker]

  val application: Application =
    applicationBuilder()
      .overrides(
        bind[UploadProgressTracker].toInstance(mockUploadProgressTracker)
      )
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUploadProgressTracker)
  }

  "UpscanCallbackDispatcher" - {

    "handleCallback must return UploadedSuccessfully for the input ReadyCallbackBody" in {
      val reference = Reference("ref")
      val uploadDetails = UploadDetails(
        Instant.now(),
        "1234",
        "application/xml",
        "test.xml",
        1000
      )

      val readyCallbackBody = ReadyCallbackBody(
        reference,
        "downloadUrl",
        UploadDetails(
          Instant.now(),
          "1234",
          "application/xml",
          "test.xml",
          1000
        )
      )

      val uploadStatus = UploadedSuccessfully(
        uploadDetails.fileName,
        uploadDetails.fileMimeType,
        readyCallbackBody.downloadUrl,
        Option(uploadDetails.size),
        Option(uploadDetails.checksum)
      )

      when(
        mockUploadProgressTracker.registerUploadResult(reference, uploadStatus)
      ).thenReturn(Future.successful(true))

      val uploadCallbackDispatcher =
        new UpScanCallbackDispatcher(mockUploadProgressTracker)

      val result: Future[Boolean] =
        uploadCallbackDispatcher.handleCallback(readyCallbackBody)
      result.futureValue mustBe true
    }

    "handleCallback must return Quarantined for the input FailedCallbackBody" in {
      val reference    = Reference("ref")
      val errorDetails = ErrorDetails("QUARANTINE", "message")

      val readyCallbackBody = FailedCallbackBody(reference, errorDetails)

      val uploadStatus = Quarantined

      when(
        mockUploadProgressTracker.registerUploadResult(reference, uploadStatus)
      ).thenReturn(Future.successful(true))

      val uploadCallbackDispatcher =
        new UpScanCallbackDispatcher(mockUploadProgressTracker)

      val result: Future[Boolean] =
        uploadCallbackDispatcher.handleCallback(readyCallbackBody)
      result.futureValue mustBe true
    }

    "handleCallback must return REJECTED for the input FailedCallbackBody" in {
      val reference    = Reference("ref")
      val errorDetails = ErrorDetails("REJECTED", "message")

      val readyCallbackBody = FailedCallbackBody(reference, errorDetails)

      val uploadStatus = UploadRejected(readyCallbackBody.failureDetails)

      when(
        mockUploadProgressTracker.registerUploadResult(reference, uploadStatus)
      ).thenReturn(Future.successful(true))

      val uploadCallbackDispatcher =
        new UpScanCallbackDispatcher(mockUploadProgressTracker)

      val result: Future[Boolean] =
        uploadCallbackDispatcher.handleCallback(readyCallbackBody)
      result.futureValue mustBe true
    }

    "handleCallback must return Failed for the input FailedCallbackBody" in {
      val reference    = Reference("ref")
      val errorDetails = ErrorDetails("Failed", "message")

      val readyCallbackBody = FailedCallbackBody(reference, errorDetails)

      val uploadStatus = Failed

      when(
        mockUploadProgressTracker.registerUploadResult(reference, uploadStatus)
      ).thenReturn(Future.successful(true))

      val uploadCallbackDispatcher =
        new UpScanCallbackDispatcher(mockUploadProgressTracker)

      val result: Future[Boolean] =
        uploadCallbackDispatcher.handleCallback(readyCallbackBody)
      result.futureValue mustBe true
    }

  }
}
