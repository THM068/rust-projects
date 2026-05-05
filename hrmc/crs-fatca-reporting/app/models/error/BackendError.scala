/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.error

import play.api.libs.json.*
import uk.gov.hmrc.auth.core.AffinityGroup

abstract class BackendError private[error] (val detail: String)

final case class SdesSubmissionError(status: Int) extends BackendError(detail = s"SDES submission failed with status $status")
final case class RepositoryError(override val detail: String) extends BackendError(detail = detail)
final case class SubmissionServiceError(override val detail: String, userType: Option[AffinityGroup] = None) extends BackendError(detail = detail)
final case class ApiError(details: String) extends BackendError(detail = details)

object SubmissionServiceError {
  implicit val format: OFormat[SubmissionServiceError] = Json.format
}
