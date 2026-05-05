/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services

import base.SpecBase
import config.AppConfig
import models.submission.*
import models.submission.MessageType.CRS
import models.validation.SaxParseError
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers.*
import play.api.mvc.Results.{BadRequest, InternalServerError, NoContent}
import repositories.submission.FileDetailsRepository
import services.validation.XMLValidationService

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.xml.{Elem, NodeSeq, Text}

class EISServiceSpec extends SpecBase {

  private val randomUUID                           = UUID.randomUUID()
  val xmlValidationService: XMLValidationService   = mock[XMLValidationService]
  val appConfig: AppConfig                         = mock[AppConfig]
  val fileDetailsRepository: FileDetailsRepository = mock[FileDetailsRepository]
  val service: EISService                          = new EISService(xmlValidationService, appConfig, fileDetailsRepository)

  override def beforeEach(): Unit = {
    reset(xmlValidationService, appConfig, fileDetailsRepository)
    super.beforeEach()
  }

  private val xml: NodeSeq =
    <gsm:BREResponse xmlns:gsm="http://www.hmrc.gsi.gov.uk/gsm">
      <requestCommon>
        <receiptDate>2025-12-17T09:30:47.400Z</receiptDate>
        <regime>AEOI</regime>
        <conversationID>
          {randomUUID}
        </conversationID>
        <schemaVersion>1.0.0</schemaVersion>
      </requestCommon>
      <requestDetail>
        <GenericStatusMessage>
          <ValidationErrors>
            <FileError>
              <Code>50008</Code>
              <Details>Invalid MessageRefID format</Details>
            </FileError>
            <FileError>
              <Code>50009</Code>
              <Details>MessageRefID has already been used</Details>
            </FileError>
            <RecordError>
              <Code>50010</Code>
              <Details>File Contains Test Data for Production Environment</Details>
              <DocRefIDInError>GB2022GBXACBC0000012345CBC40120231231093012UniqueCharacters_1234567890OECD10ENTUniqueCharactersForDocType</DocRefIDInError>
              <DocRefIDInError>GB2022GBXACBC0000012345CBC40120231231093012UniqueCharacters_1234567890OECD11REPUniqueCharactersForDocType</DocRefIDInError>
              <DocRefIDInError>GB2022GBXACBC0000012345CBC40120231231093012UniqueCharacters_1234567890OECD12ADDUniqueCharactersForDocType</DocRefIDInError>
            </RecordError>
          </ValidationErrors>
          <ValidationResult>
            <Status>Rejected</Status>
          </ValidationResult>
        </GenericStatusMessage>
      </requestDetail>
    </gsm:BREResponse>

  def fileDetails(mt: MessageType = CRS) = FileDetails(
    _id = ConversationId(randomUUID.toString),
    enrolmentId = "XACBC0000123456",
    messageRefId = "GBXACBC12345678",
    reportingEntityName = "Test Entity",
    messageType = mt,
    status = Pending,
    name = "test-file.xml",
    submitted = LocalDateTime.now(),
    lastUpdated = LocalDateTime.now(),
    reportingPeriod = LocalDate.now()
  )

