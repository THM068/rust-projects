/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services

import base.SpecBase
import connectors.CADXElectionsConnector
import models.elections.*
import models.submission.MessageType
import models.validation.ViewElectionErrors
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.matchers.must.Matchers.*

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class ElectionServiceSpec extends SpecBase {

  val CADXElectionsConnector: CADXElectionsConnector = mock[CADXElectionsConnector]
  val service: ElectionService                       = new ElectionService(CADXElectionsConnector)

  "checkElectionRequired" - {
    "must return false when reporting period is outside the reporting period window" in new TestContext {
      val reportingPeriod              = 2011
      val checkElectionRequiredRequest = CheckElectionRequiredRequest.of(MessageType.CRS, fiid, reportingPeriod)

      val result = Await.result(service.checkElectionRequired(checkElectionRequiredRequest), 1.seconds)

      result.isRight mustBe true
      result.value mustBe false
      verify(CADXElectionsConnector, never()).viewElections(fiid)
    }

    "must return false when election exist for the reporting period and regime" in new TestContext {
      val reportingPeriod = 2023

      val checkElectionRequiredRequest = CheckElectionRequiredRequest.of(MessageType.CRS, fiid, reportingPeriod)
      when(CADXElectionsConnector.viewElections(fiid)).thenReturn(Future.successful(electionList))

      val result = service.checkElectionRequired(checkElectionRequiredRequest).futureValue
      result.isRight mustBe true
      result.value mustBe false
    }

    "must return true when CADXElectionsConnector returns an empty election list" in new TestContext {
      val reportingPeriod = LocalDate.now().getYear

      val checkElectionRequiredRequest = CheckElectionRequiredRequest.of(MessageType.CRS, fiid, reportingPeriod)
      val emptyElectionsList           = Seq.empty
      when(CADXElectionsConnector.viewElections(fiid)).thenReturn(Future.successful(emptyElectionsList))

      val result = service.checkElectionRequired(checkElectionRequiredRequest).futureValue
      result.isRight mustBe true
      result.value mustBe true
    }

    "must return true when no election exist for the reporting period and regime" in new TestContext {
      Seq(MessageType.FATCA, MessageType.CRS).foreach { regime =>
        val reportingPeriod = 2025

        val checkElectionRequiredRequest = CheckElectionRequiredRequest.of(regime, fiid, reportingPeriod)
        when(CADXElectionsConnector.viewElections(fiid)).thenReturn(Future.successful(electionList))

        val result = service.checkElectionRequired(checkElectionRequiredRequest).futureValue
        result.isRight mustBe true
        result.value mustBe true
      }
    }

    "must return Left(ViewElectionErrors) when CADXElectionsConnector fails" in new TestContext {
      val reportingPeriod = 2023

      val checkElectionRequiredRequest = CheckElectionRequiredRequest.of(MessageType.CRS, fiid, reportingPeriod)
      when(CADXElectionsConnector.viewElections(fiid)).thenReturn(Future.failed(new Exception("Some error occurred")))

      val result = service.checkElectionRequired(checkElectionRequiredRequest).futureValue
      result.isLeft mustBe true
      result.left.value mustBe a[ViewElectionErrors]
    }

  }
}

trait TestContext {
  val fiid            = "FI12345"
  val fatcaElection   = FATCA(HasThresholds(Some(true)), HasTreasuryRegulations(Some(false)))
  val crsElection     = CRS(HasCARF(Some(true)), HasContracts(Some(false)), HasDormantAccounts(Some(true)), HasThresholds(Some(true)))
  val electionDetail1 = ElectionDetails(Some(crsElection), Some(fatcaElection), ReportingPeriod("2022"))
  val electionDetail2 = ElectionDetails(Some(crsElection), Some(fatcaElection), ReportingPeriod("2023"))
  val electionDetail3 = ElectionDetails(Some(crsElection), None, ReportingPeriod("2024"))
  val electionList    = Seq(electionDetail1, electionDetail2, electionDetail3)

}
