/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package services.transform

import models.submission.{ConversationId, SubmissionMetaData}
import models.subscription.{ContactInformation, CrfaSubscriptionDetails, OrganisationDetails}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import services.validation.XMLValidationService

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.*

class FatcaTransformServiceSpec extends AnyFreeSpec with Matchers {
  private val xmlValidationService = new XMLValidationService()
  private val service              = new FatcaTransformService(xmlValidationService)

  private def loadXml(path: String): Elem = XML.load(getClass.getResourceAsStream(path))

  "FatcaTransformService.transformAndValidate" - {
    "should wrap uploaded Fatca payload into FATCASubmissionRequest with expected structure" in {
      val uploadedXml: Elem = loadXml("/transform/input-fatca-xml.xml")

      val meta = SubmissionMetaData(
        submissionTime = ZonedDateTime.now(),
        conversationId = ConversationId("test-conversation-id"),
        fileName = Some("test-file.xml")
      )

      val subscription = CrfaSubscriptionDetails(
        crfaReference = "CRFA123456789",
        tradingName = Some("Test Trading Name"),
        gbUser = true,
        primaryContact = ContactInformation(
          phone = Some("0123456789"),
          mobile = None,
          email = "primary@test.com",
          contactInformation = OrganisationDetails("Test Org Ltd")
        ),
        secondaryContact = Some(
          ContactInformation(
            phone = Some("0123456789"),
            mobile = None,
            email = "primary@test.com",
            contactInformation = OrganisationDetails("Test Org Ltd")
          )
        )
      )

      val result = service.transformAndValidate(uploadedXml, meta, subscription)

      result.isRight shouldBe true
      val xml = result.toOption.get

      val payloadRoot = (xml \\ "FATCASubmissionRequest").head
      // namespace assertions
      nameSpaceAttr(payloadRoot, "fatca") shouldBe "urn:oecd:ties:fatca:v2"
      nameSpaceAttr(payloadRoot, "oecd") shouldBe "urn:oecd:ties:stf:v2"
      nameSpaceAttr(payloadRoot, "iso") shouldBe "urn:oecd:ties:commontypesfatcacrs:v2"
      nameSpaceAttr(payloadRoot, "xsi") shouldBe "http://www.w3.org/2001/XMLSchema-instance"
      val schemaLocation = payloadRoot \@ "{http://www.w3.org/2001/XMLSchema-instance}schemaLocation"
      schemaLocations.foreach { loc =>
        schemaLocation should include(loc)
      }

      // requestCommon assertions
      val requestCommon = (payloadRoot \\ "requestCommon").head
      (requestCommon \\ "receiptDate").text shouldBe meta.submissionTime.format(DateTimeFormatter.ISO_INSTANT)
      (requestCommon \\ "regime").text shouldBe "FATCA"
      (requestCommon \\ "schemaVersion").text shouldBe "2.0"
      (requestCommon \\ "conversationID").text shouldBe meta.conversationId.value

      // requestDetail assertions
      val requestDetails = (xml \ "requestDetail").head.child.collectFirst { case e: Elem => e }.get
      requestDetails.scope.getURI("fatca") shouldBe "urn:oecd:ties:fatca:v2"

      // requestAdditionalDetail assertions
      val requestAdditionalDetail = (payloadRoot \\ "requestAdditionalDetail").head
      (requestAdditionalDetail \\ "fileName").text shouldBe meta.fileName.get
      (requestAdditionalDetail \\ "subscriptionID").text shouldBe subscription.crfaReference
      (requestAdditionalDetail \\ "tradingName").text shouldBe subscription.tradingName.get
      (requestAdditionalDetail \\ "isManual").text shouldBe "true"
      (requestAdditionalDetail \\ "isGBUser").text shouldBe subscription.gbUser.toString

      val primaryContact = (requestAdditionalDetail \\ "primaryContact").head
      (primaryContact \\ "phoneNumber").text shouldBe subscription.primaryContact.phone.get
      (primaryContact \\ "mobileNumber").isEmpty shouldBe true
      (primaryContact \\ "emailAddress").text shouldBe subscription.primaryContact.email

      val primaryOrganisationDetails = (primaryContact \\ "organisationDetails").head
      val primaryOrgName = subscription.primaryContact.contactInformation match {
        case OrganisationDetails(name) => name
        case _                         => fail("Expected OrganisationDetails in primary contact")
      }
      (primaryOrganisationDetails \\ "organisationName").text shouldBe primaryOrgName

      val secondaryContact: Node = (requestAdditionalDetail \\ "secondaryContact").head
      (secondaryContact \\ "phoneNumber").text shouldBe subscription.secondaryContact.get.phone.get
      (secondaryContact \\ "mobileNumber").isEmpty shouldBe true
      (secondaryContact \\ "emailAddress").text shouldBe subscription.secondaryContact.get.email

      val secondaryOrganisationDetails = (secondaryContact \\ "organisationDetails").head
      val secondaryOrgName = subscription.secondaryContact.get.contactInformation match {
        case OrganisationDetails(name) => name
        case _                         => fail("Expected OrganisationDetails in primary contact")
      }
      (secondaryOrganisationDetails \\ "organisationName").text shouldBe secondaryOrgName

    }
  }

  private def nameSpaceAttr(node: Node, name: String): String =
    node.scope.getURI(name)

  private def attr(node: Node, name: String): String =
    node.attribute(name).map(_.text).getOrElse("")

  private def schemaLocations = Seq(
    "urn:oecd:ties:fatca:v2 FatcaXML_v2.0.xsd",
    "urn:oecd:ties:commontypesfatcacrs:v2 isofatcatypes_v1.1.xsd",
    "urn:oecd:ties:stf:v2 stffatcatypes_v2.0.xsd",
    "urn:oecd:ties:oecdtypes:v4 oecdtypes_v4.2.xsd"
  )
}
