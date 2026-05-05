/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services

import com.google.inject.Inject
import connectors.SubscriptionConnector
import models.subscription.{DisplaySubscriptionResponse, ReadSubscriptionRequest, SubscriptionID}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionService @Inject() (val subscriptionConnector: SubscriptionConnector) {

  def subscription(subscriptionId: SubscriptionID)(using headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[DisplaySubscriptionResponse] =
    subscriptionConnector.readSubscription(ReadSubscriptionRequest(subscriptionId.value))
}
