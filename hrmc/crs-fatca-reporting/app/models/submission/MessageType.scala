/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.submission
import play.api.libs.json.*

enum MessageType:
  case CRS, FATCA

object MessageType:
  extension (p: MessageType)
    def toDisplayString: String = p match
      case MessageType.CRS   => "CRS"
      case MessageType.FATCA => "FATCA"

  given Format[MessageType] = new Format[MessageType] {
    override def reads(json: JsValue): JsResult[MessageType] = json match {
      case JsString("CRS")   => JsSuccess(MessageType.CRS)
      case JsString("FATCA") => JsSuccess(MessageType.FATCA)
      case _                 => JsError(s"Unexpected value of _type: $json")
    }

    override def writes(o: MessageType): JsValue = o match {
      case MessageType.CRS   => JsString("CRS")
      case MessageType.FATCA => JsString("FATCA")
    }
  }
