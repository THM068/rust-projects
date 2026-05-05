/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.validation

import models.submission.MessageSpecData
import play.api.libs.json._

sealed trait SubmissionValidationResult

case class SubmissionValidationSuccess(messageSpecData: MessageSpecData) extends SubmissionValidationResult
object SubmissionValidationSuccess {
  implicit val format: OFormat[SubmissionValidationSuccess] = {
    val base = Json.format[SubmissionValidationSuccess]
    OFormat(
      base, // reads stays the same
      OWrites[SubmissionValidationSuccess](s => base.writes(s) + ("type" -> JsString("Success")))
    )
  }
}

case class SubmissionValidationFailure(validationErrors: ValidationErrors, messageType: String) extends SubmissionValidationResult
object SubmissionValidationFailure {
  implicit val format: OFormat[SubmissionValidationFailure] = {
    val base = Json.format[SubmissionValidationFailure]
    OFormat(
      base,
      OWrites[SubmissionValidationFailure](f => base.writes(f) + ("type" -> JsString("ValidationFailure")))
    )
  }
}

case class InvalidXmlError(saxException: String) extends SubmissionValidationResult {
  override def toString: String = s"Invalid XML - $saxException"
}
object InvalidXmlError {
  implicit val format: OFormat[InvalidXmlError] = {
    val base = Json.format[InvalidXmlError]
    OFormat(
      base,
      OWrites[InvalidXmlError](e => base.writes(e) + ("type" -> JsString("InvalidXml")))
    )
  }
}

case class FIIDDoesNotMatchSendCompanyInError(error: String) extends SubmissionValidationResult

object FIIDDoesNotMatchSendCompanyInError {
  implicit val format: OFormat[FIIDDoesNotMatchSendCompanyInError] = {
    val base = Json.format[FIIDDoesNotMatchSendCompanyInError]
    OFormat(
      base,
      OWrites[FIIDDoesNotMatchSendCompanyInError](e => base.writes(e) + ("type" -> JsString("InvalidFIID")))
    )
  }
}

case class InvalidReportingPeriodError(error: String) extends SubmissionValidationResult

object InvalidReportingPeriodError {
  implicit val format: OFormat[InvalidReportingPeriodError] = {
    val base = Json.format[InvalidReportingPeriodError]
    OFormat(
      base,
      OWrites[InvalidReportingPeriodError](e => base.writes(e) + ("type" -> JsString("InvalidReportingPeriod")))
    )
  }
}

case class InvalidMessageTypeError(error: String = "Invalid message type") extends SubmissionValidationResult
object InvalidMessageTypeError {
  implicit val format: OFormat[InvalidMessageTypeError] = new OFormat[InvalidMessageTypeError] {
    def reads(json: JsValue): JsResult[InvalidMessageTypeError] =
      JsSuccess(InvalidMessageTypeError())
    def writes(o: InvalidMessageTypeError): JsObject =
      Json.obj(
        "error" -> JsString(o.error),
        "type"  -> JsString("InvalidMessageType")
      )
  }
}

case class ValidationErrors(errors: Seq[GenericError])
object ValidationErrors {
  implicit val format: OFormat[ValidationErrors] = Json.format[ValidationErrors]
}

case class ViewElectionErrors(error: String) extends SubmissionValidationResult

object SubmissionValidationResult {
  private val successFmt                         = SubmissionValidationSuccess.format
  private val failureFmt                         = SubmissionValidationFailure.format
  private val invalidXmlFmt                      = InvalidXmlError.format
  private val invalidReportingPeriod             = InvalidReportingPeriodError.format
  private val fIIDDoesNotMatchSendCompanyInError = FIIDDoesNotMatchSendCompanyInError.format
  private val invalidMessageTypeError            = InvalidMessageTypeError.format

  implicit val format: OFormat[SubmissionValidationResult] = {
    val reads: Reads[SubmissionValidationResult] =
      (__ \ "type").read[String].flatMap {
        case "Success"                => Reads(js => successFmt.reads(js))
        case "ValidationFailure"      => Reads(js => failureFmt.reads(js))
        case "InvalidXml"             => Reads(js => invalidXmlFmt.reads(js))
        case "InvalidMessageType"     => Reads(_ => JsSuccess(InvalidMessageTypeError()))
        case "InvalidReportingPeriod" => Reads(js => invalidReportingPeriod.reads(js))
        case "InvalidFIID"            => Reads(js => fIIDDoesNotMatchSendCompanyInError.reads(js))
        case other                    => Reads(_ => JsError(s"Unknown SubmissionValidationResult type '$other'"))
      }

    val writes: OWrites[SubmissionValidationResult] = OWrites {
      case s: SubmissionValidationSuccess        => successFmt.writes(s)
      case f: SubmissionValidationFailure        => failureFmt.writes(f)
      case e: InvalidXmlError                    => invalidXmlFmt.writes(e)
      case e: InvalidReportingPeriodError        => invalidReportingPeriod.writes(e)
      case e: FIIDDoesNotMatchSendCompanyInError => fIIDDoesNotMatchSendCompanyInError.writes(e)
      case _                                     => invalidMessageTypeError.writes(InvalidMessageTypeError())
    }

    OFormat(reads, writes)
  }
}
