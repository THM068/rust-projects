/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.crsfatcareporting.utils

import com.github.tomakehurst.wiremock.{client, WireMockServer}
import com.github.tomakehurst.wiremock.client.{MappingBuilder, ResponseDefinitionBuilder, WireMock}
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.http.{HttpHeader, HttpHeaders, RequestMethod}
import com.github.tomakehurst.wiremock.matching.{EqualToJsonPattern, EqualToPattern}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import play.api.http.Status.UNAUTHORIZED
import play.api.libs.json.Json

import java.net.ServerSocket
import scala.jdk.CollectionConverters.*
import scala.util.Using

object WireMockConstants {
  val stubPort: Int = Using(new ServerSocket(0))(_.getLocalPort)
    .getOrElse(throw new Exception("Failed to find random free port"))
  val stubHost      = "localhost"
}

trait WireMockHelper extends BeforeAndAfterEach with BeforeAndAfterAll {
  this: Suite =>

  val wireMockHost: String                = WireMockConstants.stubHost
  val wireMockPort: Int                   = WireMockConstants.stubPort
  val mockServerUrl                       = s"http://$wireMockHost:$wireMockPort"
  protected val endpointConfigurationPath = "microservice.services"

  protected val server: WireMockServer = {
    val s = new WireMockServer(wireMockConfig().port(wireMockPort))
    s.start()
    WireMock.configureFor("localhost", wireMockPort)
    s
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    server.resetAll()
  }

  override def afterAll(): Unit = {
    server.stop()
    super.afterAll()
  }

  def stubPostResponse(url: String, status: Int, body: String = Json.obj().toString(), requestBody: Option[String] = None): StubMapping =
    server.stubFor(requestBody match
      case Some(value) =>
        post(urlPathMatching(url))
          .withRequestBody(equalToJson(value, true, true))
          .willReturn(
            aResponse()
              .withStatus(status)
              .withBody(body)
          )
      case None =>
        post(urlPathMatching(url))
          .willReturn(
            aResponse()
              .withStatus(status)
              .withBody(body)
          )
    )

  def stubPutResponse(url: String, status: Int, body: String = Json.obj().toString(), requestBody: Option[String] = None): StubMapping =
    server.stubFor(requestBody match
      case Some(value) =>
        put(urlPathMatching(url))
          .withRequestBody(equalToJson(value, true, true))
          .willReturn(
            aResponse()
              .withStatus(status)
              .withBody(body)
          )
      case None =>
        put(urlPathMatching(url))
          .willReturn(
            aResponse()
              .withStatus(status)
              .withBody(body)
          )
    )

