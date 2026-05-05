/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.elections

import models.submission.MessageType

case class CheckElectionRequiredRequest(fiId: String, regime: MessageType, reportingPeriod: Int) {}

object CheckElectionRequiredRequest {
  def of(messageType: MessageType, sendCompanyIn: String, reportingPeriod: Int): CheckElectionRequiredRequest =
    CheckElectionRequiredRequest(sendCompanyIn, messageType, reportingPeriod)
}
