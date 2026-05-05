/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers

import models.elections.{ElectionsSubmissionDetails, SubmissionError, SubmissionSuccess}
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.ElectionsSubmissionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class CADXElectionsController @Inject() (
  cc: ControllerComponents,
  electionsSubmissionService: ElectionsSubmissionService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def submitElections(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.map(_.validate[ElectionsSubmissionDetails]) match {

      case Some(JsSuccess(electionsBody, _)) =>
        electionsSubmissionService
          .submitElections(electionsBody)
          .map {
            case SubmissionSuccess =>
              NoContent

            case SubmissionError(message) =>
              InternalServerError(message)
          }
      case Some(JsError(errors)) =>
        val errorString = JsError.toJson(errors).toString()
        Future.successful(BadRequest(s"Invalid request body format: $errorString"))

      case _ =>
        Future.successful(BadRequest("Request body missing or not valid JSON."))
    }
  }
}