  "EISService" - {
    "processCRS" - {
      "must NoContent Result when valid request received" in {

        when(appConfig.eisCRSFileUploadResponseXSDFilePath).thenReturn("/xsd/crs/EIS_File_Upload_Response.xsd")
        when(xmlValidationService.validate(any(), any())).thenReturn(Right(xml))
        when(fileDetailsRepository.updateStatus(any(), any())).thenReturn(Future.successful(Some(fileDetails)))

        val result = Await.result(service.processCRS(xml, randomUUID.toString), 1.seconds)
        result mustBe NoContent
      }

      "must BadRequest Result when request received failed in schema validation" in {
        val xml: NodeSeq = <test>"test"</test>

        when(appConfig.eisCRSFileUploadResponseXSDFilePath).thenReturn("/xsd/crs/EIS_File_Upload_Response.xsd")
        when(xmlValidationService.validate(any(), any())).thenReturn(Left(Seq(SaxParseError(1, "failed"))))

        val result = Await.result(service.processCRS(xml, randomUUID.toString), 1.seconds)
        result mustBe BadRequest("XML Schema validation failed")
      }

      "must BadRequest Result when request received failed in model conversion" in {
        val xml: NodeSeq = <test>"test"</test>

        when(appConfig.eisCRSFileUploadResponseXSDFilePath).thenReturn("/xsd/crs/EIS_File_Upload_Response.xsd")
        when(xmlValidationService.validate(any(), any())).thenReturn(Right(xml))

        val result = Await.result(service.processCRS(xml, randomUUID.toString), 1.seconds)
        result mustBe BadRequest("Failed to read XML")
      }

      "must return BadRequest when request conversationId does not match file content conversationId" in {
        val xmlConvIdMismatch =
          replaceChild(xml.asInstanceOf[Elem], "conversationID", "testId")

        when(appConfig.eisCRSFileUploadResponseXSDFilePath).thenReturn("/xsd/crs/EIS_File_Upload_Response.xsd")
        when(xmlValidationService.validate(any(), any())).thenReturn(Right(xmlConvIdMismatch))

        val result = Await.result(service.processCRS(xmlConvIdMismatch, "mismatch-id"), 1.second)

        result mustBe BadRequest("Conversation ID does not match request header & file content")
      }

      "must InternalServer Result when unable to update file status" in {
        when(appConfig.eisCRSFileUploadResponseXSDFilePath).thenReturn("/xsd/crs/EIS_File_Upload_Response.xsd")
        when(xmlValidationService.validate(any(), any())).thenReturn(Right(xml))
        when(fileDetailsRepository.updateStatus(any(), any())).thenReturn(Future.successful(None))

        val result = Await.result(service.processCRS(xml, randomUUID.toString), 1.seconds)
        result mustBe InternalServerError
      }

    }
    "processFatca" - {
      "must NoContent Result when valid request received" in {

        when(appConfig.eisFATCAFileUploadResponseXSDFilePath).thenReturn("/xsd/fatca/EIS_File_Upload_Response.xsd")
        when(xmlValidationService.validate(any(), any())).thenReturn(Right(xml))
        when(fileDetailsRepository.updateStatus(any(), any())).thenReturn(Future.successful(Some(fileDetails(MessageType.FATCA))))

        val result = Await.result(service.processFatca(xml, randomUUID.toString), 1.seconds)
        result mustBe NoContent
      }

      "must BadRequest Result when request received failed in schema validation" in {
        val xml: NodeSeq = <test>"test"</test>

        when(appConfig.eisFATCAFileUploadResponseXSDFilePath).thenReturn("/xsd/fatca/EIS_File_Upload_Response.xsd")
        when(xmlValidationService.validate(any(), any())).thenReturn(Left(Seq(SaxParseError(1, "failed"))))

        val result = Await.result(service.processFatca(xml, randomUUID.toString), 1.seconds)
        result mustBe BadRequest("XML Schema validation failed")
      }

      "must BadRequest Result when request received failed in model conversion" in {
        val xml: NodeSeq = <test>"test"</test>

        when(appConfig.eisFATCAFileUploadResponseXSDFilePath).thenReturn("/xsd/fatca/EIS_File_Upload_Response.xsd")
        when(xmlValidationService.validate(any(), any())).thenReturn(Right(xml))

        val result = Await.result(service.processFatca(xml, randomUUID.toString), 1.seconds)
        result mustBe BadRequest("Failed to read XML")
      }

      "must return BadRequest when request conversationId does not match file content conversationId" in {
        val xmlConvIdMismatch =
          replaceChild(xml.asInstanceOf[Elem], "conversationID", "testId")

        when(appConfig.eisFATCAFileUploadResponseXSDFilePath).thenReturn("/xsd/fatca/EIS_File_Upload_Response.xsd")
        when(xmlValidationService.validate(any(), any())).thenReturn(Right(xmlConvIdMismatch))

        val result = Await.result(service.processFatca(xmlConvIdMismatch, "mismatch-id"), 1.second)

        result mustBe BadRequest("Conversation ID does not match request header & file content")
      }

      "must InternalServer Result when unable to update file status" in {
        when(appConfig.eisFATCAFileUploadResponseXSDFilePath).thenReturn("/xsd/fatca/EIS_File_Upload_Response.xsd")
        when(xmlValidationService.validate(any(), any())).thenReturn(Right(xml))
        when(fileDetailsRepository.updateStatus(any(), any())).thenReturn(Future.successful(None))

        val result = Await.result(service.processFatca(xml, randomUUID.toString), 1.seconds)
        result mustBe InternalServerError
      }

    }
  }

  def replaceChild(xml: Elem, label: String, value: String): Elem =
    xml.copy(child = xml.child.map {
      case e: Elem if e.label == label =>
        e.copy(child = Text(value))
      case n => n
    })
}
