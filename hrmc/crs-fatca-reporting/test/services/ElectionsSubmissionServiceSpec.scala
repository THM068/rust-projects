/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services

import connectors.CADXElectionsConnector
import models.elections.*
import org.apache.pekko.util.Helpers.Requiring
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class ElectionsSubmissionServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  val mockConnector: CADXElectionsConnector = mock[CADXElectionsConnector]
  val service                               = new ElectionsSubmissionService(mockConnector)

  private val testFiid            = FIID("TestFIID123")
  private val testReportingPeriod = ReportingPeriod("2024")

  private val crsDetails   = CRS(HasCARF(Some(true)), HasContracts(Some(false)), HasDormantAccounts(Some(true)), HasThresholds(Some(false)))
  private val fatcaDetails = FATCA(HasThresholds(Some(true)), HasTreasuryRegulations(Some(true)))

  val crsOnlyData: ElectionsSubmissionDetails = ElectionsSubmissionDetails(
    fiId = testFiid.toString,
    reportingPeriod = testReportingPeriod.toString,
    crsDetails = Some(CrsElectionsDetails(Some(true), Some(false), Some(true), Some(false))),
    fatcaDetails = None
  )

  val fatcaOnlyData: ElectionsSubmissionDetails = ElectionsSubmissionDetails(
    fiId = testFiid.toString,
    reportingPeriod = testReportingPeriod.toString,
    crsDetails = None,
    fatcaDetails = Some(FatcaElectionsDetails(Some(true), Some(true)))
  )

  val bothCrsFatcaData: ElectionsSubmissionDetails = ElectionsSubmissionDetails(
    fiId = testFiid.toString,
    reportingPeriod = testReportingPeriod.toString,
    crsDetails = Some(CrsElectionsDetails(Some(true), Some(false), Some(true), Some(false))),
    fatcaDetails = Some(FatcaElectionsDetails(Some(true), Some(true)))
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "submitElections" should {

    "return SubmissionSuccess when the connector call is successful (204 No Content)" in {
      when(mockConnector.submitElections(any[ElectionsRequest])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful("Request Processed Successfully"))

      val result: ElectionsSubmissionResult = service.submitElections(crsOnlyData).futureValue

      result shouldBe SubmissionSuccess
    }

    "return SubmissionError with generic message when connector returns 422 UNPROCESSABLE_ENTITY" in {
      val upstreamError = UpstreamErrorResponse("Business error: FIID not found", 422, 422)

      when(mockConnector.submitElections(any[ElectionsRequest])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(upstreamError))

      val result: ElectionsSubmissionResult = service.submitElections(crsOnlyData).futureValue

      result shouldBe SubmissionError("An error occurred during submission. Please try again later.")
    }

    "return SubmissionError with generic message when connector returns 400 Bad Request" in {
      val upstreamError = UpstreamErrorResponse("Malformed Request", 400, 400)

      when(mockConnector.submitElections(any[ElectionsRequest])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(upstreamError))

      val result: ElectionsSubmissionResult = service.submitElections(crsOnlyData).futureValue

      result shouldBe SubmissionError("An error occurred during submission. Please try again later.")
    }

    "return SubmissionError for any other NonFatal exception" in {
      val runtimeError = new RuntimeException("DB Connection Failed")

      when(mockConnector.submitElections(any[ElectionsRequest])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(runtimeError))

      val result: ElectionsSubmissionResult = service.submitElections(crsOnlyData).futureValue

      result shouldBe SubmissionError("An error occurred during submission. Please try again later.")
    }

    "correctly build RequestDetails and submit the request for" when {

      val captor = ArgumentCaptor.forClass(classOf[ElectionsRequest])

      "CRS only data is provided" in {
        when(mockConnector.submitElections(any[ElectionsRequest])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful("Success"))

        service.submitElections(crsOnlyData).futureValue

        verify(mockConnector, times(1)).submitElections(captor.capture())(any[HeaderCarrier], any[ExecutionContext])

        val capturedBody = captor.getValue
        capturedBody.requestDetails shouldBe ElectionRequestDetails(
          crs = Some(crsDetails),
          fatca = None,
          fiId = testFiid,
          reportingPeriod = testReportingPeriod
        )
      }

      "FATCA only data is provided" in {
        when(mockConnector.submitElections(any[ElectionsRequest])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful("Success"))

        service.submitElections(fatcaOnlyData).futureValue

        verify(mockConnector, times(1)).submitElections(captor.capture())(any[HeaderCarrier], any[ExecutionContext])

        val capturedBody = captor.getValue
        capturedBody.requestDetails shouldBe ElectionRequestDetails(
          crs = None,
          fatca = Some(fatcaDetails),
          fiId = testFiid,
          reportingPeriod = testReportingPeriod
        )
      }

      "Both CRS and FATCA data are provided" in {
        when(mockConnector.submitElections(any[ElectionsRequest])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful("Success"))

        service.submitElections(bothCrsFatcaData).futureValue

        verify(mockConnector, times(1)).submitElections(captor.capture())(any[HeaderCarrier], any[ExecutionContext])

        val capturedBody = captor.getValue
        capturedBody.requestDetails shouldBe ElectionRequestDetails(
          crs = Some(crsDetails),
          fatca = Some(fatcaDetails),
          fiId = testFiid,
          reportingPeriod = testReportingPeriod
        )
      }
    }
  }
}
