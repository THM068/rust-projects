/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package controllers.upscan

import models.upscan.CallbackBody
import play.api.i18n.MessagesApi
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import services.upscan.UpScanCallbackDispatcher
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UploadCallbackController @Inject() (
  val upscanCallbackDispatcher: UpScanCallbackDispatcher,
  cc: ControllerComponents,
  implicit override val messagesApi: MessagesApi
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  val callback: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val callback = request.body.validate[CallbackBody]
    callback.fold(
      _ => Future.successful(BadRequest("Invalid callback body")),
      validCallback =>
        upscanCallbackDispatcher
          .handleCallback(validCallback)
          .map(_ => Ok)
    )
  }
}
