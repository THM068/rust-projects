/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.validation

import base.SpecBase
import models.submission.MessageSpecData
import models.submission.MessageType.CRS
import models.validation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers._
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, POST}
import play.api.test.{FakeHeaders, FakeRequest}
import services.validation.UploadedXmlValidationEngine
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class SubmissionValidationControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockValidationEngine: UploadedXmlValidationEngine = mock[UploadedXmlValidationEngine]
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[UploadedXmlValidationEngine].toInstance(mockValidationEngine)
      )
      .build()

  private lazy val controller = app.injector.instanceOf[SubmissionValidationController]

  private val messageSpecData = MessageSpecData(
    sendingCompanyIN = "some-sending-company-in",
    messageRefId = "some-message-ref-id",
    messageType = CRS,
    reportingFIName = "some-reporting-fi-name",
    reportingPeriod = LocalDate.of(2023, 1, 1),
    giin = Some("some-giin"),
    fiNameFromFim = "some-finame",
    electionsRequired = true
  )

  val upscanUrl       = "/some-upscan-url"
  val conversationId  = "conversationId123"
  val subscriptionId  = "subscriptionId123"
  val fileReferenceId = "fileReferenceId123"
  val validateRequestJsonBody =
    Json.obj("url" -> upscanUrl, "conversationId" -> conversationId, "subscriptionId" -> subscriptionId, "fileReferenceId" -> fileReferenceId)

  override def beforeEach(): Unit = {
    reset(mockValidationEngine)
    super.beforeEach()
  }

  "validateSubmission" - {

    "must validate a submission and return OK with success message" in {
      val submissionSuccess = SubmissionValidationSuccess(messageSpecData)

      when(
        mockValidationEngine.validateUploadSubmission(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(submissionSuccess))

      val request                = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url, FakeHeaders(), validateRequestJsonBody)
      val result: Future[Result] = controller.validateSubmission()(request)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(submissionSuccess).as[JsObject] + ("type" -> JsString("Success"))
    }
    "must validate a submission and return OK with a schema failure error message" in {
      val expectedErrors =
        Seq(
          GenericError(176, Message("xml.empty.field", List("Entity"))),
          GenericError(258, Message("xml.add.a.element", List("Summary")))
        )

      val submissionError = SubmissionValidationFailure(ValidationErrors(expectedErrors), "CRS")

      when(
        mockValidationEngine.validateUploadSubmission(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(submissionError))

      val request                = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url, FakeHeaders(), validateRequestJsonBody)
      val result: Future[Result] = controller.validateSubmission()(request)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(submissionError).as[JsObject] + ("type" -> JsString("ValidationFailure"))
    }

    "must validate a submission and return OK with a FIID Does Not Match SendCompanyIn failure error message" in {
      val fiIDDoesNotMatchSendCompanyInError =
        FIIDDoesNotMatchSendCompanyInError("The FI ID in your file does not match any financial institutions in the service")

      when(
        mockValidationEngine.validateUploadSubmission(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(fiIDDoesNotMatchSendCompanyInError))

      val request                = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url, FakeHeaders(), validateRequestJsonBody)
      val result: Future[Result] = controller.validateSubmission()(request)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(fiIDDoesNotMatchSendCompanyInError).as[JsObject] + ("type" -> JsString("InvalidFIID"))
    }

    "must validate a submission and return OK with an invalid reporting period date error" in {
      val invalidReportingPeriodError = InvalidReportingPeriodError("The reporting period must be after 2020-12-31 or in the current year")

      when(
        mockValidationEngine.validateUploadSubmission(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(invalidReportingPeriodError))

      val request                = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url, FakeHeaders(), validateRequestJsonBody)
      val result: Future[Result] = controller.validateSubmission()(request)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(invalidReportingPeriodError).as[JsObject] + ("type" -> JsString("InvalidReportingPeriod"))
    }

    "must return a ok request when an invalid message type is present in xml" in {
      val invalidMessageTypeError = InvalidMessageTypeError()

      when(
        mockValidationEngine.validateUploadSubmission(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(invalidMessageTypeError))

      val request                = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url, FakeHeaders(), validateRequestJsonBody)
      val result: Future[Result] = controller.validateSubmission()(request)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(invalidMessageTypeError).as[JsObject] + ("type" -> JsString("InvalidMessageType"))
    }

    "return InternalServerError and audit when UpscanURL is missing from the request body" in {

      val invalidJsonBody = Json.obj(
        "someOtherField" -> "someValue"
      )
      val request = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url, FakeHeaders(), invalidJsonBody)

      val result: Future[Result] = controller.validateSubmission()(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe "Missing upscan URL"
    }

    "returns InternalServerError when a ViewElectionErrors is returned from the validation engine" in {
      val viewElectionErrors = ViewElectionErrors("Elections could not be found for the provided FIID")

      when(
        mockValidationEngine.validateUploadSubmission(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(viewElectionErrors))

      val request = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url, FakeHeaders(), validateRequestJsonBody)

      val result: Future[Result] = controller.validateSubmission()(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "must validate a submission and return BadRequest with error message" in {
      val invalidXmlError = InvalidXmlError("Sax Exception occurred")

      when(
        mockValidationEngine.validateUploadSubmission(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(invalidXmlError))

      val request = FakeRequest(POST, routes.SubmissionValidationController.validateSubmission.url, FakeHeaders(), validateRequestJsonBody)

      val result: Future[Result] = controller.validateSubmission()(request)

      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.toJson(InvalidXmlError("Invalid xml provided")).as[JsObject] + ("type" -> JsString("InvalidXml"))
    }

  }
}
