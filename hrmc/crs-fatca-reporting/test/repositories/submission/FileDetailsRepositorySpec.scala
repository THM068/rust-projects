/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package repositories.submission

import config.AppConfig
import models.submission.*
import models.submission.MessageType.CRS
import org.mockito.Mockito.when
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.*
import scala.concurrent.ExecutionContext.Implicits.global

class FileDetailsRepositorySpec extends AnyWordSpec with Matchers with DefaultPlayMongoRepositorySupport[FileDetails] with MockitoSugar with ScalaFutures {

  val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.submissionTtl).thenReturn(900L)

  implicit val clock: Clock = Clock.fixed(Instant.parse("2026-01-06T12:00:00Z"), ZoneId.of("UTC"))

  override protected val repository: FileDetailsRepository = new FileDetailsRepository(
    mongoComponent,
    mockAppConfig
  )

  // Test Data
  val conversationId = ConversationId("conversation-123")
  val dateTime       = LocalDateTime.of(2026, 1, 6, 12, 0, 0)
  val reportingDate  = LocalDate.of(2026, 1, 1)

  val fileDetails = FileDetails(
    _id = conversationId,
    enrolmentId = "XACBC0000123456",
    messageRefId = "GBXACBC12345678",
    reportingEntityName = "Test Entity",
    status = Pending,
    name = "test-file.xml",
    submitted = dateTime,
    lastUpdated = dateTime,
    reportingPeriod = reportingDate,
    messageType = CRS
  )

  "FileDetailsRepository" should {

    "insert" should {

      "successfully insert a new file details record" in {
        val result = repository.insert(fileDetails).futureValue

        result mustBe ()

        val inserted = repository.findByConversationId(conversationId).futureValue
        inserted mustBe Some(fileDetails)
      }

      "update (overwrite) an existing record if the ConversationId already exists" in {
        repository.insert(fileDetails).futureValue

        val modifiedFile = fileDetails.copy(name = "updated-name.xml")

        val result = repository.insert(modifiedFile).futureValue

        result mustBe ()

        val savedFile = repository.findByConversationId(conversationId).futureValue
        savedFile.value.name mustBe "updated-name.xml"
      }
    }

    "find a file by conversationId" in {
      insert(fileDetails).futureValue

      val result = repository.findByConversationId(conversationId).futureValue
      result shouldBe Some(fileDetails)
    }

    "return None when finding a non-existent conversationId" in {
      val result = repository.findByConversationId(ConversationId("non-existent")).futureValue
      result shouldBe None
    }

    "find files by enrolmentId" in {
      val otherFile = fileDetails.copy(
        _id = ConversationId("conversation-456"),
        enrolmentId = "OTHER_ID"
      )

      insert(fileDetails).futureValue
      insert(otherFile).futureValue

      val result = repository.findByEnrolmentId("XACBC0000123456").futureValue

      result.size shouldBe 1
      result.head shouldBe fileDetails
    }

    "update the status of a file" in {
      insert(fileDetails).futureValue

      val updatedStatus = Accepted
      val result        = repository.updateStatus(conversationId, updatedStatus).futureValue

      result shouldBe defined
      result.get.status shouldBe updatedStatus

      val updatedFile = repository.findByConversationId(conversationId).futureValue
      updatedFile.map(_.status) shouldBe Some(updatedStatus)
    }

    "validate indices" in {
      val indices = repository.indexes

      indices.exists(_.getOptions.getName == "enrolmentId-index") shouldBe true
      indices.exists(_.getOptions.getName == "submission-last-updated-index") shouldBe true
    }
  }
}
