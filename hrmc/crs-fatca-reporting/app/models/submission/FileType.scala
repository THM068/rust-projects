/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package models.submission

import play.api.libs.json.*

sealed trait FileType

case object NormalFile extends FileType
case object LargeFile extends FileType

object FileType {

  val values: Seq[FileType] = Seq(NormalFile, LargeFile)

  def fromString(fileType: String): FileType = fileType.toUpperCase match {
    case "NORMAL" => NormalFile
    case "LARGE"  => LargeFile
    case _        => throw new NoSuchElementException
  }

  given Format[FileType] = Format(
    Reads {
      case JsString("NormalFile") => JsSuccess(NormalFile)
      case JsString("LargeFile")  => JsSuccess(LargeFile)
      case _                      => JsError("Expected JsObject")
    },
    Writes {
      case NormalFile => JsString("NormalFile")
      case LargeFile  => JsString("LargeFile")
    }
  )
}
