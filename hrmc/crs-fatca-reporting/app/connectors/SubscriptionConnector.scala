/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import models.subscription.{DisplaySubscriptionResponse, ReadSubscriptionRequest}
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject() (val config: AppConfig, val http: HttpClientV2) extends Logging {

  def readSubscription(
    readSubscriptionRequest: ReadSubscriptionRequest
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[DisplaySubscriptionResponse] =
    val readSubscription = url"${config.registrationUrl}/subscription/read-subscription"
    http
      .post(readSubscription)
      .withBody(Json.toJson(readSubscriptionRequest))
      .execute[HttpResponse]
      .map { res =>
        if is2xx(res.status) then res.json.as[DisplaySubscriptionResponse]
        else throw UpstreamErrorResponse(s"Unexpected status: ${res.status}", res.status)
      }
}
