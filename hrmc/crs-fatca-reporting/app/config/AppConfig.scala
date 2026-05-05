/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package config

import models.sdes.Algorithm

import javax.inject.{Inject, Singleton}
import play.api.Configuration

import java.time.LocalDate

@Singleton
class AppConfig @Inject() (config: Configuration) {

  val appName: String = config.get[String]("appName")

  lazy val cacheTtl: Long = config.get[Long]("mongodb.timeToLiveInSeconds")

  val fatcaFileUploadXSDFilePath: String            = config.get[String]("xsd-files.fileUpload_FATCA_XSDFile")
  val crsFileUploadXSDFilePath: String              = config.get[String]("xsd-files.fileUpload_CRS_XSDFile")
  val eisCRSFileUploadResponseXSDFilePath: String   = config.get[String]("xsd-files.eis_CRS_FileUploadResponse_XSDFile")
  val eisFATCAFileUploadResponseXSDFilePath: String = config.get[String]("xsd-files.eis_FATCA_FileUploadResponse_XSDFile")

  val reportingPeriodEarliestDate: LocalDate = LocalDate.parse(config.get[String]("reportingPeriod.earliestDate"))

  lazy val fIManagementUrl: String              = config.get[Service]("microservice.services.crs-fatca-fi-management").baseUrl
  lazy val cadxViewElectionsDetailUrl: String   = config.get[Service]("microservice.services.view-elections-details").baseUrl
  lazy val cadxSubmitElectionsDetailUrl: String = config.get[Service]("microservice.services.submit-elections-details").baseUrl
  private lazy val registrationBaseUrl: String  = config.get[Service]("microservice.services.crs-fatca-registration").baseUrl
  lazy val registrationUrl: String              = registrationBaseUrl + config.get[String]("microservice.services.crs-fatca-registration.base-path")

  lazy val sdesBaseUrl: String              = config.get[Service]("microservice.services.sdes").baseUrl
  private val sdesLocation: Option[String]  = Option(config.get[String]("sdes.location")).filter(_.nonEmpty)
  lazy val sdesUrl: String                  = List(Option(sdesBaseUrl), sdesLocation, Some("notification"), Some("fileready")).flatten.mkString("/")
  lazy val sdesClientId: String             = config.get[String]("sdes.client-id")
  lazy val sdesInformationType: String      = config.get[String]("sdes.information-type")
  lazy val sdesChecksumAlgorithm: Algorithm = Algorithm(config.get[String]("sdes.checksum-algorithm"))
  lazy val sdesRecipientOrSender: String    = config.get[String]("sdes.recipient-or-sender")

  val bearerToken: String => String = (serviceName: String) => config.get[String](s"microservice.services.$serviceName.bearer-token")
  val environment: String => String = (serviceName: String) => config.get[String](s"microservice.services.$serviceName.environment")

  lazy val crsFileSubmission: String   = config.get[Service](s"microservice.services.crs-submission").baseUrl
  lazy val fatcaFileSubmission: String = config.get[Service](s"microservice.services.fatca-submission").baseUrl
  lazy val submissionTtl: Long         = config.get[Long]("mongodb.submission.timeToLiveInDays")

}
