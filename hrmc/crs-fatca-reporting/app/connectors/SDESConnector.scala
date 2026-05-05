/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package connectors

import com.google.inject.Inject
import config.AppConfig
import models.sdes
import models.sdes.FileTransferNotification
import play.api.Logging
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class SDESConnector @Inject() (
  val config: AppConfig,
  val http: HttpClientV2
) extends Logging {

  type Http_Status = Int
  private val clientIdHeader: Seq[(String, String)] = Seq("x-client-id" -> config.sdesClientId)

  def sendFileNotification(
    request: FileTransferNotification
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Http_Status, Http_Status]] =
    http
      .post(url"${config.sdesUrl}")
      .setHeader(clientIdHeader: _*)
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .map { response =>
        Right(response.status)
      }
      .recover { case NonFatal(e) =>
        logger.error(s"Unexpected error when sending file notification to SDES for correlationId [${request.audit.correlationID}]", e)
        Left(INTERNAL_SERVER_ERROR)
      }
}
