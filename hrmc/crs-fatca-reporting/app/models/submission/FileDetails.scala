/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.submission

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

case class FileDetails(
  _id: ConversationId,
  enrolmentId: String,
  messageRefId: String,
  reportingEntityName: String,
  status: FileStatus,
  name: String,
  submitted: LocalDateTime,
  lastUpdated: LocalDateTime,
  reportingPeriod: LocalDate,
  messageType: MessageType,
  fileType: FileType = NormalFile
)

object FileDetails {

  implicit val format: OFormat[FileDetails] = Json.format[FileDetails]

  val mongoFormat: OFormat[FileDetails] = {

    val localDateTimeReads: Reads[LocalDateTime] =
      Reads
        .at[String](__ \ "$date" \ "$numberLong")
        .map(date => Instant.ofEpochMilli(date.toLong).atZone(ZoneOffset.UTC).toLocalDateTime)

    val localDateTimeWrites: Writes[LocalDateTime] =
      Writes
        .at[String](__ \ "$date" \ "$numberLong")
        .contramap(_.toInstant(ZoneOffset.UTC).toEpochMilli.toString)

    implicit val dateTimeFormat: Format[LocalDateTime] = Format(localDateTimeReads, localDateTimeWrites)

    implicit val dateFormat: Format[LocalDate] = MongoJavatimeFormats.localDateFormat

    Json.format[FileDetails]
  }
}
