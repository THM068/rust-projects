/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.submission

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class FileStatusSpec extends AnyFreeSpec with Matchers {

  "FileStatus" - {
    "must convert to string as expected" in {
      val rejected = Rejected
      rejected.toString() mustBe "Rejected"
    }
  }
}
