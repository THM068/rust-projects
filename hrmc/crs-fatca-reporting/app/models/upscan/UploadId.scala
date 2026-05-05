/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.upscan

import play.api.libs.json._
import play.api.mvc.QueryStringBindable

case class UploadId(value: String)

object UploadId {

  implicit def queryBinder(implicit
    stringBinder: QueryStringBindable[String]
  ): QueryStringBindable[UploadId] =
    stringBinder.transform(UploadId(_), _.value)

  implicit val uploadIdFormat: OFormat[UploadId] = Json.format[UploadId]

  implicit def readsUploadId: Reads[UploadId] =
    Reads.StringReads.map(UploadId(_))

  implicit def writesUploadId: Writes[UploadId] =
    Writes[UploadId](x => JsString(x.value))
}
