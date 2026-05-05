/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.elections

import play.api.libs.json.{Json, OFormat}

case class CrsElectionsDetails(hasCARF: Option[Boolean], hasContracts: Option[Boolean], hasDormantAccounts: Option[Boolean], hasThresholds: Option[Boolean])
object CrsElectionsDetails:
  given OFormat[CrsElectionsDetails] = Json.format[CrsElectionsDetails]

case class FatcaElectionsDetails(hasThresholds: Option[Boolean], hasTreasuryRegulations: Option[Boolean])
object FatcaElectionsDetails:
  given OFormat[FatcaElectionsDetails] = Json.format[FatcaElectionsDetails]

case class ElectionsSubmissionDetails(
  fiId: String,
  reportingPeriod: String,
  crsDetails: Option[CrsElectionsDetails],
  fatcaDetails: Option[FatcaElectionsDetails]
)

object ElectionsSubmissionDetails:
  given OFormat[ElectionsSubmissionDetails] = Json.format[ElectionsSubmissionDetails]

sealed trait ElectionsSubmissionResult
case object SubmissionSuccess extends ElectionsSubmissionResult
case class SubmissionError(message: String) extends ElectionsSubmissionResult
