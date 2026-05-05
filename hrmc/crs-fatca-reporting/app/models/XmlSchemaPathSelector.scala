/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models

import com.google.inject.Inject
import config.AppConfig
import models.submission.MessageType
import models.submission.MessageType.{CRS, FATCA}

class XmlSchemaPathSelector @Inject() (appConfig: AppConfig) {
  type XmlSchemaPath = String

  def selectSchema(messageType: MessageType): XmlSchemaPath =
    messageType match {
      case CRS   => appConfig.crsFileUploadXSDFilePath
      case FATCA => appConfig.fatcaFileUploadXSDFilePath
    }

}
