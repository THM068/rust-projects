/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services

import base.SpecBase
import connectors.SubscriptionConnector
import models.subscription.*
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionServiceSpec extends SpecBase {
  val mockConnector: SubscriptionConnector = mock[SubscriptionConnector]
  val service                              = new SubscriptionService(mockConnector)

  "SubscriptionService" - {
    "subscription should return subscriptionDetail when connector returns success" in {
      val subscriptionId = SubscriptionID("testID")
      val response = DisplaySubscriptionResponse(
        DisplayResponseDetail(
          CrfaSubscriptionDetails(
            subscriptionId.value,
            Some("testBusinessName"),
            true,
            ContactInformation(OrganisationDetails("testUser"), "testemail@test.com", None, None),
            None
          )
        )
      )

      when(mockConnector.readSubscription(ReadSubscriptionRequest(subscriptionId.value))).thenReturn(Future.successful(response))
      whenReady(service.subscription(subscriptionId)) { result =>
        result mustBe response
      }
    }

    "subscription should fail when connector returns failure" in {
      val subscriptionId = SubscriptionID("testID")
      when(mockConnector.readSubscription(ReadSubscriptionRequest(subscriptionId.value))).thenReturn(Future.failed(new RuntimeException("Failed")))

      val result = service.subscription(subscriptionId)

      an[RuntimeException] must be thrownBy result.futureValue
    }
  }
}
