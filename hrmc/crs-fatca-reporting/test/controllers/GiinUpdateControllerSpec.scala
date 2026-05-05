/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers

import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status, stubControllerComponents, POST}
import services.FinancialInstitutionsService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import org.scalatestplus.mockito.MockitoSugar
import models.update.GiinUpdateRequest._

import scala.concurrent.{ExecutionContext, Future}

class GiinUpdateControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach {

  implicit lazy val materializer: Materializer = app.materializer
  implicit lazy val ec: ExecutionContext       = app.injector.instanceOf[ExecutionContext]
  implicit val hc: HeaderCarrier               = HeaderCarrier()

  val mockFinancialInstitutionsService: FinancialInstitutionsService = mock[FinancialInstitutionsService]

  lazy val controller = new GiinUpdateController(stubControllerComponents(), mockFinancialInstitutionsService)

  private val testSubscriptionId = "XEFATCA000000001"
  private val testFiid           = "FIID123"
  private val testGiin           = "A11111.99999.SL.826"

  private val contentTypeHeader = "Content-Type" -> "application/json"

  private def makeValidJson(): JsValue = Json.obj(
    "subscriptionId" -> testSubscriptionId,
    "fiid"           -> testFiid,
    "giin"           -> testGiin
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFinancialInstitutionsService)
  }

  private def mockServiceCall(response: Future[Unit]) =
    when(
      mockFinancialInstitutionsService.updateFiWithGiin(
        subscriptionId = any[String],
        fiid = any[String],
        newGiin = any[String]
      )(any[HeaderCarrier], any[ExecutionContext])
    ).thenReturn(response)

  "updateGiin" should {

    "return 204 NoContent when the request body is valid and the service succeeds" in {
      mockServiceCall(Future.successful(()))

      val jsonBody = makeValidJson()
      val fakeRequest = FakeRequest()
        .withMethod(POST)
        .withBody(jsonBody)
        .withHeaders(contentTypeHeader)

      val result = controller.updateGiin()(fakeRequest)

      status(result) mustBe NO_CONTENT
      contentAsString(result) mustBe ""
    }

    "return 500 InternalServerError when the service throws a NonFatal exception" in {
      val exceptionMessage = "Connection refused to downstream system."

      mockServiceCall(Future.failed(new RuntimeException(exceptionMessage)))

      val jsonBody = makeValidJson()
      val fakeRequest = FakeRequest()
        .withMethod(POST)
        .withBody(jsonBody)
        .withHeaders(contentTypeHeader)

      val result = controller.updateGiin()(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe ""
    }

    "return 400 BadRequest when the request body contains invalid JSON structure (JsError)" in {
      // Missing 'giin' field, which is mandatory
      val invalidBody = Json.obj("subscriptionId" -> "XEFATCA000000001", "fiid" -> testFiid)
      val fakeRequest = FakeRequest()
        .withMethod(POST)
        .withBody(invalidBody)
        .withHeaders(contentTypeHeader)

      val result = controller.updateGiin()(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("Invalid request body")
    }

    "return 400 BadRequest when the request body is missing or empty" in {
      val fakeRequest = FakeRequest(POST, routes.GiinUpdateController.updateGiin.url)
        .withHeaders(contentTypeHeader)

      val result = controller.updateGiin()(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("Invalid Json: No content to map due to end-of-input")
    }
  }
}
