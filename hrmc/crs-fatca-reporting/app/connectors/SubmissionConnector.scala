/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import models.submission.ConversationId
import play.api.Logging
import play.api.libs.ws.DefaultBodyWritables.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class SubmissionConnector @Inject() (val config: AppConfig, val http: HttpClientV2) extends Logging {

  def submitCRS(
    submission: NodeSeq,
    conversationId: ConversationId
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    submit(
      serviceName = "crs-submission",
      url = url"${config.crsFileSubmission}/dac6/crs/CustomerSubmissionData/v1",
      submission = submission,
      conversationId = conversationId
    )

  def submitFatca(
    submission: NodeSeq,
    conversationId: ConversationId
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    submit(
      serviceName = "fatca-submission",
      url = url"${config.crsFileSubmission}/dac6/fatca/CustomerSubmissionData/v1",
      submission = submission,
      conversationId = conversationId
    )

  private def submit(serviceName: String, url: URL, submission: NodeSeq, conversationId: ConversationId)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {

    val headers =
      Seq()
        .withBearerToken(config.bearerToken(serviceName))
        .withXForwardedHost()
        .withDate()
        .withXCorrelationId()
        .withXConversationId(Some(conversationId.value))
        .withContentType(Some("application/xml;charset=UTF-8"))
        .withAccept(Some("application/xml"))
        .withEnvironment(Some(config.environment(serviceName)))

    http
      .post(url)
      .setHeader(headers: _*)
      .withBody(submission.toString)
      .execute[HttpResponse]
  }
}
