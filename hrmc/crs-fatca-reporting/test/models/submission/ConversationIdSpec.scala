/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.submission

import base.SpecBase
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.libs.json.{JsString, JsValue, Json}

class ConversationIdSpec extends SpecBase {

  "ConversationId" - {
    "must serialise and de-serialise ConversationId" in {

      val json: JsValue = JsString("conversationId")

      val cid = json.as[ConversationId]
      cid mustBe ConversationId("conversationId")

      Json.toJson(cid) mustBe json
    }
  }
}
