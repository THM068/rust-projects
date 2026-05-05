/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.submission

import java.time.ZonedDateTime

case class SubmissionMetaData(submissionTime: ZonedDateTime, conversationId: ConversationId, fileName: Option[String])
