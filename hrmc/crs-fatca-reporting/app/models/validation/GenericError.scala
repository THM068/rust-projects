/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.validation

import play.api.libs.json.{Json, OFormat}

case class GenericError(lineNumber: Int, message: Message)

case class Message(messageKey: String, args: Seq[String] = Seq.empty)

object Message {
  implicit val messageFormat: OFormat[Message] = Json.format[Message]
}

object GenericError {

  implicit def orderByLineNumber[A <: GenericError]: Ordering[A] =
    Ordering.by(ge => (ge.lineNumber, ge.message.messageKey))

  implicit val format: OFormat[GenericError] = Json.format[GenericError]
}
