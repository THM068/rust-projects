/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, urlEqualTo}
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import play.api.libs.ws.{DefaultWSCookie, WSClient}
import play.api.mvc.{Session, SessionCookieBaker}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.ISpecBase

class IndexControllerISpec extends PlaySpec with ISpecBase {

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]
  val session                 = Session(Map("authToken" -> "abc123"))
  val sessionCookieBaker      = app.injector.instanceOf[SessionCookieBaker]
  val sessionCookie           = sessionCookieBaker.encodeAsCookie(session)
  val wsSessionCookie         = DefaultWSCookie(sessionCookie.name, sessionCookie.value)

  "GET / IndexController.onPageLoad" must {
    "return OK when the user is authorised" in {
      stubAuthorised("cbc12345")

      val readSubscriptionUrl = "/country-by-country-reporting/subscription/read-subscription/.*"
      val responseDetailString: String =
        """
          |{
          |"subscriptionID": "111111111",
          |"tradingName": "",
          |"isGBUser": true,
          |"primaryContact":
          |{
          |"email": "",
          |"phone": "",
          |"mobile": "",
          |"organisation": {
          |"organisationName": "orgName"
          |}
          |},
          |"secondaryContact":
          |{
          |"email": "",
          |"organisation": {
          |"organisationName": ""
          |}
          |}
          |}""".stripMargin

      stubPostResponse(readSubscriptionUrl, OK, responseDetailString)
      val response = await(
        buildClient()
          .withFollowRedirects(false)
          .addCookies(wsSessionCookie)
          .get()
      )
      response.status mustBe OK
      verifyPost(authUrl)
      response.body must include("Manage your country-by-country report")
    }

    "redirect to login when there is no active session" in {
      val response = await(
        buildClient()
          .withFollowRedirects(false)
          .get()
      )
      response.status mustBe SEE_OTHER
      response.header("Location").value must include("gg-sign-in")
    }
    "redirect to /unauthorised" in {
      stubPostUnauthorised("/auth/authorise")
      val formData = Map("field1" -> Seq("value1"), "field2" -> Seq("value2"))

      val response = await(
        buildClient()
          .withFollowRedirects(false)
          .addCookies(wsSessionCookie)
          .post(formData)
      )
      response.status mustBe SEE_OTHER
      response.header("Location") mustBe Some("/send-a-country-by-country-report/unauthorised")
      verifyPost(authUrl)
    }

    "redirect to problem client access page" in {
      server.stubFor(
        WireMock
          .post(urlEqualTo(authUrl))
          .inScenario("Problem with client access")
          .whenScenarioStateIs("STARTED")
          .withRequestBody(new EqualToJsonPattern(authRequest, true, false))
          .willReturn(aResponse().withStatus(OK).withBody(authOKResponseForAgent))
          .willSetStateTo("Failing_client_access")
      )

      server.stubFor(
        WireMock.post(urlEqualTo(authUrl))
          .inScenario("Problem with client access")
          .willReturn(
            aResponse()
              .withHeader("Failing-Enrolment","NO_ASSIGNMENT;HMRC-CBC-ORG")
              .withHeader("WWW-Authenticate","MDTP detail=Insufficient Enrolments")
            .withStatus(UNAUTHORIZED)
          )
      )

      val formData = Map("value" -> "valid cbc id")

      val response = await(
        buildClient()
          .withUrl(s"http://localhost:$port/send-a-country-by-country-report/agent/client-id")
          .withFollowRedirects(false)
          .addCookies(wsSessionCookie)
          .post(formData)
      )

    }

  }

}
