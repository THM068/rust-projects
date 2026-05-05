/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.elections

import play.api.libs.json.*
import utils.json.OpaqueFormatOps.*

private enum YesNoNa:
  case Yes, No, NA

object YesNoNa:
  given Format[YesNoNa] = new Format[YesNoNa]:
    def writes(v: YesNoNa): JsValue = JsString(
      v match
        case YesNoNa.Yes => "yes"
        case YesNoNa.No  => "no"
        case YesNoNa.NA  => "na"
    )

    def reads(json: JsValue): JsResult[YesNoNa] =
      json.validate[String].map(_.toLowerCase) match
        case JsSuccess("yes", _) => JsSuccess(YesNoNa.Yes)
        case JsSuccess("no", _)  => JsSuccess(YesNoNa.No)
        case JsSuccess("na", _)  => JsSuccess(YesNoNa.NA)
        case _                   => JsError("Expected yes|no|na")

  given Conversion[Option[Boolean], YesNoNa] with
    def apply(opt: Option[Boolean]): YesNoNa =
      opt match
        case Some(true)  => YesNoNa.Yes
        case Some(false) => YesNoNa.No
        case None        => YesNoNa.NA

opaque type HasThresholds = YesNoNa
object HasThresholds:
  def apply(v: YesNoNa): HasThresholds = v

opaque type HasTreasuryRegulations = YesNoNa
object HasTreasuryRegulations:
  def apply(v: YesNoNa): HasTreasuryRegulations = v

opaque type HasCARF = YesNoNa
object HasCARF:
  def apply(v: YesNoNa): HasCARF = v

opaque type HasContracts = YesNoNa
object HasContracts:
  def apply(v: YesNoNa): HasContracts = v

opaque type HasDormantAccounts = YesNoNa
object HasDormantAccounts:
  def apply(v: YesNoNa): HasDormantAccounts = v

opaque type FIID = String
object FIID:
  def apply(v: String): FIID = v

opaque type ReportingPeriod = String
object ReportingPeriod:
  def apply(v: String): ReportingPeriod = v

opaque type OriginatingSystem = String
object OriginatingSystem:
  def apply(v: String): OriginatingSystem = v

opaque type Regime = String
object Regime:
  def apply(v: String): Regime = v

opaque type ParamName = String
object ParamName:
  def apply(v: String): ParamName = v

opaque type ParamValue = String
object ParamValue:
  def apply(v: String): ParamValue = v

opaque type RequestType = String
object RequestType:
  def apply(v: String): RequestType = v

opaque type TransmittingSystem = String
object TransmittingSystem:
  def apply(v: String): TransmittingSystem = v

object OpaqueFormats:
  given Format[HasThresholds]          = summon[Format[YesNoNa]].asOpaque
  given Format[HasTreasuryRegulations] = summon[Format[YesNoNa]].asOpaque
  given Format[HasCARF]                = summon[Format[YesNoNa]].asOpaque
  given Format[HasContracts]           = summon[Format[YesNoNa]].asOpaque
  given Format[HasDormantAccounts]     = summon[Format[YesNoNa]].asOpaque
  given Format[FIID]                   = summon[Format[String]].asOpaque
  given Format[ReportingPeriod]        = summon[Format[String]].asOpaque
  given Format[OriginatingSystem]      = summon[Format[String]].asOpaque
  given Format[Regime]                 = summon[Format[String]].asOpaque
  given Format[ParamName]              = summon[Format[String]].asOpaque
  given Format[ParamValue]             = summon[Format[String]].asOpaque
  given Format[RequestType]            = summon[Format[String]].asOpaque
  given Format[TransmittingSystem]     = summon[Format[String]].asOpaque

case class RequestParameter(paramName: ParamName, paramValue: ParamValue)

object RequestParameter:
  import OpaqueFormats.given
  given OFormat[RequestParameter] = Json.format[RequestParameter]

case class RequestElectionsCommon(
  originatingSystem: OriginatingSystem,
  regime: Regime,
  requestParameters: Seq[RequestParameter],
  requestType: RequestType,
  transmittingSystem: TransmittingSystem
)

case class CRS(hasCARF: HasCARF, hasContracts: HasContracts, hasDormantAccounts: HasDormantAccounts, hasThresholds: HasThresholds)
object CRS:
  import OpaqueFormats.given
  given OFormat[CRS] = Json.format[CRS]

  given Conversion[CrsElectionsDetails, CRS] with
    def apply(r: CrsElectionsDetails): CRS = CRS(
      hasCARF = HasCARF(r.hasCARF),
      hasContracts = HasContracts(r.hasContracts),
      hasDormantAccounts = HasDormantAccounts(r.hasDormantAccounts),
      hasThresholds = HasThresholds(r.hasThresholds)
    )

case class FATCA(hasThresholds: HasThresholds, hasTreasuryRegulations: HasTreasuryRegulations)
object FATCA:
  import OpaqueFormats.given
  given OFormat[FATCA] = Json.format[FATCA]

  given Conversion[FatcaElectionsDetails, FATCA] with
    def apply(r: FatcaElectionsDetails): FATCA = FATCA(
      hasThresholds = HasThresholds(r.hasThresholds),
      hasTreasuryRegulations = HasTreasuryRegulations(r.hasTreasuryRegulations)
    )

object RequestElectionsCommon:
  import OpaqueFormats.given
  given OFormat[RequestElectionsCommon] = Json.format[RequestElectionsCommon]

  val CREATE = RequestElectionsCommon(
    originatingSystem = OriginatingSystem("MDTP"),
    regime = Regime("CRFA"),
    requestParameters = Seq.empty,
    requestType = RequestType("CREATE"),
    transmittingSystem = TransmittingSystem("EIS")
  )

case class ElectionDetails(crs: Option[CRS], fatca: Option[FATCA], reportingPeriod: ReportingPeriod)
object ElectionDetails:
  import OpaqueFormats.given
  given OFormat[ElectionDetails] = Json.format[ElectionDetails]

case class ElectionRequestDetails(crs: Option[CRS], fatca: Option[FATCA], fiId: FIID, reportingPeriod: ReportingPeriod)

object ElectionRequestDetails:
  import OpaqueFormats.given
  given OFormat[ElectionRequestDetails] = Json.format[ElectionRequestDetails]

case class ElectionsRequest(requestCommon: RequestElectionsCommon, requestDetails: ElectionRequestDetails)

object ElectionsRequest:
  given OFormat[ElectionsRequest] = Json.format[ElectionsRequest]

  given Conversion[ElectionsSubmissionDetails, ElectionsRequest] with
    def apply(r: ElectionsSubmissionDetails): ElectionsRequest = ElectionsRequest(
      requestCommon = RequestElectionsCommon.CREATE,
      requestDetails = ElectionRequestDetails(
        crs = r.crsDetails match {
          case Some(crs: CrsElectionsDetails) => Some(crs)
          case None                           => None
        },
        fatca = r.fatcaDetails match {
          case Some(fatca: FatcaElectionsDetails) => Some(fatca)
          case None                               => None
        },
        fiId = FIID(r.fiId),
        reportingPeriod = ReportingPeriod(r.reportingPeriod)
      )
    )
