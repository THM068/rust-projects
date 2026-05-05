/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package connectors

import models.submission.ConversationId
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.*
import org.scalatest.prop.TableDrivenPropertyChecks.*
import play.api.Application
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.crsfatcareporting.utils.ISpecBase
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class SubmissionConnectorSpec extends AnyFreeSpec with ISpecBase {

  override lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.crs-submission.port" -> server.port(),
      "auditing.enabled"                                           -> "false",
      "mongodb.uri"                                                -> mongoUri
    )
    .build()

  private val conversationId = ConversationId(UUID.randomUUID().toString)

  lazy val connector: SubmissionConnector = app.injector.instanceOf[SubmissionConnector]

  "SubmissionConnector" - {

    "must return Future success when there is success response for CRS" in {
      stubPostResponse("/dac6/crs/CustomerSubmissionData/v1", NO_CONTENT)

      val xml = <test></test>

      whenReady(connector.submitCRS(xml, conversationId)) {
        result => assert(result.status == 204)
      }
    }

    "must throw exception for error HTTP response status for CRS" in {

      val errorStatus = Table("status", 408, 500)

      forAll(errorStatus) { status =>
        stubPostResponse("/dac6/crs/CustomerSubmissionData/v1", status)

        val xml = <test></test>

        val exception = connector.submitCRS(xml, conversationId).failed.futureValue
        exception mustBe a[UpstreamErrorResponse]
      }
    }

    "must return Future success when there is success response for FATCA" in {
      stubPostResponse("/dac6/fatca/CustomerSubmissionData/v1", NO_CONTENT)

      val xml = <test></test>

      whenReady(connector.submitFatca(xml, conversationId)) {
        result => assert(result.status == 204)
      }
    }

    "must throw exception for error HTTP response status for FACTA" in {

      val errorStatus = Table("status", 408, 500)

      forAll(errorStatus) { status =>
        stubPostResponse("/dac6/fatca/CustomerSubmissionData/v1", status)

        val xml = <test></test>

        val exception = connector.submitFatca(xml, conversationId).failed.futureValue
        exception mustBe a[UpstreamErrorResponse]
      }
    }
  }
}
