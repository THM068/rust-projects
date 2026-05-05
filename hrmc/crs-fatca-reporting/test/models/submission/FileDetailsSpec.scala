/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.submission

import models.submission.MessageType.CRS
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

import java.time.{LocalDate, LocalDateTime}

class FileDetailsSpec extends AnyWordSpec with Matchers {

  "FileDetails" should {

    val conversationId = ConversationId("conversation-123")
    val submittedTime  = LocalDateTime.of(2026, 1, 6, 12, 0, 0)
    val reportingDate  = LocalDate.of(2026, 1, 1)

    val fileDetails = FileDetails(
      _id = conversationId,
      enrolmentId = "XACBC0000123456",
      messageRefId = "GBXACBC12345678",
      reportingEntityName = "Test Entity",
      status = Pending,
      name = "test-file.xml",
      submitted = submittedTime,
      lastUpdated = submittedTime,
      reportingPeriod = reportingDate,
      messageType = CRS,
      fileType = NormalFile
    )

    val expectedJson = Json.parse(
      """
        |{
        |  "_id": "conversation-123",
        |  "enrolmentId": "XACBC0000123456",
        |  "messageRefId": "GBXACBC12345678",
        |  "reportingEntityName": "Test Entity",
        |  "status": "Pending",
        |  "name": "test-file.xml",
        |  "submitted": "2026-01-06T12:00:00",
        |  "lastUpdated": "2026-01-06T12:00:00",
        |  "reportingPeriod": "2026-01-01",
        |  "messageType": "CRS",
        |  "fileType":"NormalFile"
        |}
        |""".stripMargin
    )

    "serialise to JSON correctly" in {
      Json.toJson(fileDetails) shouldBe expectedJson
    }

    "deserialise from JSON correctly" in {
      expectedJson.validate[FileDetails] shouldBe JsSuccess(fileDetails)
    }
  }
}
