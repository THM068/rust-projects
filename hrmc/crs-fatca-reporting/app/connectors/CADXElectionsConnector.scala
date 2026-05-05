/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package connectors

import config.AppConfig
import models.elections.*
import models.elections.{ElectionDetails, ElectionsRequest}
import play.api.Logging
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpResponse, NotFoundException, StringContextOps, UpstreamErrorResponse}

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import play.api.libs.ws.JsonBodyWritables.*

class CADXElectionsConnector @Inject() (val config: AppConfig, val httpClient: HttpClientV2) extends Logging {

  def viewElections(fiid: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ElectionDetails]] = {
    val serviceName = "view-elections-details"
    val endpoint    = s"${config.cadxViewElectionsDetailUrl}/dac6/ViewElectionData/v1/$fiid"

    httpClient
      .get(url"$endpoint")
      .setHeader(extraHeaders(config, serviceName): _*)
      .execute[HttpResponse]
      .map { res =>
        (res.json \ "responseDetails" \ "electionDetails").as[Seq[ElectionDetails]]
      }
      .recover {
        case NonFatal(e: NotFoundException) =>
          logger.warn(s"No elections found for the given FIID: $fiid")
          Seq.empty
        case NonFatal(e) =>
          logger.error(s"An error occurred while retrieving elections for FIID: $fiid", e)
          throw e
      }
  }

  def submitElections(requestBody: ElectionsRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {
    val serviceName   = "submit-elections-details"
    val submissionUrl = url"${config.cadxSubmitElectionsDetailUrl}/dac6/SubmitElectionData/v1"

    httpClient
      .post(submissionUrl)
      .withBody(Json.toJson(requestBody))
      .setHeader(extraHeaders(config, serviceName): _*)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case NO_CONTENT =>
            response.body
          case status =>
            logger.error(s"Elections submission failed with status $status. Response body: ${response.body}")
            throw UpstreamErrorResponse(s"Elections submission failed with status $status", status, status)
        }
      }
      .recover { case NonFatal(e) =>
        logger.error(s"An error occurred while submitting elections", e)
        throw e
      }
  }

  private[connectors] def extraHeaders(
    config: AppConfig,
    serviceName: String
  )(implicit headerCarrier: HeaderCarrier): Seq[(String, String)] = {
    val newHeaders = headerCarrier
      .copy(authorization = Some(Authorization(s"Bearer ${config.bearerToken(serviceName)}")))

    newHeaders.headers(Seq(HeaderNames.authorisation)) ++ addHeaders(config.environment(serviceName))
  }

  private[connectors] def addHeaders(eisEnvironment: String)(implicit headerCarrier: HeaderCarrier): Seq[(String, String)] = {
    val formatter                      = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'UTC'").withZone(ZoneId.of("UTC"))
    val stripSession: String => String = (input: String) => input.replace("session-", "")

    Seq(
      "x-forwarded-host"  -> "mdtp",
      "date"              -> ZonedDateTime.now().format(formatter),
      "x-correlation-id"  -> headerCarrier.requestId.map(_.toString).getOrElse(UUID.randomUUID().toString),
      "x-conversation-id" -> headerCarrier.sessionId.map(_.toString).map(id => stripSession(id)).getOrElse(UUID.randomUUID().toString),
      "x-regime-type"     -> "CRSFATCA",
      "content-type"      -> "application/json",
      "accept"            -> "application/json",
      "Environment"       -> eisEnvironment
    )
  }
}
