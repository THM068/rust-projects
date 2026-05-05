/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import base.SpecBase
import models.validation.InvalidXmlError
import org.scalatest.matchers.must.Matchers._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import services.validation.XMLValidationService

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import java.io.File
import scala.xml.XML
class XMLValidationServiceSpec extends SpecBase {

  override lazy val app: Application = new GuiceApplicationBuilder().build()
  val service: XMLValidationService  = app.injector.instanceOf[XMLValidationService]

  "XMLValidationService" - {

    "must return a valid Elem and correctly parse the MessageType for CRS" in {
      val xmlFile = new File("test/resources/valid-crs-xml.xml").toURI.toURL.toString
      val result  = Await.result(service.loadXml(xmlFile), 10.seconds)

      result.isRight mustBe true
      val xmlElem = result.getOrElse(fail("Expected a valid XML element"))
      xmlElem.label mustBe "CRS_OECD"
      (xmlElem \\ "MessageType").text mustBe "CRS"
    }

    "must return a valid Elem and correctly parse the MessageType for FATCA" in {
      val xmlFile = new File("test/resources/valid-fatca-xml.xml").toURI.toURL.toString
      val result  = Await.result(service.loadXml(xmlFile), 10.seconds)

      result.isRight mustBe true
      val xmlElem = result.getOrElse(fail("Expected a valid XML element"))
      xmlElem.label mustBe "FATCA_OECD"
      (xmlElem \\ "MessageType").text mustBe "FATCA"
    }

    "must return a InvalidXmlError if the XML is not well-formed" in {
      val xmlFile = new File("test/resources/malformed-xml.xml").toURI.toURL.toString
      val result  = Await.result(service.loadXml(xmlFile), 10.seconds)

      result.isLeft mustBe true
      val validationError = result.left.value
      validationError mustBe a[InvalidXmlError]
      validationError.asInstanceOf[InvalidXmlError].toString must include("XML parsing failed")
    }

    "must return a InvalidXmlError if an exception is thrown" in {
      val invalidUrl = "http://invalid.url"
      val result     = Await.result(service.loadXml(invalidUrl), 10.seconds)

      result.isLeft mustBe true
      val validationError = result.left.value
      validationError mustBe a[InvalidXmlError]
      validationError.asInstanceOf[InvalidXmlError].toString must include("XML parsing failed")
    }

    "fatca validation" - {
      val fatcaXsdPath = "/xsd/fatca/FatcaXML_v2.0.xsd"

      "must correctly validate a submission" in {
        val service = app.injector.instanceOf[XMLValidationService]

        val validSubmission = XML.loadFile("test/resources/valid-fatca-xml.xml")

        val result = service.validate(validSubmission, fatcaXsdPath)

        result.isLeft mustBe false
      }

      "must correctly validate a submission with empty value" in {
        val service = app.injector.instanceOf[XMLValidationService]

        val validSubmission = XML.loadFile("test/resources/invalid/fatca/invalidfatca_empty.xml")

        val result = service.validate(validSubmission, fatcaXsdPath)

        result.isLeft mustBe true
      }
    }

    "crs validation" - {
      val crsXsdPath = "/xsd/crs/CrsXML_v3.0.xsd"

      "must correctly validate a submission" in {
        val service = app.injector.instanceOf[XMLValidationService]

        val validSubmission = XML.loadFile("test/resources/valid-crs-xml.xml")

        val result = service.validate(validSubmission, crsXsdPath)

        result.isLeft mustBe false
      }

      "must correctly validate a submission with empty value" in {
        val service = app.injector.instanceOf[XMLValidationService]

        val validSubmission = XML.loadFile("test/resources/invalid/crs/invalidcrs_empty.xml")

        val result = service.validate(validSubmission, crsXsdPath)

        result.isLeft mustBe true
      }
    }
  }
}
