/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services.transform

import models.submission.{ConversationId, SubmissionMetaData}
import models.subscription.{ContactInformation, CrfaSubscriptionDetails, OrganisationDetails}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import services.validation.XMLValidationService

import java.time.ZonedDateTime
import scala.xml.*
import scala.concurrent.ExecutionContext.Implicits.global

class CrsTransformServiceSpec extends AnyFreeSpec with Matchers {
  private val xmlValidationService = new XMLValidationService()
  private val service              = new CrsTransformService(xmlValidationService)

  private def loadXml(path: String): Elem = XML.load(getClass.getResourceAsStream(path))

  "CrsTransformService.transformAndValidate" - {

    "should wrap uploaded CRS payload into CRSSubmissionRequest with expected structure" in {
      val uploadedXml: Elem = loadXml("/transform/input-crs-xml.xml")

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
        secondaryContact = None
      )

      val result = service.transformAndValidate(uploadedXml, meta, subscription)

      result.isRight shouldBe true

      val xml = result.toOption.get

      val payloadRoot = (xml \ "requestDetail").head.child.collectFirst { case e: Elem => e }.get
      payloadRoot.scope.getURI("crs") shouldBe "urn:oecd:ties:crs:v3"
    }
  }
}
