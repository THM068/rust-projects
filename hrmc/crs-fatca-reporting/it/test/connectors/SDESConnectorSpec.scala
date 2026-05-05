/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package connectors

import generators.Generators
import models.sdes.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, REQUEST_TIMEOUT}
import uk.gov.hmrc.crsfatcareporting.utils.ISpecBase

import scala.concurrent.ExecutionContext.Implicits.global

class SDESConnectorSpec extends AnyFreeSpec with ISpecBase with ScalaCheckPropertyChecks with Generators {



  override lazy val app: Application = applicationBuilder()
    .configure(
      "microservice.services.sdes.port" -> server.port(),
      "auditing.enabled"                                            -> "false",
      "mongodb.uri"                                                 -> mongoUri
    )
    .build()

  lazy val connector: SDESConnector = app.injector.instanceOf[SDESConnector]

  private val statusCodes = Table(
    ("sdesStatusCode", "expectedConnectorResult"),
    (NO_CONTENT, Right(NO_CONTENT)),
    (INTERNAL_SERVER_ERROR, Left(INTERNAL_SERVER_ERROR)),
    (REQUEST_TIMEOUT, Left(INTERNAL_SERVER_ERROR))
  )

  "SDESConnector" - {
    forAll(statusCodes) { (sdesStatusCode, expectedConnectorResult) =>
      s"sendFileNotification must return $expectedConnectorResult when SDES returns status $sdesStatusCode" in {
        stubResponse("/sdes-stub/notification/fileready", sdesStatusCode)

        forAll { (fileTransferNotification: FileTransferNotification) =>
          val result = connector.sendFileNotification(fileTransferNotification)

          result.futureValue mustBe expectedConnectorResult
        }
      }
    }
  }

}
