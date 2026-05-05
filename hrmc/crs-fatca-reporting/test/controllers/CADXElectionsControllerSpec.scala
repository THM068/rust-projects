/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers

import models.elections.{ElectionsSubmissionDetails, SubmissionError, SubmissionSuccess}
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status, stubControllerComponents, POST}
import services.ElectionsSubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}

class CADXElectionsControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach {

  implicit lazy val materializer: Materializer = app.materializer

  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val hc: HeaderCarrier         = HeaderCarrier()

  val mockElectionsSubmissionService: ElectionsSubmissionService = mock[ElectionsSubmissionService]

  lazy val controller = new CADXElectionsController(stubControllerComponents(), mockElectionsSubmissionService)

  private val testFiid            = "XY1234567890"
  private val testReportingPeriod = "2024-2024"

  private def makeValidJson(fiId: String = testFiid): JsValue = Json.parse(
    s"""
       |{
       |    "crs": {
       |      "hasThresholds": false,
       |      "hasDormantAccounts": true,
       |      "hasContracts": false,
       |      "hasCARF": true
       |    },
       |    "fatca": {
       |      "hasThresholds": true,
       |      "hasTreasuryRegulations": true
       |    },
       |    "fiId": "$fiId",
       |    "reportingPeriod": "$testReportingPeriod"
       |}
    """.stripMargin
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockElectionsSubmissionService)
  }

  "submitElections" should {

    "return 200 OK when the request body is valid and the service returns SubmissionSuccess" in {
      when(
        mockElectionsSubmissionService.submitElections(
          submittedData = any[ElectionsSubmissionDetails]
        )(any[HeaderCarrier], any[ExecutionContext])
      ).thenReturn(Future.successful(SubmissionSuccess))

      val jsonBody    = makeValidJson()
      val fakeRequest = FakeRequest(POST, routes.CADXElectionsController.submitElections().url).withJsonBody(jsonBody)

      val result = controller.submitElections()(fakeRequest)

      status(result) mustBe NO_CONTENT
    }

    "return 500 InternalServerError when the service returns SubmissionError" in {
      val errorMessage = "The FIID is not valid in the downstream system."

      when(
        mockElectionsSubmissionService.submitElections(
          submittedData = any[ElectionsSubmissionDetails]
        )(any[HeaderCarrier], any[ExecutionContext])
      ).thenReturn(Future.successful(SubmissionError(errorMessage)))

      val jsonBody    = makeValidJson()
      val fakeRequest = FakeRequest(POST, routes.CADXElectionsController.submitElections().url).withJsonBody(jsonBody)

      val result = controller.submitElections()(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe errorMessage
    }

    "return 400 BadRequest when the request body contains invalid JSON structure (JsError)" in {
      val invalidBody = Json.obj("someKey" -> "I am not a valid ElectionsRequestBody")
      val fakeRequest = FakeRequest(POST, routes.CADXElectionsController.submitElections().url).withJsonBody(invalidBody)

      val result = controller.submitElections()(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("Invalid request body format")
    }

    "return 400 BadRequest when the request body is missing or empty" in {
      val fakeRequest = FakeRequest(POST, routes.CADXElectionsController.submitElections().url)
        .withHeaders("Content-Type" -> "application/json")

      val result = controller.submitElections()(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Request body missing or not valid JSON."
    }
  }
}
