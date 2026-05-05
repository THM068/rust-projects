/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.crsfatcareporting.utils

import models.upscan.UploadSessionDetails
import org.mongodb.scala.{Document, SingleObservableFuture}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.{Application, Configuration}
import repositories.upscan.UpScanSessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

trait ISpecBase extends GuiceOneServerPerSuite with DefaultPlayMongoRepositorySupport[UploadSessionDetails] with ScalaFutures with WireMockHelper {

  override def beforeEach(): Unit = {
    Await.result(repository.collection.deleteMany(Document()).toFuture(),5.seconds)
    super.beforeEach()
  }

  val repository: UpScanSessionRepository = app.injector.instanceOf[UpScanSessionRepository]
  implicit val hc: HeaderCarrier         = HeaderCarrier()

  def config: Map[String, String] = Map(
    "mongodb.uri"                                             -> mongoUri
  )

  def buildClient(path: String): WSRequest =
    app.injector.instanceOf[WSClient].url(s"http://localhost:$port/crs-fatca-reporting$path")

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(20, Seconds)))

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build()

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        Configuration("metrics.enabled" -> "false", "enrolmentKeys.crsfatca.key" -> "HMRC-FATCA-ORG", "enrolmentKeys.crsfatca.identifier" -> "FATCAID")
      )
      .overrides()
}
