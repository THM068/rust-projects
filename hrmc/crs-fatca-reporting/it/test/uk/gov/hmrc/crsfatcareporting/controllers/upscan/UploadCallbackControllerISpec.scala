/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.crsfatcareporting.controllers.upscan

import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import play.api.libs.ws.WSClient
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.crsfatcareporting.utils.ISpecBase
import play.api.libs.ws.DefaultBodyWritables._

class UploadCallbackControllerISpec extends PlaySpec with ISpecBase {

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  "POST /callback UploadCallbackController.callback" must {
    "return OK when the request is valid" in {

      val requestBody =
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

      val response = await(
        buildClient("/callback")
          .withHttpHeaders("Content-Type" -> "application/json")
          .post(requestBody)
      )
      response.status mustBe OK
    }
  }

}
