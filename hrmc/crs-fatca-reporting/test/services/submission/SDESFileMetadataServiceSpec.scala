/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services.submission

import base.SpecBase
import generators.Generators
import models.submission.{ConversationId, MessageType}
import models.subscription.CrfaSubscriptionDetails
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SDESFileMetadataServiceSpec extends SpecBase with MockitoSugar with ScalaCheckDrivenPropertyChecks with Generators {

  private val sdesFileMetaDataService = app.injector.instanceOf[SDESFileMetadataService]
  private val dateTime                = LocalDateTime.now(fixedClock)

  "SDESMetaDataServiceSpec" - {
    "compileMetaData" - {
      "must compile metadata for an organisation" in {
        forAll { (responseDetail: CrfaSubscriptionDetails, conversationId: ConversationId, fileName: String, regime: MessageType) =>
          val result = sdesFileMetaDataService.compileMetaData(responseDetail, conversationId, dateTime, fileName, regime)

          val primaryContact        = responseDetail.primaryContact
          val maybeSecondaryContact = responseDetail.secondaryContact

          result.get("requestCommon/conversationID").value mustBe conversationId.value
          result.get("requestCommon/receiptDate").value mustBe dateTime.format(DateTimeFormatter.ISO_DATE_TIME)
          result.get("requestCommon/schemaVersion").value mustBe "1.0.0"
          result.get("requestCommon/regime").value mustBe regime.toString

          result.get("requestAdditionalDetail/primaryContact/emailAddress").value mustBe primaryContact.email
          result.get("requestAdditionalDetail/primaryContact/organisationDetails/organisationName").value mustBe (primaryContact.contactInformation match {
            case e: models.subscription.OrganisationDetails => e.name
            case _                                          => fail()
          })
          result.get("requestAdditionalDetail/fileName").value mustBe fileName
          result.get("requestAdditionalDetail/subscriptionID").value mustBe responseDetail.crfaReference
          result.get("requestAdditionalDetail/isGBUser").value mustBe responseDetail.gbUser.toString

          result.get("requestAdditionalDetail/tradingName") mustBe responseDetail.tradingName
          result.get("requestAdditionalDetail/primaryContact/mobileNumber") mustBe primaryContact.mobile
          result.get("requestAdditionalDetail/primaryContact/phoneNumber") mustBe primaryContact.phone

          result.get("requestAdditionalDetail/secondaryContact/phoneNumber") mustBe maybeSecondaryContact.flatMap(_.phone)
          result.get("requestAdditionalDetail/secondaryContact/emailAddress") mustBe maybeSecondaryContact.map(_.email)
          result.get("requestAdditionalDetail/secondaryContact/mobileNumber") mustBe maybeSecondaryContact.flatMap(_.mobile)
          result.get("requestAdditionalDetail/secondaryContact/organisationDetails/organisationName") mustBe maybeSecondaryContact.map(
            _.contactInformation match {
              case e: models.subscription.OrganisationDetails =>
                e.name
              case _ => fail()
            }
          )
        }
      }
    }
  }

}
