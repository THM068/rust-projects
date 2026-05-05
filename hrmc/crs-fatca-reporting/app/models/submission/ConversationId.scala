/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.submission

import models.upscan.UploadId
import play.api.libs.json.{JsString, Reads, Writes}
import play.api.mvc.PathBindable

opaque type ConversationId = String

object ConversationId {
  def apply(value: String): ConversationId = value

  extension (id: ConversationId) def value: String = id

  def fromUploadId(uploadId: UploadId): ConversationId = ConversationId(uploadId.value)
  given Writes[ConversationId]                         = conversationId => JsString(conversationId.value)
  given Reads[ConversationId]                          = Reads.StringReads.map(ConversationId.apply)

  given pathBindable: PathBindable[ConversationId] = new PathBindable[ConversationId] {
    override def bind(key: String, value: String): Either[String, ConversationId] =
      implicitly[PathBindable[String]].bind(key, value).map(ConversationId(_))

    override def unbind(key: String, value: ConversationId): String =
      value.value
  }
}
