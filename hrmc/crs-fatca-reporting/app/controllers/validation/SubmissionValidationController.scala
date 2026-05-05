/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.validation
import models.upscan.ValidateRequest
import models.validation._
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import services.validation.UploadedXmlValidationEngine
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionValidationController @Inject() (cc: ControllerComponents, validationEngine: UploadedXmlValidationEngine)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def validateSubmission: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[ValidateRequest] match {
      case JsSuccess(validateRequest, _) =>
        validationEngine.validateUploadSubmission(validateRequest.url, validateRequest.subscriptionId) map {
          case submissionValidationSuccess @ SubmissionValidationSuccess(_) =>
            Ok(Json.toJson(submissionValidationSuccess))
          case InvalidXmlError(saxException) =>
            logger.error(s"InvalidXmlError: $saxException")
            BadRequest(Json.toJson(InvalidXmlError("Invalid xml provided")))
          case ViewElectionErrors(_) =>
            InternalServerError("Error during election view processing")
          case result =>
            logger.warn(s"File Validation Result: $result")
            Ok(Json.toJson(result))
        }
      case JsError(errors) =>
        logger.warn(s"Missing upscan URL: $errors")
        Future.successful(InternalServerError("Missing upscan URL"))
    }
  }
}
