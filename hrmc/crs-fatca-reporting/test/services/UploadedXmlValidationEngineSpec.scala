/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import base.SpecBase
import config.AppConfig
import models.elections.CheckElectionRequiredRequest
import models.{DataExtraction, XmlExtractedElements, XmlSchemaPathSelector}
import models.submission.MessageSpecData
import models.submission.MessageType.{CRS, FATCA}
import models.validation._
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.matchers.must.Matchers._
import services.validation.{UploadedXmlValidationEngine, XMLValidationService}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{LocalDate, ZoneId}
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.xml.{Elem, NodeSeq}

class UploadedXmlValidationEngineSpec extends SpecBase {
  val reportingPeriod: LocalDate                 = LocalDate.of(2014, 12, 31)
  val reportingPeriodAtCurrentYear: LocalDate    = LocalDate.of(LocalDate.now().getYear, 12, 31)
  val noErrors: ListBuffer[SaxParseError]        = ListBuffer()
  val xmlExtractedElements: XmlExtractedElements = XmlExtractedElements(CRS, "683373339", "message-ref-id", "reporting-fi-name", reportingPeriod)
  val xmlExtractedElementsForFatca: XmlExtractedElements =
    XmlExtractedElements(FATCA, "683373339", "message-ref-id", "reporting-fi-name", reportingPeriodAtCurrentYear)
  val messageSpecData: MessageSpecData =
    MessageSpecData(CRS, "683373339", "message-ref-id", "reporting-fi-name", reportingPeriod, giin = Some("689355555"), "First FI", true)
  val fatcaMessageSpecData: MessageSpecData = MessageSpecData(
    FATCA,
    "683373339",
    "message-ref-id",
    "reporting-fi-name",
    reportingPeriodAtCurrentYear,
    giin = Some("689355555"),
    fiNameFromFim = "First FI",
    electionsRequired = true
  )

  trait SetUp {
    val doesFileHaveBusinessErrors = false

    val mockXmlValidationService: XMLValidationService                 = mock[XMLValidationService]
    val appConfig: AppConfig                                           = app.injector.instanceOf[AppConfig]
    val xmlSchemaPathSelector: XmlSchemaPathSelector                   = app.injector.instanceOf[XmlSchemaPathSelector]
    val mockDataExtraction: DataExtraction                             = mock[DataExtraction]
    val mockFinancialInstitutionsService: FinancialInstitutionsService = mock[FinancialInstitutionsService]
    val mockElectionService: ElectionService                           = mock[ElectionService]

    val validationEngine =
      new UploadedXmlValidationEngine(mockXmlValidationService,
                                      mockDataExtraction,
                                      xmlSchemaPathSelector,
                                      mockFinancialInstitutionsService,
                                      appConfig,
                                      mockElectionService
      )

    val source               = "src"
    val subscriptionId       = "some-subscriptionId"
    val mockCRSXML: Elem     = <MessageType>CRS</MessageType>
    val mockFATCAXML: Elem   = <MessageType>FATCA</MessageType>
    val mockInvalidXML: Elem = <MessageType>INVALID</MessageType>
  }

  "ValidateUploadSubmission" - {

    "must return UploadSubmissionValidationSuccess for crs when xml with no errors received" in new SetUp {
      when(mockXmlValidationService.loadXml(any[String]()))
        .thenReturn(Future.successful(Right(mockCRSXML)))

      when(mockDataExtraction.parseMessageType(any[Elem]))
        .thenReturn(Future.successful(Right(CRS)))

      when(mockXmlValidationService.validate(any[NodeSeq](), any[String]())).thenReturn(Right(mockCRSXML))
      when(mockDataExtraction.extractRequiredElements(any[Elem]())).thenReturn(Some(xmlExtractedElements))
      when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(subscriptionId))
        .thenReturn(Future.successful(testFiDetails))
      when(mockElectionService.checkElectionRequired(any[CheckElectionRequiredRequest]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Right(true)))

      Await.result(validationEngine.validateUploadSubmission(source, subscriptionId), 10.seconds) mustBe SubmissionValidationSuccess(messageSpecData)

      verify(mockXmlValidationService).validate(
        argThat((nodeSeq: NodeSeq) => (nodeSeq \\ "MessageType").text == "CRS"),
        argThat((filePath: String) => filePath == appConfig.crsFileUploadXSDFilePath)
      )

      verify(mockFinancialInstitutionsService, times(1)).getListOfFinancialInstitutions(subscriptionId)
    }

