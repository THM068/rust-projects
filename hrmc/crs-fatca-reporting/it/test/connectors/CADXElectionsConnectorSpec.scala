/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package connectors

import com.github.tomakehurst.wiremock.http.RequestMethod
import models.elections._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import play.api.Application
import play.api.http.Status.*
import uk.gov.hmrc.crsfatcareporting.utils.ISpecBase
import uk.gov.hmrc.http.{BadRequestException, UpstreamErrorResponse}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class CADXElectionsConnectorSpec extends AnyFreeSpec with ISpecBase {

  override lazy val app: Application = applicationBuilder()
    .configure(
      "microservice.services.view-elections-details.port"           -> server.port(),
      "microservice.services.view-elections-details.protocol"       -> "http",
      "microservice.services.view-elections-details.host"           -> "localhost",
      "microservice.services.view-elections-details.bearer-token"   -> "local-token",
      "microservice.services.submit-elections-details.port"         -> server.port(),
      "microservice.services.submit-elections-details.protocol"     -> "http",
      "microservice.services.submit-elections-details.host"         -> "localhost",
      "microservice.services.submit-elections-details.bearer-token" -> "local-token",
      "auditing.enabled"                                            -> "false",
      "mongodb.uri"                                                 -> mongoUri
    )
    .build()

  lazy val connector: CADXElectionsConnector = app.injector.instanceOf[CADXElectionsConnector]

  private val submissionUrlPath = "/dac6/SubmitElectionData/v1"

  val sampleCrs   = Some(CRS(HasCARF(Some(true)), HasContracts(Some(true)), HasDormantAccounts(Some(true)), HasThresholds(Some(true))))
  val sampleFatca = Some(FATCA(HasThresholds(Some(true)), HasTreasuryRegulations(Some(true))))

  private val submissionRequestBody = ElectionsRequest(
    requestCommon = RequestElectionsCommon(
      originatingSystem = OriginatingSystem("MDTP"),
      regime = Regime("CRSFATCA"),
      requestParameters = Seq.empty,
      requestType = RequestType("CREATE"),
      transmittingSystem = TransmittingSystem("MDTP")
    ),
    requestDetails = ElectionRequestDetails(
      crs = sampleCrs,
      fatca = sampleFatca,
      fiId = FIID("512345678"),
      reportingPeriod = ReportingPeriod("2024-2024")
    )
  )

  val requiredHeaders = Map(
    "x-forwarded-host" -> "mdtp",
    "x-regime-type"    -> "CRSFATCA",
    "accept"           -> "application/json",
    "content-type"     -> "application/json"
  )

  val errorStatusCodes = Table(
    "errorStatus",
    FORBIDDEN,
    METHOD_NOT_ALLOWED,
    UNPROCESSABLE_ENTITY,
    INTERNAL_SERVER_ERROR,
    SERVICE_UNAVAILABLE
  )

  "CADXElectionConnector" - {
    "list Elections" - {
      "must return an election list" in new TestContext {
        val fiid = "512345678"

        stubResponse(
          url = s"/dac6/ViewElectionData/v1/$fiid",
          statusCode = OK,
          requestMethod = RequestMethod.GET,
          requestHeaders = requiredHeaders,
          responseBody = electionsDetails
        )

        val responseDetails = Await.result(connector.viewElections(fiid), 1.seconds)

        responseDetails.size mustBe 2
        val electionDetail = responseDetails(1)
        val crsElection    = electionDetail.crs.get
        val fatcaElection  = electionDetail.fatca.get

        crsElection.hasCARF mustBe HasCARF(Some(true))
        crsElection.hasContracts mustBe HasContracts(Some(true))
        crsElection.hasDormantAccounts mustBe HasDormantAccounts(Some(true))
        crsElection.hasThresholds mustBe HasThresholds(Some(true))

        fatcaElection.hasThresholds mustBe HasThresholds(Some(true))
        fatcaElection.hasTreasuryRegulations mustBe HasTreasuryRegulations(Some(true))
      }

      "must return an empty election list when a not found status code is returned" in new TestContext {
        val fiid = "512345678"

        stubResponse(
          url = s"/dac6/ViewElectionData/v1/$fiid",
          statusCode = NOT_FOUND,
          requestMethod = RequestMethod.GET,
          requestHeaders = requiredHeaders
        )

        private val responseDetails = Await.result(connector.viewElections(fiid), 1.seconds)

        responseDetails.isEmpty mustBe true
      }

      "must throw an exception for error status codes other than not found and 2XX" in new TestContext {
        val fiid = "512345678"
        forAll(errorStatusCodes) { errorCode =>
          stubResponse(
            url = s"/dac6/ViewElectionData/v1/$fiid",
            statusCode = errorCode,
            requestMethod = RequestMethod.GET,
            requestHeaders = requiredHeaders
          )

          val exception = intercept[Exception](Await.result(connector.viewElections(fiid), 1.seconds))
          exception.isInstanceOf[Exception] mustBe true
        }
      }
    }

    "submit Elections" - {

      "must return the success message for a successful 204 No Content response" in new TestContext {
        stubResponse(
          url = submissionUrlPath,
          statusCode = NO_CONTENT,
          requestMethod = RequestMethod.POST,
          requestHeaders = requiredHeaders,
          responseBody = "Request Processed Successfully"
        )

        val result: String = Await.result(connector.submitElections(submissionRequestBody), 1.seconds)
        result mustBe ""
      }

      "must throw UpstreamErrorResponse for 422 Unprocessable Entity" in new TestContext {
        stubResponse(
          url = submissionUrlPath,
          statusCode = UNPROCESSABLE_ENTITY,
          requestMethod = RequestMethod.POST,
          requestHeaders = requiredHeaders,
          responseBody = "Business validation failure"
        )

        val exception = intercept[UpstreamErrorResponse](Await.result(connector.submitElections(submissionRequestBody), 1.seconds))
        exception.statusCode mustBe UNPROCESSABLE_ENTITY
        exception.message must include("Business validation failure")
      }

      "must throw UpstreamErrorResponse for 400 Bad Request" in new TestContext {
        stubResponse(
          url = submissionUrlPath,
          statusCode = BAD_REQUEST,
          requestMethod = RequestMethod.POST,
          requestHeaders = requiredHeaders,
          responseBody = "Malformed Request"
        )

        val exception = intercept[BadRequestException](Await.result(connector.submitElections(submissionRequestBody), 1.seconds))
        exception.message must include("Malformed Request")
      }

      "must throw UpstreamErrorResponse for 503 Service Unavailable" in new TestContext {
        stubResponse(
          url = submissionUrlPath,
          statusCode = SERVICE_UNAVAILABLE,
          requestMethod = RequestMethod.POST,
          requestHeaders = requiredHeaders,
          responseBody = "Server unavailable"
        )

        val exception = intercept[UpstreamErrorResponse](Await.result(connector.submitElections(submissionRequestBody), 1.seconds))
        exception.statusCode mustBe SERVICE_UNAVAILABLE
        exception.message must include("Server unavailable")
      }
    }
  }
}

trait TestContext {
  val electionsDetails: String =
    """
         {
  "responseCommon": {
    "originatingSystem": "MDTP",
    "regime": "CRFA",
    "requestType": "VIEW",
    "responseParameters": [
      {
        "paramName": "String",
        "paramValue": "String"
      }
    ],
    "transmittingSystem": "EIS"
  },
  "responseDetails": {
    "electionDetails": [
      {
        "crs": {
          "hasCARF": "yes",
          "hasContracts": "yes",
          "hasDormantAccounts": "yes",
          "hasThresholds": "yes"
        },
        "fatca": {
          "hasThresholds": "yes",
          "hasTreasuryRegulations": "yes"
        },
        "reportingPeriod": "2023"
      },
      {
        "crs": {
          "hasCARF": "yes",
          "hasContracts": "yes",
          "hasDormantAccounts": "yes",
          "hasThresholds": "yes"
        },
        "fatca": {
          "hasThresholds": "yes",
          "hasTreasuryRegulations": "yes"
        },
        "reportingPeriod": "{currentYear}"
      }
    ],
    "fiId": "512345678"
  }
}
      """.stripMargin
}
