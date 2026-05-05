/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package services.submission

import connectors.SubmissionConnector
import generators.{Generators, ModelGenerators}
import models.error.{ApiError, RepositoryError, SdesSubmissionError, SubmissionServiceError}
import models.submission.*
import models.submission.MessageType.{CRS, FATCA}
import models.subscription.{CrfaSubscriptionDetails, DisplayResponseDetail, DisplaySubscriptionResponse, SubscriptionID}
import models.validation.SaxParseError
import models.xml.XmlHandler
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as mEq}
import org.mockito.Mockito.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import repositories.submission.FileDetailsRepository
import services.SubscriptionService
import services.transform.{CrsTransformService, FatcaTransformService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.io.IOException
import java.time.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure
import scala.xml.{Elem, NodeSeq, TopScope}

class SubmissionServiceSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures
    with ModelGenerators
    with Generators
    with OptionValues
    with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  val fixedInstant: Instant        = Instant.parse("2026-01-06T12:00:00Z")
  val fixedZone: ZoneId            = ZoneId.of("UTC")
  implicit val fixedClock: Clock   = Clock.fixed(fixedInstant, fixedZone)
  val fixedDateTime: LocalDateTime = LocalDateTime.now(fixedClock)

  val mockSubscriptionService   = mock[SubscriptionService]
  val mockSdesService           = mock[SDESService]
  val mockFileDetailsRepository = mock[FileDetailsRepository]
  val mockXmlHandler            = mock[XmlHandler]
  val mockCrsTransformService   = mock[CrsTransformService]
  val mockFatcaTransformService = mock[FatcaTransformService]
  val mockSubmissionConnector   = mock[SubmissionConnector]

  override def beforeEach(): Unit =
    reset(mockSubscriptionService, mockSdesService, mockFileDetailsRepository)

  val service = new SubmissionService(
    mockSubscriptionService,
    mockSdesService,
    mockFileDetailsRepository,
    mockXmlHandler,
    mockCrsTransformService,
    mockFatcaTransformService,
    mockSubmissionConnector
  )

  val submissionDetails: SubmissionDetails = arbitrary[SubmissionDetails].sample.value
  val crfaDetails: CrfaSubscriptionDetails = arbitrary[CrfaSubscriptionDetails].sample.value
  val expectedConversationId               = ConversationId.fromUploadId(submissionDetails.uploadId)

  val mockResponseDetail = mock[DisplayResponseDetail]
  when(mockResponseDetail.crfaSubscriptionDetails).thenReturn(crfaDetails)
  val displaySubResponse = DisplaySubscriptionResponse(success = mockResponseDetail)

  "submitLargeFile" should {

    "successfully submit a file, send to SDES, and persist details" in {
      when(mockSubscriptionService.subscription(SubscriptionID(submissionDetails.enrolmentId)))
        .thenReturn(Future.successful(displaySubResponse))

      when(
        mockSdesService.sendFileNotification(
          submissionDetails,
          crfaDetails,
          expectedConversationId,
          submissionDetails.messageSpecData.messageType
        )
      )
        .thenReturn(Future.successful(Right(expectedConversationId)))

      when(mockFileDetailsRepository.insert(any[FileDetails]))
        .thenReturn(Future.successful(()))

      val result = service.submitLargeFile(submissionDetails).futureValue

      result shouldBe Right(expectedConversationId)

      verify(mockSdesService).sendFileNotification(
        submissionDetails,
        crfaDetails,
        expectedConversationId,
        submissionDetails.messageSpecData.messageType
      )

      val fileDetailsCaptor = ArgumentCaptor.forClass(classOf[FileDetails])
      verify(mockFileDetailsRepository).insert(fileDetailsCaptor.capture())

      val capturedFile = fileDetailsCaptor.getValue
      capturedFile.enrolmentId shouldBe submissionDetails.enrolmentId
      capturedFile.status shouldBe Pending
      capturedFile.submitted shouldBe fixedDateTime
      capturedFile.lastUpdated shouldBe fixedDateTime
      capturedFile.fileType shouldBe LargeFile
      capturedFile.reportingPeriod shouldBe submissionDetails.messageSpecData.reportingPeriod
    }

    "fail if SDES Service returns an error" in {
      when(mockSubscriptionService.subscription(SubscriptionID(submissionDetails.enrolmentId)))
        .thenReturn(Future.successful(displaySubResponse))

      val sdesError = SdesSubmissionError(503)

      when(
        mockSdesService.sendFileNotification(
          submissionDetails,
          crfaDetails,
          expectedConversationId,
          submissionDetails.messageSpecData.messageType
        )
      )
        .thenReturn(Future.successful(Left(sdesError)))

      val result = service.submitLargeFile(submissionDetails).futureValue

      result shouldBe Left(sdesError)

      verifyNoInteractions(mockFileDetailsRepository)
    }

    "fail if Repository insert fails" in {
      when(mockSubscriptionService.subscription(SubscriptionID(submissionDetails.enrolmentId)))
        .thenReturn(Future.successful(displaySubResponse))

      when(
        mockSdesService.sendFileNotification(
          submissionDetails,
          crfaDetails,
          expectedConversationId,
          submissionDetails.messageSpecData.messageType
        )
      )
        .thenReturn(Future.successful(Right(expectedConversationId)))

      when(mockFileDetailsRepository.insert(any[FileDetails]))
        .thenReturn(Future.failed(new RuntimeException("Mongo connection failed")))

      val result = service.submitLargeFile(submissionDetails).futureValue

      result match {
        case Left(e: RepositoryError) =>
          e.detail should include("Failed to persist details")
        case _ => fail("Expected RepositoryError")
      }
    }
  }

  "Submit normal file" should {
    val xmlSubmissionFilePath      = "xml-submission-file-path"
    val testXml                    = Elem("CRS_OECD", "CRS_OECD", xml.Null, TopScope, minimizeEmpty = true)
    val reportingPeriod: LocalDate = LocalDate.of(2014, 12, 31)
    val crsMessageSpecData: MessageSpecData =
      MessageSpecData(CRS, "683373339", "message-ref-id", "reporting-fi-name", reportingPeriod, giin = Some("689355555"), "First FI", true)
    val fatcaMessageSpecData: MessageSpecData = MessageSpecData(FATCA,
                                                                "683373339",
                                                                "message-ref-id",
                                                                "reporting-fi-name",
                                                                reportingPeriod,
                                                                giin = Some("689355555"),
                                                                fiNameFromFim = "First FI",
                                                                electionsRequired = true
    )
    val crsSubmissionDetails: SubmissionDetails = submissionDetails.copy(
      messageSpecData = crsMessageSpecData
    )
    val fatcaSubmissionDetails: SubmissionDetails = submissionDetails.copy(
      messageSpecData = fatcaMessageSpecData
    )

    "CRS: must submit file to EIS and persist the file details with Pending status" in {
      when(mockSubscriptionService.subscription(SubscriptionID(submissionDetails.enrolmentId)))
        .thenReturn(Future.successful(displaySubResponse))
      when(mockXmlHandler.load(submissionDetails.documentUrl)).thenReturn(scala.util.Success(testXml))

      when(mockCrsTransformService.transformAndValidate(any[NodeSeq], any[SubmissionMetaData], any[CrfaSubscriptionDetails]))
        .thenReturn(Right(testXml))

      when(mockSubmissionConnector.submitCRS(any[NodeSeq], mEq(expectedConversationId))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(OK, "submission response")))

      when(mockFileDetailsRepository.insert(any[FileDetails]))
        .thenReturn(Future.successful(()))

      val result = service.submitNormalFile(crsSubmissionDetails)

      result.futureValue match {
        case Right(conversationId) =>
          conversationId mustBe expectedConversationId
        case Left(error) =>
          fail(s"Expected Right but got Left with error: $error")
      }

      verify(mockCrsTransformService, times(1)).transformAndValidate(any[NodeSeq], any[SubmissionMetaData], any[CrfaSubscriptionDetails])
      verify(mockSubmissionConnector, times(1)).submitCRS(any[NodeSeq], mEq(expectedConversationId))(any[HeaderCarrier], any[ExecutionContext])

      val fileDetailsCaptor = ArgumentCaptor.forClass(classOf[FileDetails])
      verify(mockFileDetailsRepository).insert(fileDetailsCaptor.capture())

      val capturedFile = fileDetailsCaptor.getValue
      capturedFile.enrolmentId shouldBe crsSubmissionDetails.enrolmentId
      capturedFile.status shouldBe Pending
      capturedFile.submitted shouldBe fixedDateTime
      capturedFile.lastUpdated shouldBe fixedDateTime
      capturedFile.messageType shouldBe CRS
      capturedFile.fileType shouldBe NormalFile
      capturedFile.reportingPeriod shouldBe crsSubmissionDetails.messageSpecData.reportingPeriod
      verify(mockFileDetailsRepository, times(1)).insert(any[FileDetails])
    }

    "FATCA: must submit file to EIS and persist the file details with Pending status" in {
      when(mockSubscriptionService.subscription(SubscriptionID(submissionDetails.enrolmentId)))
        .thenReturn(Future.successful(displaySubResponse))
      when(mockXmlHandler.load(submissionDetails.documentUrl)).thenReturn(scala.util.Success(testXml))

      when(mockFatcaTransformService.transformAndValidate(any[NodeSeq], any[SubmissionMetaData], any[CrfaSubscriptionDetails]))
        .thenReturn(Right(testXml))

      when(mockFileDetailsRepository.insert(any[FileDetails]))
        .thenReturn(Future.successful(()))

      when(mockSubmissionConnector.submitFatca(any[NodeSeq], mEq(expectedConversationId))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(OK, "submission response")))

      when(mockFileDetailsRepository.insert(any[FileDetails]))
        .thenReturn(Future.successful(()))

      val result = service.submitNormalFile(fatcaSubmissionDetails)

      result.futureValue match {
        case Right(conversationId) =>
          conversationId mustBe expectedConversationId
        case Left(error) =>
          fail(s"Expected Right but got Left with error: $error")
      }

      verify(mockFatcaTransformService, times(1)).transformAndValidate(any[NodeSeq], any[SubmissionMetaData], any[CrfaSubscriptionDetails])
      verify(mockSubmissionConnector, times(1)).submitFatca(any[NodeSeq], mEq(expectedConversationId))(any[HeaderCarrier], any[ExecutionContext])

      val fileDetailsCaptor = ArgumentCaptor.forClass(classOf[FileDetails])
      verify(mockFileDetailsRepository).insert(fileDetailsCaptor.capture())

      val capturedFile = fileDetailsCaptor.getValue
      capturedFile.enrolmentId shouldBe fatcaSubmissionDetails.enrolmentId
      capturedFile.status shouldBe Pending
      capturedFile.submitted shouldBe fixedDateTime
      capturedFile.lastUpdated shouldBe fixedDateTime
      capturedFile.messageType shouldBe FATCA
      capturedFile.fileType shouldBe NormalFile
      capturedFile.reportingPeriod shouldBe fatcaSubmissionDetails.messageSpecData.reportingPeriod
      verify(mockFileDetailsRepository, times(1)).insert(any[FileDetails])
      verify(mockFileDetailsRepository, times(1)).insert(any[FileDetails])
    }

    "must return an api error when subscription service fails" in {
      when(mockXmlHandler.load(submissionDetails.documentUrl)).thenReturn(scala.util.Success(testXml))

      when(mockSubscriptionService.subscription(SubscriptionID(submissionDetails.enrolmentId)))
        .thenReturn(Future.failed(new RuntimeException("Subscription service error")))

      val result = service.submitNormalFile(submissionDetails)

      result.futureValue match {
        case Left(error) =>
          error mustBe a[ApiError]
        case Right(_) => fail("Expected Left but got Right")
      }
    }

    "must return a SubmissionServiceError when eis request xml fails validation" in {
      val xmlErrors: List[SaxParseError] = List()
      when(mockSubscriptionService.subscription(SubscriptionID(submissionDetails.enrolmentId)))
        .thenReturn(Future.successful(displaySubResponse))
      when(mockXmlHandler.load(submissionDetails.documentUrl)).thenReturn(scala.util.Success(testXml))

      when(mockFatcaTransformService.transformAndValidate(any[NodeSeq], any[SubmissionMetaData], any[CrfaSubscriptionDetails]))
        .thenReturn(Left(xmlErrors))
      val result = service.submitNormalFile(fatcaSubmissionDetails)

      result.futureValue match {
        case Left(error) =>
          error mustBe a[SubmissionServiceError]
        case Right(_) => fail("Expected Left but got Right")
      }
    }

    "FATCA: must return error when a failure occurs during file submission to EIS" in {
      when(mockSubscriptionService.subscription(SubscriptionID(submissionDetails.enrolmentId)))
        .thenReturn(Future.successful(displaySubResponse))
      when(mockXmlHandler.load(submissionDetails.documentUrl)).thenReturn(scala.util.Success(testXml))

      when(mockFatcaTransformService.transformAndValidate(any[NodeSeq], any[SubmissionMetaData], any[CrfaSubscriptionDetails]))
        .thenReturn(Right(testXml))

      when(mockFileDetailsRepository.insert(any[FileDetails]))
        .thenReturn(Future.successful(()))

      when(mockSubmissionConnector.submitFatca(any[NodeSeq], mEq(expectedConversationId))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "submission error")))

      val result = service.submitNormalFile(fatcaSubmissionDetails)

      result.futureValue match {
        case Left(error) =>
          error mustBe a[SubmissionServiceError]
        case Right(_) => fail("Expected Left but got Right")
      }
    }

    "CRS: must return error when a failure occurs during file submission to EIS" in {
      when(mockSubscriptionService.subscription(SubscriptionID(submissionDetails.enrolmentId)))
        .thenReturn(Future.successful(displaySubResponse))
      when(mockXmlHandler.load(submissionDetails.documentUrl)).thenReturn(scala.util.Success(testXml))

      when(mockCrsTransformService.transformAndValidate(any[NodeSeq], any[SubmissionMetaData], any[CrfaSubscriptionDetails]))
        .thenReturn(Right(testXml))

      when(mockFileDetailsRepository.insert(any[FileDetails]))
        .thenReturn(Future.successful(()))

      when(mockSubmissionConnector.submitCRS(any[NodeSeq], mEq(expectedConversationId))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "submission error")))

      val result = service.submitNormalFile(crsSubmissionDetails)

      result.futureValue match {
        case Left(error) =>
          error mustBe a[SubmissionServiceError]
        case Right(_) => fail("Expected Left but got Right")
      }
    }

    "must return error when unable to load xml file" in {
      when(mockXmlHandler.load(submissionDetails.documentUrl)).thenReturn(Failure(new IOException("File load error")))

      val result = service.submitNormalFile(submissionDetails)

      result.futureValue match {
        case Left(error) => error mustBe a[SubmissionServiceError]
        case Right(_)    => fail("Expected Left but got Right")
      }
    }
  }
}