    "must return UploadSubmissionValidationSuccess for fatca when xml with no errors received" in new SetUp {
      when(mockXmlValidationService.loadXml(any[String]()))
        .thenReturn(Future.successful(Right(mockFATCAXML)))

      when(mockDataExtraction.parseMessageType(any[Elem]))
        .thenReturn(Future.successful(Right(CRS)))

      when(mockXmlValidationService.validate(any[NodeSeq](), any[String]())).thenReturn(Right(mockFATCAXML))
      when(mockDataExtraction.extractRequiredElements(any[Elem]())).thenReturn(Some(xmlExtractedElementsForFatca))
      when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(subscriptionId))
        .thenReturn(Future.successful(testFiDetails))

      when(mockElectionService.checkElectionRequired(any[CheckElectionRequiredRequest]())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Right(true)))

      Await.result(validationEngine.validateUploadSubmission(source, subscriptionId), 10.seconds) mustBe SubmissionValidationSuccess(fatcaMessageSpecData)

      verify(mockXmlValidationService).validate(
        argThat((nodeSeq: NodeSeq) => (nodeSeq \\ "MessageType").text == "FATCA"),
        argThat((filePath: String) => filePath == appConfig.crsFileUploadXSDFilePath)
      )

      verify(mockFinancialInstitutionsService, times(1)).getListOfFinancialInstitutions(subscriptionId)
    }

    "must return FIIDDoesNotMatchSendCompanyInError when SendCompanyIn does not match any of the FIIDS" in new SetUp {
      val extractedXmlWithDifferentSendCompanyIn: XmlExtractedElements =
        XmlExtractedElements(FATCA, "DifferentFIID", "message-ref-id", "reporting-fi-name", reportingPeriod)
      when(mockXmlValidationService.loadXml(any[String]()))
        .thenReturn(Future.successful(Right(mockFATCAXML)))

      when(mockDataExtraction.parseMessageType(any[Elem]))
        .thenReturn(Future.successful(Right(CRS)))

      when(mockXmlValidationService.validate(any[NodeSeq](), any[String]())).thenReturn(Right(mockFATCAXML))
      when(mockDataExtraction.extractRequiredElements(any[Elem]())).thenReturn(Some(extractedXmlWithDifferentSendCompanyIn))
      when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(subscriptionId))
        .thenReturn(Future.successful(testFiDetails))

      Await.result(validationEngine.validateUploadSubmission(source, subscriptionId), 10.seconds) mustBe FIIDDoesNotMatchSendCompanyInError(
        "The FI ID in your file does not match any financial institutions in the service"
      )

      verify(mockElectionService, never()).checkElectionRequired(any())(any[HeaderCarrier](), any[ExecutionContext]())
    }

    "must return FIIDDoesNotMatchSendCompanyInError when financialInstuteService returns an empy List" in new SetUp {
      when(mockXmlValidationService.loadXml(any[String]()))
        .thenReturn(Future.successful(Right(mockFATCAXML)))

      when(mockDataExtraction.parseMessageType(any[Elem]))
        .thenReturn(Future.successful(Right(CRS)))

      when(mockXmlValidationService.validate(any[NodeSeq](), any[String]())).thenReturn(Right(mockFATCAXML))
      when(mockDataExtraction.extractRequiredElements(any[Elem]())).thenReturn(Some(xmlExtractedElementsForFatca))
      when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(subscriptionId))
        .thenReturn(Future.successful(Seq()))

      Await.result(validationEngine.validateUploadSubmission(source, subscriptionId), 10.seconds) mustBe FIIDDoesNotMatchSendCompanyInError(
        "The FI ID in your file does not match any financial institutions in the service"
      )
      verify(mockElectionService, never()).checkElectionRequired(any())(any[HeaderCarrier](), any[ExecutionContext]())
    }

    "must return InvalidReportingPeriodError when given period is before configured earliest cuttoff date" in new SetUp {
      val earlyDate = LocalDate.of(2014, 11, 30)
      when(mockXmlValidationService.loadXml(any[String]()))
        .thenReturn(Future.successful(Right(mockCRSXML)))

      when(mockDataExtraction.parseMessageType(any[Elem]))
        .thenReturn(Future.successful(Right(CRS)))

      when(mockXmlValidationService.validate(any[NodeSeq](), any[String]())).thenReturn(Right(mockCRSXML))
      when(mockDataExtraction.extractRequiredElements(any[Elem]())).thenReturn(Some(xmlExtractedElements.copy(reportingPeriod = earlyDate)))
      when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(subscriptionId))
        .thenReturn(Future.successful(testFiDetails))

      Await.result(validationEngine.validateUploadSubmission(source, subscriptionId), 10.seconds) mustBe InvalidReportingPeriodError(
        s"The reporting period must be after ${appConfig.reportingPeriodEarliestDate} or in the current year"
      )
      verify(mockElectionService, never()).checkElectionRequired(any())(any[HeaderCarrier](), any[ExecutionContext]())
    }

    "must return InvalidReportingPeriodError when given period is after the current year" in new SetUp {
      val zoneId   = ZoneId.of("Europe/London")
      val nextYear = LocalDate.now(zoneId).plusYears(1).getYear
      val lateDate = LocalDate.of(nextYear, 11, 30)
      when(mockXmlValidationService.loadXml(any[String]()))
        .thenReturn(Future.successful(Right(mockCRSXML)))

      when(mockDataExtraction.parseMessageType(any[Elem]))
        .thenReturn(Future.successful(Right(CRS)))

      when(mockXmlValidationService.validate(any[NodeSeq](), any[String]())).thenReturn(Right(mockCRSXML))
      when(mockDataExtraction.extractRequiredElements(any[Elem]())).thenReturn(Some(xmlExtractedElements.copy(reportingPeriod = lateDate)))
      when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(subscriptionId))
        .thenReturn(Future.successful(testFiDetails))

      Await.result(validationEngine.validateUploadSubmission(source, subscriptionId), 10.seconds) mustBe InvalidReportingPeriodError(
        s"The reporting period must be after ${appConfig.reportingPeriodEarliestDate} or in the current year"
      )
      verify(mockElectionService, never()).checkElectionRequired(any())(any[HeaderCarrier](), any[ExecutionContext]())
    }

    "must return ValidationFailure with generic error message if parse error is not in an expected format" in new SetUp {
      when(mockXmlValidationService.loadXml(any[String]()))
        .thenReturn(Future.successful(Left(InvalidXmlError("Invalid Xml"))))

      Await.result(validationEngine.validateUploadSubmission(source, subscriptionId), 10.seconds) mustBe InvalidXmlError("Invalid Xml")
    }

    "must return SubmissionValidationFailure when it is not possible to extract messageSpec data" in new SetUp {
      when(mockXmlValidationService.loadXml(any[String]()))
        .thenReturn(Future.successful(Right(mockCRSXML)))

      when(mockDataExtraction.parseMessageType(any[Elem]))
        .thenReturn(Future.successful(Right(CRS)))

      when(mockXmlValidationService.validate(any[NodeSeq](), any[String]())).thenReturn(Right(mockCRSXML))
      when(mockDataExtraction.extractRequiredElements(any[Elem]())).thenReturn(None)

      Await.result(validationEngine.validateUploadSubmission(source, subscriptionId), 10.seconds) mustBe InvalidXmlError(
        "Could not retrieve messageSpec information from the submission"
      )
      verify(mockElectionService, never()).checkElectionRequired(any())(any[HeaderCarrier](), any[ExecutionContext]())

    }

    "must return InvalidMessageTypeError for an unknown MessageType" in new SetUp {
      when(mockXmlValidationService.loadXml(any[String]()))
        .thenReturn(Future.successful(Right(mockInvalidXML)))

      when(mockDataExtraction.parseMessageType(any[Elem]))
        .thenReturn(Future.successful(Left(InvalidMessageTypeError())))

      Await.result(validationEngine.validateUploadSubmission(source, subscriptionId), 10.seconds) mustBe InvalidMessageTypeError()
    }
  }
}
