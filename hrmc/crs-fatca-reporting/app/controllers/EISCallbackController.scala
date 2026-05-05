/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package controllers

import controllers.auth.ValidateAuthTokenAction
import play.api.Logging
import play.api.mvc.Results.BadRequest
import play.api.mvc.{Action, ControllerComponents}
import services.EISService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class EISCallbackController @Inject() (
  cc: ControllerComponents,
  validateAuth: ValidateAuthTokenAction,
  eisService: EISService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def handleCRS(): Action[NodeSeq] = validateAuth(parse.xml).async { implicit request =>
    request.headers.get("x-conversation-id").map(_.trim) match {
      case Some(conversationId) =>
        eisService.processCRS(request.body, conversationId)
      case None =>
        logger.error(s"x-conversation-id is missing in the request header")
        Future.successful(BadRequest("x-conversation-id is missing in the request header"))
    }
  }

  def handleFatca(): Action[NodeSeq] = validateAuth(parse.xml).async { implicit request =>
    request.headers.get("x-conversation-id").map(_.trim) match {
      case Some(conversationId) =>
        eisService.processFatca(request.body, conversationId)
      case None =>
        logger.error("x-conversation-id is missing or empty in the request header")
        Future.successful(BadRequest("x-conversation-id is missing or empty in the request header"))
    }

  }
}
