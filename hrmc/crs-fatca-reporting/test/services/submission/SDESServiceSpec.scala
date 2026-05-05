/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services.submission

import org.scalatest.funsuite.AnyFunSuiteLike
import base.SpecBase
import connectors.SDESConnector
import generators.Generators
import models.error.SdesSubmissionError
import models.sdes.*
import models.submission.*
import models.subscription.CrfaSubscriptionDetails
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.IntegrationPatience
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.inject.bind
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito.{reset, when}
import org.scalatest.matchers.must.Matchers.mustBe

import java.time.Clock
import scala.concurrent.{ExecutionContext, Future}

class SDESServiceSpec extends SpecBase with IntegrationPatience with Generators with ScalaCheckDrivenPropertyChecks {

  val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]
  val mockSDESConnector: SDESConnector             = mock[SDESConnector]

  override def beforeEach(): Unit =
    reset(mockSDESConnector, mockSubscriptionService)

  override lazy val app: Application = applicationBuilder()
    .overrides(
      bind[SDESConnector].toInstance(mockSDESConnector),
      bind[SubscriptionService].toInstance(mockSubscriptionService),
      bind[Clock].toInstance(fixedClock)
    )
    .build()

  private val sdesService = app.injector.instanceOf[SDESService]

  "SDESService" - {
    "sendFileNotification" - {
      "must send file submission notification to SDES connector and return success status when connector returns a Right" in {
        forAll { (responseDetail: CrfaSubscriptionDetails, submissionDetails: SubmissionDetails) =>
          mockSdesConnectorWithResponse(Future.successful(Right(NO_CONTENT)))
          val conversationId = ConversationId.fromUploadId(submissionDetails.uploadId)

          val result = sdesService.sendFileNotification(submissionDetails, responseDetail, conversationId, MessageType.CRS).futureValue

          result.value mustBe conversationId
        }
      }

      "must send file submission to SDES connector and return error status when connector returns a Left" in {
        forAll { (responseDetail: CrfaSubscriptionDetails, submissionDetails: SubmissionDetails) =>
          mockSdesConnectorWithResponse(Future.successful(Left(INTERNAL_SERVER_ERROR)))
          val conversationId = ConversationId.fromUploadId(submissionDetails.uploadId)

          val result = sdesService.sendFileNotification(submissionDetails, responseDetail, conversationId, MessageType.CRS).futureValue

          result.left.value mustBe SdesSubmissionError(INTERNAL_SERVER_ERROR)
        }
      }
    }

  }

  private def mockSdesConnectorWithResponse(response: Future[Either[Int, Int]]): Unit =
    when(mockSDESConnector.sendFileNotification(any[FileTransferNotification])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(response)
}
