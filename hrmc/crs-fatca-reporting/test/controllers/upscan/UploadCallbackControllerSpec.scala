/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package controllers.upscan

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers._
import play.api.Application
import play.api.http.Status._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, route, status, POST}
import services.upscan.UpScanCallbackDispatcher

import scala.concurrent.Future

class UploadCallbackControllerSpec extends SpecBase with BeforeAndAfterEach {

  val upscanCallbackDispatcher: UpScanCallbackDispatcher = mock[UpScanCallbackDispatcher]

  override def beforeEach(): Unit = {
    reset(upscanCallbackDispatcher)
    super.beforeEach()
  }

  val application: Application = applicationBuilder()
    .overrides(
      bind[UpScanCallbackDispatcher].toInstance(upscanCallbackDispatcher)
    )
    .build()

  "UploadCallbackControllerSpec" - {
    "must return OK when valid request is passed with readycallback body" in {
      val requestBody = Json.parse(
        """{
              "reference" : "ref",
              "downloadUrl" : "downloadUrl",
              "uploadDetails" : {
                "uploadTimestamp" : "2025-09-19T14:17:46.657933Z",
                "checksum" : "1234",
                "fileMimeType" : "application/xml",
                "fileName" : "test.xml",
                "size" : 1000
                },
              "fileStatus" : "READY"
              }""".stripMargin
      )

      when(upscanCallbackDispatcher.handleCallback(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(POST, routes.UploadCallbackController.callback.url).withBody(requestBody)

      val result = route(application, request).value

      status(result) mustEqual OK
    }

    "must return OK when valid request is passed with failed" in {

      val requestBody = Json.parse(
        """{
           "reference" : "test",
           "failureDetails" : {
            "failureReason" : "QUARANTINE",
            "message" : "message"
            },
            "fileStatus" : "FAILED"
            }"""
      )

      when(upscanCallbackDispatcher.handleCallback(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(POST, routes.UploadCallbackController.callback.url).withBody(requestBody)

      val result = route(application, request).value

      status(result) mustEqual OK
    }

    "must return BadRequest when valid Invalid request is passed" in {

      val requestBody = Json.parse(
        """{
           "reference" : "test",
           "failureDetails" : {
            "failureReason" : "QUARANTINE",
            "message" : "message"
            }
            }"""
      )

      when(upscanCallbackDispatcher.handleCallback(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(POST, routes.UploadCallbackController.callback.url)
        .withBody(requestBody)

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST
    }
  }

}
