/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package connectors

import models.subscription.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.*
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.crsfatcareporting.utils.ISpecBase
import uk.gov.hmrc.http.UpstreamErrorResponse

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class SubscriptionConnectorSpec extends AnyFreeSpec with ISpecBase  {

  override lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.crs-fatca-registration.port" -> server.port(),
      "auditing.enabled"                                           -> "false",
      "mongodb.uri"                                                -> mongoUri
    )
    .build()

  lazy val connector: SubscriptionConnector =
    app.injector.instanceOf[SubscriptionConnector]

  "SubscriptionConnector" - {
    "must return Future success with response" in {
      val subscriptionID = SubscriptionID("testSubscriptionID")
      val readSubscriptionRequest = ReadSubscriptionRequest(subscriptionID.value)
      val response = DisplaySubscriptionResponse(
        DisplayResponseDetail(
          CrfaSubscriptionDetails(
            subscriptionID.value,
            Some("testBusinessNames"),
            true,
            ContactInformation(OrganisationDetails("testUser"), "testemail@test.com", None, None),
            None
          )
        )
      )
      stubPostResponse("/crs-fatca-registration/subscription/read-subscription", OK,
        Json.toJson(response).toString
      )

      whenReady(connector.readSubscription(readSubscriptionRequest)){
        result => assert(result.equals(response))
      }
    }
    "must return Exception" in {
      val subscriptionID = SubscriptionID("testSubscriptionID")
      val readSubscriptionRequest = ReadSubscriptionRequest(subscriptionID.value)
      stubPostResponse("/crs-fatca-registration/subscription/read-subscription",
        INTERNAL_SERVER_ERROR
      )

      val exception = intercept[UpstreamErrorResponse](Await.result(connector.readSubscription(readSubscriptionRequest), 1.seconds))
      exception.isInstanceOf[UpstreamErrorResponse] mustBe true
    }

  }
}
