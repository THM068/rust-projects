/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models

import java.time.ZoneId

object Constants {
  val EUROPE_LONDON_TIME_ZONE: ZoneId = ZoneId.of("Europe/London")

  val MDTPSystem: String            = "MDTP"
  val CRFARegime: String            = "CRFA"
  val EISTransmittingSystem: String = "EIS"
}
