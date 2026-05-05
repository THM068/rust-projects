/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services.submission

import models.submission.{ConversationId, MessageType}
import models.subscription.{ContactInformation, CrfaSubscriptionDetails}
import play.api.Logging

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SDESFileMetadataService extends Logging {
  private val formatter = DateTimeFormatter.ISO_DATE_TIME

  def compileMetaData(subscriptionDetails: CrfaSubscriptionDetails,
                      conversationID: ConversationId,
                      submissionTime: LocalDateTime,
                      fileName: String,
                      regime: MessageType
  ): Map[String, String] = {
    val primaryContact = transformContactInformation(subscriptionDetails.primaryContact, "primaryContact")
    val secondaryContact = subscriptionDetails.secondaryContact
      .map(transformContactInformation(_, "secondaryContact"))
      .getOrElse(Map.empty)
    val tradingName = subscriptionDetails.tradingName
      .map(tradingName => Map("requestAdditionalDetail/tradingName" -> tradingName))
      .getOrElse(Map.empty)

    tradingName ++ Map(
      "requestCommon/conversationID"           -> conversationID.value,
      "requestCommon/receiptDate"              -> submissionTime.format(formatter),
      "requestCommon/regime"                   -> regime.toDisplayString,
      "requestCommon/schemaVersion"            -> "1.0.0",
      "requestAdditionalDetail/fileName"       -> fileName,
      "requestAdditionalDetail/subscriptionID" -> subscriptionDetails.crfaReference,
      "requestAdditionalDetail/isGBUser"       -> subscriptionDetails.gbUser.toString
    ) ++ primaryContact ++ secondaryContact
  }

  private def transformContactInformation(contactInformation: ContactInformation, contactType: String): Map[String, String] = {
    val organisationName = contactInformation.contactInformation match {
      case orgDetails: models.subscription.OrganisationDetails => orgDetails.name
      case _                                                   => ""
    }
    val contactName  = Map(s"requestAdditionalDetail/$contactType/organisationDetails/organisationName" -> organisationName)
    val phoneNumber  = contactInformation.phone.map(phone => Map(s"requestAdditionalDetail/$contactType/phoneNumber" -> phone))
    val mobileNumber = contactInformation.mobile.map(mobile => Map(s"requestAdditionalDetail/$contactType/mobileNumber" -> mobile))
    val email        = Map(s"requestAdditionalDetail/$contactType/emailAddress" -> contactInformation.email)

    contactName ++ email ++ phoneNumber.getOrElse(Map.empty) ++ mobileNumber.getOrElse(Map.empty)

  }

}
