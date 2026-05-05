/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.crsfatcareporting.controllers.upscan

import models.upscan.{Reference, UploadId, UpscanIdentifiers}
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.crsfatcareporting.utils.ISpecBase
import play.api.libs.ws.DefaultBodyWritables._

import java.util.UUID

class UploadFormControllerISpec extends PlaySpec with ISpecBase {


  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  "POST /upscan/upload UploadFormController.requestUpload" must {
    "return OK when the request is valid" in {
      val uuid = UUID.randomUUID().toString
      val uploadId = UploadId(uuid)
      val reference = Reference("test")
      val upscanIdentifiers = UpscanIdentifiers(uploadId,reference)
      val requestBody = Json.toJson(upscanIdentifiers)

      val response = await(
        buildClient("/upscan/upload")
          .withHttpHeaders("Content-Type" -> "application/json")
          .post(requestBody.toString())
      )
      response.status mustBe OK
    }
  }

  "GET /upscan/status/:uploadId UploadFormController.getStatus" must {
    "return OK when the request is valid" in {
      val uuid = UUID.randomUUID().toString
      val uploadId = UploadId(uuid)
      val reference = Reference("test")
      val upscanIdentifiers = UpscanIdentifiers(uploadId,reference)
      val requestBody = Json.toJson(upscanIdentifiers)
      await(
        buildClient("/upscan/upload")
          .withHttpHeaders("Content-Type" -> "application/json")
          .post(requestBody.toString())
      )

      val response = await(
        buildClient(s"/upscan/status/$uuid")
          .addHttpHeaders(
            "Accept"        -> "application/json"
          )
          .get()
      )
      response.status mustBe OK
    }
  }

  "GET /upscan/details/:uploadId UploadFormController.getDetails" must {
    "return OK when the request is valid" in {
      val uuid = UUID.randomUUID().toString
      val uploadId = UploadId(uuid)
      val reference = Reference("test")
      val upscanIdentifiers = UpscanIdentifiers(uploadId,reference)
      val requestBody = Json.toJson(upscanIdentifiers)
      await(
        buildClient("/upscan/upload")
          .withHttpHeaders("Content-Type" -> "application/json")
          .post(requestBody.toString())
      )

      val response = await(
        buildClient(s"/upscan/details/$uuid")
          .addHttpHeaders(
            "Accept"        -> "application/json"
          )
          .get()
      )
      response.status mustBe OK
    }
  }

}
