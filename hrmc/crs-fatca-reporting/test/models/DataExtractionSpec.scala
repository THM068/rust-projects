/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models
import base.SpecBase
import models.submission.MessageType.{CRS, FATCA}
import models.validation.InvalidMessageTypeError
import org.scalatest.matchers.must.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.xml.{Elem, XML}

class DataExtractionSpec extends SpecBase {

  val service: DataExtraction = new DataExtraction()

  "DataExtraction" - {

    "parseMessageType" - {

      "must return Right(CRS) when the MessageType is CRS" in new TestContext {
        val xml: Elem = selectValidXml("CRS")
        val result    = Await.result(service.parseMessageType(xml), 5.seconds)
        result mustBe Right(CRS)
      }

      "must return Right(FATCA) when the MessageType is FATCA" in new TestContext {
        val xml: Elem = selectValidXml("FATCA")
        val result    = Await.result(service.parseMessageType(xml), 5.seconds)
        result mustBe Right(FATCA)
      }

      "must handle different casing and return Right(CRS) for 'crs'" in new TestContext {
        val xml: Elem = generateXml("crs")
        val result    = Await.result(service.parseMessageType(xml), 5.seconds)
        result mustBe Right(CRS)
      }

      "must return Left(InvalidMessageTypeError) for an unknown MessageType" in new TestContext {
        val xml: Elem = generateXml("Unknown")
        val result    = Await.result(service.parseMessageType(xml), 5.seconds)
        result mustBe Left(InvalidMessageTypeError())
      }

      "must return Left(InvalidMessageTypeError) if MessageType is missing" in {
        val xml: Elem = <SomeOtherTag>Hello</SomeOtherTag>
        val result    = Await.result(service.parseMessageType(xml), 5.seconds)
        result mustBe Left(InvalidMessageTypeError())
      }

    }

    "messageSpecData" - {
      "must return Some(MessageSpecData) when given valid xml" in new TestContext {
        val input = Table(
          ("messageType", "expectedMessageType", "sendingCompanyIN", "messageRefId", "reportingFIName", "reportingPeriod"),
          ("CRS", CRS, "FAT206362799315", "GB2013CA123456789", "Test FI Name", LocalDate.of(2014, 8, 13)),
          ("FATCA", FATCA, "a", "message-ref-id", "a", LocalDate.of(9999, 1, 1))
        )
        forAll(input) { (messageType, expectedMessageType, sendingCompanyIN, messageRefId, reportingFIName, reportingPeriod) =>
          val xml    = selectValidXml(messageType)
          val result = service.extractRequiredElements(xml)
          result mustBe Some(XmlExtractedElements(expectedMessageType, sendingCompanyIN, messageRefId, reportingFIName, reportingPeriod))
        }
      }

      "must return None when given invalid xml" in {
        val xml: Elem =
          <file>
            <MessageTypeIndic></MessageTypeIndic>
            <ReportingEntity>
              <Entity>
                <Name>Name</Name>
              </Entity>
            </ReportingEntity>
          </file>

        service.extractRequiredElements(xml) mustBe None
      }
    }

    trait TestContext {
      def selectValidXml(messageType: String): Elem = messageType match {
        case "CRS"   => XML.loadFile("test/resources/valid-crs-xml.xml")
        case "FATCA" => XML.loadFile("test/resources/valid-fatca-xml.xml")
        case _       => fail("Unsupported message type for XML generation")
      }

      def generateXml(messageType: String): Elem =
        <ftc:FATCA_OECD xmlns:ftc="urn:oecd:ties:fatca:v2" xmlns:sfa="urn:oecd:ties:stffatcatypes:v2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0" xsi:schemaLocation="urn:oecd:ties:fatca:v2 fatca.xsd">
          <ftc:MessageSpec>
            <sfa:SendingCompanyIN>a</sfa:SendingCompanyIN>
            <sfa:TransmittingCountry>SK</sfa:TransmittingCountry>
            <sfa:ReceivingCountry>DE</sfa:ReceivingCountry>
            <sfa:MessageType>{messageType}</sfa:MessageType>
            <sfa:Warning>a</sfa:Warning>
            <sfa:Contact>a</sfa:Contact>
            <sfa:MessageRefId>a</sfa:MessageRefId>
            <sfa:CorrMessageRefId>a</sfa:CorrMessageRefId>
            <sfa:CorrMessageRefId>a</sfa:CorrMessageRefId>
            <sfa:ReportingPeriod>9999-01-01</sfa:ReportingPeriod>
            <sfa:Timestamp>9999-12-30T23:59:59Z</sfa:Timestamp>
          </ftc:MessageSpec>
        </ftc:FATCA_OECD>
    }
  }
}