  def stubGetResponse(url: String, status: Int, body: String = Json.obj().toString()): StubMapping =
    server.stubFor(
      WireMock
        .get(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  protected def getWireMockAppConfig(endpointNames: Seq[String]): Map[String, Any] =
    endpointNames
      .flatMap(endpointName =>
        Seq(
          s"$endpointConfigurationPath.$endpointName.host" -> wireMockHost,
          s"$endpointConfigurationPath.$endpointName.port" -> wireMockPort
        )
      )
      .toMap

  protected def getWireMockAppConfigWithRetry(endpointNames: Seq[String]): Map[String, Any] =
    endpointNames
      .flatMap(endpointName =>
        Seq(
          s"$endpointConfigurationPath.$endpointName.host"   -> wireMockHost,
          s"$endpointConfigurationPath.$endpointName.port"   -> wireMockPort,
          s"$endpointConfigurationPath.retry.retry-attempts" -> 1
        )
      )
      .toMap

  private def stripToPath(url: String) =
    if (url.startsWith("http://") || url.startsWith("https://"))
      url.dropWhile(_ != '/').dropWhile(_ == '/').dropWhile(_ != '/')
    else
      url

  private def urlWithParameters(url: String, parameters: Seq[(String, String)]) = {
    val queryParams = parameters
      .map { case (k, v) =>
        s"$k=$v"
      }
      .mkString("&")

    s"${stripToPath(url)}?$queryParams"
  }

  implicit class MappingBuilderExt(builder: client.MappingBuilder) {

    def withRequestHeaders(headers: Set[(String, String)]): MappingBuilder =
      headers.foldLeft(builder) { (builder, header) =>
        val (key, value) = header
        builder.withHeader(key, equalTo(value))
      }
  }

  implicit class ResponseDefinitionBuilderExt(builder: ResponseDefinitionBuilder) {

    def withResponseHeaders(headers: Set[(String, String)]): ResponseDefinitionBuilder = {
      val responseHeadersWithContentType = Set("Content-Type" -> "application/json; charset=utf-8")
        .union(headers)
        .toList
        .map { case (key, value) =>
          HttpHeader.httpHeader(key, value)
        }
      builder.withHeaders(new HttpHeaders(responseHeadersWithContentType.asJava))
    }

  }

  def stubResponse(
    url: String,
    statusCode: Int,
    requestMethod: RequestMethod = RequestMethod.POST,
    requestHeaders: Map[String, String] = Map.empty,
    responseBody: String = "",
    responseHeaders: Map[String, String] = Map.empty
  ): Unit =
    stubFor(
      request(requestMethod.getName, urlEqualTo(url))
        .withRequestHeaders(requestHeaders.toSet)
        .willReturn(
          aResponse()
            .withStatus(statusCode)
            .withResponseHeaders(responseHeaders.toSet)
            .withBody(responseBody)
        )
    )

  def stubGet(url: String, status: Int, body: String): Unit =
    server.stubFor(
      WireMock.get(urlEqualTo(stripToPath(url))).willReturn(aResponse().withStatus(status).withBody(body))
    )

  def stubPostUnauthorised(
    url: String
  ): Unit =
    server.stubFor(
      WireMock.post(urlEqualTo(stripToPath(url))).willReturn(aResponse().withStatus(UNAUTHORIZED))
    )

  def stubPost(url: String, status: Int, requestBody: String, returnBody: String): Unit =
    server.stubFor(
      WireMock
        .post(urlEqualTo(stripToPath(url)))
        .withRequestBody(new EqualToJsonPattern(requestBody, true, false))
        .willReturn(aResponse().withStatus(status).withBody(returnBody))
    )

  def stubPut(url: String, status: Int, requestBody: String, returnBody: String): Unit =
    server.stubFor(
      WireMock
        .put(urlEqualTo(stripToPath(url)))
        .withRequestBody(new EqualToJsonPattern(requestBody, true, false))
        .willReturn(aResponse().withStatus(status).withBody(returnBody))
    )

  def verifyGet(url: String): Unit =
    server.verify(getRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyGetWithParameters(url: String, parameters: Seq[(String, String)]): Unit =
    server.verify(getRequestedFor(urlEqualTo(urlWithParameters(url, parameters))))

  def verifyGetWithParametersAndHeaders(
    url: String,
    parameters: Seq[(String, String)] = Seq.empty,
    headers: Seq[(String, String)] = Seq.empty
  ): Unit = {
    val requestPattern = getRequestedFor(urlEqualTo(urlWithParameters(url, parameters)))
    val requestPatternWithHeaders = headers.foldLeft(requestPattern) { (pattern, header) =>
      pattern.withHeader(header._1, new EqualToPattern(header._2))
    }
    server.verify(requestPatternWithHeaders)
  }

  def verifyGetWithoutRetry(url: String): Unit =
    server.verify(1, getRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyGetWithRetry(url: String): Unit =
    server.verify(2, getRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyPost(url: String): Unit =
    server.verify(postRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyPut(url: String): Unit =
    server.verify(putRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyPutWithoutRetry(url: String): Unit =
    server.verify(1, putRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyPutWithRetry(url: String): Unit =
    server.verify(2, putRequestedFor(urlEqualTo(stripToPath(url))))

}
