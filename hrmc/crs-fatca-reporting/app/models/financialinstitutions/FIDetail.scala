/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.financialinstitutions

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

sealed trait BaseFIDetail {
  val FIName: String
  val SubscriptionID: String
  val TINDetails: Seq[TINDetails]
  val GIIN: Option[String]
  val IsFIUser: Boolean
  val AddressDetails: AddressDetails
  val PrimaryContactDetails: Option[ContactDetails]
}

object BaseFIDetail {
  implicit val format: OFormat[BaseFIDetail] = Json.format[BaseFIDetail]
}

final case class FIDetail(
  FIID: String,
  FIName: String,
  SubscriptionID: String,
  TINDetails: Seq[TINDetails],
  GIIN: Option[String],
  IsFIUser: Boolean,
  IsFATCAReporting: Boolean,
  AddressDetails: AddressDetails,
  PrimaryContactDetails: Option[ContactDetails],
  SecondaryContactDetails: Option[ContactDetails]
) extends BaseFIDetail

object FIDetail {

  implicit val reads: Reads[FIDetail] = (
    (JsPath \ "FIID").read[String] and
      (JsPath \ "FIName").read[String] and
      (JsPath \ "SubscriptionID").read[String] and
      (JsPath \ "TINDetails").readNullable[Seq[TINDetails]].map(_.getOrElse(Seq.empty)) and
      (JsPath \ "GIIN").readNullable[String] and
      (JsPath \ "IsFIUser").read[Boolean] and
      (JsPath \ "IsFATCAReporting").read[Boolean] and
      (JsPath \ "AddressDetails").read[AddressDetails] and
      (JsPath \ "PrimaryContactDetails").readNullable[ContactDetails] and
      (JsPath \ "SecondaryContactDetails").readNullable[ContactDetails]
  )(FIDetail.apply _)

  implicit val writes: OWrites[FIDetail] = Json.writes[FIDetail]
  implicit val format: OFormat[FIDetail] = OFormat(reads, writes)
}

final case class RemoveFIDetail(
  SubscriptionID: String,
  FIID: String
)

object AddressDetails {
  implicit val format: OFormat[AddressDetails] = Json.format[AddressDetails]
}

final case class AddressDetails(
  AddressLine1: String,
  AddressLine2: Option[String],
  AddressLine3: Option[String],
  AddressLine4: Option[String],
  CountryCode: Option[String],
  PostalCode: Option[String]
)

final case class ContactDetails(ContactName: String, EmailAddress: String, PhoneNumber: Option[String])

object ContactDetails {
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}

final case class CreateFIDetails(
  FIName: String,
  SubscriptionID: String,
  TINDetails: Seq[TINDetails],
  GIIN: Option[String],
  IsFIUser: Boolean,
  AddressDetails: AddressDetails,
  PrimaryContactDetails: Option[ContactDetails],
  SecondaryContactDetails: Option[ContactDetails]
) extends BaseFIDetail

object CreateFIDetails {
  implicit val format: OFormat[CreateFIDetails] = Json.format[CreateFIDetails]
}

enum TINType:
  case UTR, CRN, TURN

object TINType {
  private val allValues: Seq[TINType] = TINType.values.toSeq

  private val stringMapping: Map[String, TINType]  = allValues.map(v => v.toString -> v).toMap
  private val reverseMapping: Map[TINType, String] = stringMapping.map(_.swap)

  given Format[TINType] = new Format[TINType] {

    override def reads(json: JsValue): JsResult[TINType] = json match {
      case JsString(str) =>
        stringMapping
          .get(str)
          .map(JsSuccess(_))
          .getOrElse(JsError(s"Invalid TINType: $str"))
      case _ => JsError("Invalid TINType")
    }

    override def writes(o: TINType): JsValue =
      JsString(reverseMapping(o))
  }
}

final case class TINDetails(TINType: TINType, TIN: String, IssuedBy: String)

object TINDetails {
  implicit val format: OFormat[TINDetails] = Json.format[TINDetails]
}
