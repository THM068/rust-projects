/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package controllers.upscan

import base.SpecBase
import models.upscan._
import org.bson.types.ObjectId
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers._
import play.api.Application
import play.api.http.Status._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty, GET, POST}
import repositories.upscan.UpScanSessionRepository
import services.upscan.UploadProgressTracker

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

class UploadFormControllerSpec extends SpecBase with BeforeAndAfterEach {

  val uploadProgressTracker: UploadProgressTracker = mock[UploadProgressTracker]
  val repository: UpScanSessionRepository          = mock[UpScanSessionRepository]

  override def beforeEach(): Unit = {
    reset(uploadProgressTracker, repository)
    super.beforeEach()
  }

  val application: Application = applicationBuilder()
    .overrides(
      bind[UploadProgressTracker].toInstance(uploadProgressTracker),
      bind[UpScanSessionRepository].toInstance(repository)
    )
    .build()

  "UploadFormControllerSpec" - {
    "getStatus" - {
      "must return NOTFOUND when valid request" in {

        val uuid     = UUID.randomUUID().toString
        val uploadId = UploadId(uuid)

        when(uploadProgressTracker.getUploadResult(uploadId)).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.UploadFormController.getStatus(uuid).url)

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
      }

      "must return OK when valid request" in {

        val uuid     = UUID.randomUUID().toString
        val uploadId = UploadId(uuid)

        when(uploadProgressTracker.getUploadResult(uploadId)).thenReturn(Future.successful(Some(Quarantined)))

        val request = FakeRequest(GET, routes.UploadFormController.getStatus(uuid).url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual "{\"_type\":\"Quarantined\"}"
      }
    }
    "getDetails" - {
      "must return NOTFOUND when valid request" in {

        val uuid     = UUID.randomUUID().toString
        val uploadId = UploadId(uuid)

        when(repository.findByUploadId(uploadId)).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.UploadFormController.getDetails(uuid).url)

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
      }

      "must return OK when valid request" in {

        val uuid     = UUID.randomUUID().toString
        val uploadId = UploadId(uuid)

        val uploadDetails = UploadSessionDetails(
          ObjectId.get(),
          uploadId,
          Reference("xxxx"),
          Quarantined,
          Instant.ofEpochSecond(1)
        )
        when(repository.findByUploadId(uploadId)).thenReturn(Future.successful(Some(uploadDetails)))

        val request = FakeRequest(GET, routes.UploadFormController.getDetails(uuid).url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual Json.toJson(uploadDetails).toString()
      }
    }
    "requestUpload" - {
      "must return OK when valid request body" in {

        val uuid              = UUID.randomUUID().toString
        val uploadId          = UploadId(uuid)
        val reference         = Reference("test")
        val upscanIdentifiers = UpscanIdentifiers(uploadId, reference)

        when(uploadProgressTracker.requestUpload(uploadId, reference)).thenReturn(Future.successful(true))

        val request = FakeRequest(POST, routes.UploadFormController.requestUpload.url).withBody(Json.toJson(upscanIdentifiers))

        val result = route(application, request).value

        status(result) mustEqual OK
      }

      "must return BadRequest when Invalid request" in {

        val requestBody = "{\"fileReference\" : \"test\"}"

        val request = FakeRequest(POST, routes.UploadFormController.requestUpload.url).withBody(Json.toJson(requestBody))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
