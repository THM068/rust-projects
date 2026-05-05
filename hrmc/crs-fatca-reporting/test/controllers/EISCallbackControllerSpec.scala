/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package controllers

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.Application
import play.api.http.Status.*
import play.api.inject.bind
import play.api.mvc.Results.NoContent
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, route, status, writeableOf_AnyContentAsXml, POST}
import services.EISService
import uk.gov.hmrc.http.HeaderNames

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class EISCallbackControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val randomUUID         = UUID.randomUUID()
  val mockEISService: EISService = mock[EISService]

  override def beforeEach(): Unit = {
    reset(mockEISService)
    super.beforeEach()
  }

  val application: Application = applicationBuilder()
    .overrides(
      bind[EISService].toInstance(mockEISService)
    )
    .build()

  val xml: NodeSeq = <gsm:BREResponse xmlns:gsm="http://www.hmrc.gsi.gov.uk/gsm">
    <requestCommon>
      <receiptDate>2001-12-17T09:30:47.400Z</receiptDate>
      <regime>AEOI</regime>
      <conversationID>{randomUUID}</conversationID>
      <schemaVersion>1.0.0</schemaVersion>
    </requestCommon>
    <requestDetail>
      <GenericStatusMessage>
        <ValidationErrors>
          <FileError>
            <Code>50009</Code>
            <Details>Duplicate message ref IDs</Details>
          </FileError>
          <RecordError>
            <Code>80000</Code>
            <Details>Duplicate doc ref IDs</Details>
            <DocRefIDInError>CBCUSER001DHSJEURUT20001</DocRefIDInError>
            <DocRefIDInError>CBCUSER001DHSJEURUT20002</DocRefIDInError>
          </RecordError>
        </ValidationErrors>
        <ValidationResult>
          <Status>Rejected</Status>
        </ValidationResult>
      </GenericStatusMessage>
    </requestDetail>
  </gsm:BREResponse>

  "EISResponseController" - {
    "handleCRS" - {
      "must return NoContent when input xml is valid" in {

        when(mockEISService.processCRS(any(), any())(any())).thenReturn(Future.successful(NoContent))

        val request = FakeRequest(POST, routes.EISCallbackController.handleCRS().url)
          .withHeaders("x-conversation-id" -> randomUUID.toString, HeaderNames.authorisation -> s"Bearer test-token")
          .withXmlBody(xml)

        val result = route(application, request).value

        status(result) mustBe NO_CONTENT
        verify(mockEISService, times(1)).processCRS(any(), any())(any())
      }

      "must return UnAuthorised when auth token is not matching" in {

        val request = FakeRequest(POST, routes.EISCallbackController.handleCRS().url)
          .withHeaders("x-conversation-id" -> randomUUID.toString, HeaderNames.authorisation -> s"Bearer token")
          .withXmlBody(xml)

        val result = route(application, request).value

        status(result) mustBe UNAUTHORIZED
        verify(mockEISService, times(0)).processCRS(any(), any())(any())
      }

      "must return BadRequest when conversation id is not present in request" in {

        val request = FakeRequest(POST, routes.EISCallbackController.handleCRS().url)
          .withHeaders(HeaderNames.authorisation -> s"Bearer test-token")
          .withXmlBody(xml)

        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
        verify(mockEISService, times(0)).processCRS(any(), any())(any())
      }
    }

    "handleFatca" - { // todo: this
      "must return NoContent when input xml is valid" in {

        when(mockEISService.processFatca(any(), any())(any())).thenReturn(Future.successful(NoContent))

        val request = FakeRequest(POST, controllers.routes.EISCallbackController.handleFatca().url)
          .withHeaders("x-conversation-id" -> randomUUID.toString, HeaderNames.authorisation -> s"Bearer test-token")
          .withXmlBody(xml)

        val result = route(application, request).value

        status(result) mustBe NO_CONTENT
        verify(mockEISService, times(1)).processFatca(any(), any())(any())
      }

      "must return UnAuthorised when auth token is not matching" in {

        val request = FakeRequest(POST, routes.EISCallbackController.handleFatca().url)
          .withHeaders("x-conversation-id" -> randomUUID.toString, HeaderNames.authorisation -> s"Bearer token")
          .withXmlBody(xml)

        val result = route(application, request).value

        status(result) mustBe UNAUTHORIZED
        verify(mockEISService, times(0)).processFatca(any(), any())(any())
      }

      "must return BadRequest when conversation id is not present in request" in {

        val request = FakeRequest(POST, routes.EISCallbackController.handleFatca().url)
          .withHeaders(HeaderNames.authorisation -> s"Bearer test-token")
          .withXmlBody(xml)

        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
        verify(mockEISService, times(0)).processFatca(any(), any())(any())
      }
    }
  }
}
