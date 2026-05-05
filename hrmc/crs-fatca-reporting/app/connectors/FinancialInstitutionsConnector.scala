/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import models.financialinstitutions.FIDetail
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionsConnector @Inject() (val config: AppConfig, val httpClient: HttpClientV2) {

  def viewFis(subscriptionId: String)(using
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    httpClient
      .get(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/$subscriptionId")
      .execute[HttpResponse]

  def viewFi(subscriptionId: String, fiid: String)(using
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    httpClient
      .get(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiid")
      .execute[HttpResponse]

  def updateFi(fiDetail: FIDetail)(using
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    httpClient
      .put(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/update")
      .withBody(Json.toJson(fiDetail))
      .execute[HttpResponse]
}
