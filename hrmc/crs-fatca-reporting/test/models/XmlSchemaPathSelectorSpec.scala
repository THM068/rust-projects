/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models

import base.SpecBase
import config.AppConfig
import models.submission.MessageType.{CRS, FATCA}
import org.scalatest.matchers.must.Matchers._

class XmlSchemaPathSelectorSpec extends SpecBase {

  "XmlSchemaPathSelector" - {
    val appConfig: AppConfig  = app.injector.instanceOf[AppConfig]
    val xmlSchemaPathSelector = new XmlSchemaPathSelector(appConfig)

    "must return the correct CRS  xsd path" in {
      xmlSchemaPathSelector.selectSchema(CRS) mustBe "/xsd/crs/CrsXML_v3.0.xsd"
    }

    "must return the correct Fatca xsd path" in {
      xmlSchemaPathSelector.selectSchema(FATCA) mustBe "/xsd/fatca/FatcaXML_v2.0.xsd"
    }
  }

}
