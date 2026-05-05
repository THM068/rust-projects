/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package utils.json

import play.api.libs.json.*

object OpaqueFormatOps:
  extension [A](fmt: Format[A])
    inline def asOpaque[B]: Format[B] =
      fmt.bimap[B](
        _.asInstanceOf[B],
        _.asInstanceOf[A]
      )
