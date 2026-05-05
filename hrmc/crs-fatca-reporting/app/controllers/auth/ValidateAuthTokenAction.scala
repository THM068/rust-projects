/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package controllers.auth

import com.google.inject.ImplementedBy
import config.AppConfig
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.Unauthorized
import uk.gov.hmrc.http.Authorization
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ValidateAuthTokenActionImpl @Inject() (appConfig: AppConfig, val parser: BodyParsers.Default)(using val executionContext: ExecutionContext)
    extends ValidateAuthTokenAction
    with Logging {

  private def validateBearerToken[A](request: Request[A]): Boolean = {
    val crsEndpoint = "/callback/eis/crs"
    val validToken =
      if request.path.endsWith(crsEndpoint) then s"Bearer ${appConfig.bearerToken("crs-eis-callback")}"
      else s"Bearer ${appConfig.bearerToken("fatca-eis-callback")}"

    HeaderCarrierConverter.fromRequest(request).authorization match {
      case Some(Authorization(value)) => value == validToken
      case _                          => false
    }
  }

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    if (validateBearerToken(request)) {
      block(request)
    } else {
      logger.warn("Unexpected auth Bearer token received")
      Future.successful(Unauthorized)
    }

}

@ImplementedBy(classOf[ValidateAuthTokenActionImpl])
trait ValidateAuthTokenAction extends ActionBuilder[Request, AnyContent] with ActionFunction[Request, Request]
