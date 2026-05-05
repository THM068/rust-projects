/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers

import models.update.GiinUpdateRequest
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.mvc.{Action, ControllerComponents}
import services.FinancialInstitutionsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class GiinUpdateController @Inject() (cc: ControllerComponents, fiService: FinancialInstitutionsService)(implicit
  ec: ExecutionContext
) extends BackendController(cc)
    with Logging {

  def updateGiin: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[GiinUpdateRequest] match {
      case JsSuccess(updateRequest, _) =>
        fiService
          .updateFiWithGiin(updateRequest.subscriptionId, updateRequest.fiid, updateRequest.giin)
          .map(_ => NoContent)
          .recover { case NonFatal(e) =>
            logger.error(
              s"Error updating GIIN for FIID ${updateRequest.fiid}: ${e.getMessage}",
              e
            )
            InternalServerError
          }

      case JsError(errors) =>
        logger.warn(s"Invalid JSON payload for GiinUpdateRequest: $errors")
        Future.successful(BadRequest(s"Invalid request body: $errors"))
    }
  }
}
