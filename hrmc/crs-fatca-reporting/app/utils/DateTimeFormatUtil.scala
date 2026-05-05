/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package utils

import models.Constants.EUROPE_LONDON_TIME_ZONE

import java.time.*

object DateTimeFormatUtil {
  def zonedDateTimeNow(implicit clock: Clock): ZonedDateTime = ZonedDateTime.now(clock.withZone(EUROPE_LONDON_TIME_ZONE))
}
