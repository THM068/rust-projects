/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.submission

import base.SpecBase
import org.scalatest.matchers.must.Matchers._
import models.submission.MessageType.{CRS, FATCA}
import play.api.libs.json.{JsResultException, Json}

import java.time.LocalDate

class MessageSpecDataSpec extends SpecBase {

  "MessageSpecDataSpec" - {
    val reportingPeriod = LocalDate.of(2023, 1, 1)
    "must serialize MessageSpecData with CRS" in {
      val msd = MessageSpecData(CRS, "sending-company-in", "message-ref-id", "reporting-fi-name", reportingPeriod, Some("giin"), "finame", true)
      val expectedJson =
        """{"messageType":"CRS","sendingCompanyIN":"sending-company-in","messageRefId":"message-ref-id","reportingFIName":"reporting-fi-name","reportingPeriod":"2023-01-01","giin":"giin","fiNameFromFim":"finame","electionsRequired":true}"""
      Json.toJson(msd).toString() mustBe expectedJson
    }

    "must serialize MessageSpecData with FATCA" in {
      val msd = MessageSpecData(FATCA, "sending-company-in", "message-ref-id", "reporting-fi-name", reportingPeriod, Some("giin"), "fiName", true)
      val expectedJson =
        """{"messageType":"FATCA","sendingCompanyIN":"sending-company-in","messageRefId":"message-ref-id","reportingFIName":"reporting-fi-name","reportingPeriod":"2023-01-01","giin":"giin","fiNameFromFim":"fiName","electionsRequired":true}"""
      Json.toJson(msd).toString() mustBe expectedJson
    }

    "must deserialize MessageSpecData" in {
      val json =
        """{"messageType":"FATCA","sendingCompanyIN":"sending-company-in","messageRefId":"message-ref-id","reportingFIName":"reporting-fi-name","reportingPeriod":"2023-01-01","giin":"giin","fiNameFromFim":"fiName","electionsRequired":true}"""
      val expected = MessageSpecData(FATCA, "sending-company-in", "message-ref-id", "reporting-fi-name", reportingPeriod, Some("giin"), "fiName", true)

      Json.parse(json).as[MessageSpecData] mustEqual expected
    }

    "must fail to deserialize for any other MessageType value" in {
      val json = """{"messageType":"UNKNOWN"}"""

      a[JsResultException] must be thrownBy Json.parse(json).as[MessageSpecData]
    }

  }
}
