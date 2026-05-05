/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services

import models.financialinstitutions.{AddressDetails, FIDetail, TINDetails, TINType}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.*
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.crsfatcareporting.utils.ISpecBase

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class FinancialInstitutionServiceSpec extends AnyFreeSpec with ISpecBase {

  override lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.crs-fatca-fi-management.port" -> server.port(),
      "microservice.services.crs-fatca-fi-management.bearer-token" -> "local-token",
      "auditing.enabled"                                           -> "false",
      "mongodb.uri"                                                -> mongoUri
    )
    .build()

  lazy val service: FinancialInstitutionsService =
    app.injector.instanceOf[FinancialInstitutionsService]

  "FinancialInstitutionsService" - {
    "must return all financial institutions" in new TestContext {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId",
        OK,
        body = testViewFIDetailsBody
      )
      val result = Await.result(service.getListOfFinancialInstitutions(subscriptionId), 2.seconds)
      result.size mustBe 2L
      result mustBe testFiDetails
    }

    "must return empty list when no matching records are found" in new TestContext {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId",
        422,
        body = noMatchingRecords
      )
      val result = Await.result(service.getListOfFinancialInstitutions(subscriptionId), 2.seconds)
      result.size mustBe 0
      result mustBe Seq.empty
    }

    "must throw RuntimeException for non 2xx or 422 responses" in new TestContext {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId",
        500,
        body = """{"errorDetail": {"errorCode": "002", "errorMessage": "Some other error"}}"""
      )
      val result = service.getListOfFinancialInstitutions(subscriptionId)
      an[Exception] must be thrownBy Await.result(result, 2.seconds)
    }

    "updateFiWithGiin" - {
      val subscriptionId  = "XEFATCA000000001"
      val fiidToUpdate    = "683373339"
      val newGiin         = "A11111.99999.SL.826"
      val errorMessage    = "Downstream system failure"

      "must successfully update GIIN when view and update calls succeed" in new TestContext {
        val expectedUpdatedFiDetail = testFiDetails.head.copy(GIIN = Some(newGiin))

        stubGetResponse(
          s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiidToUpdate",
          OK,
          body = testViewSingleFIDetailsBody
        )

        stubPutResponse(
          url = "/crs-fatca-fi-management/financial-institutions/update",
          status = NO_CONTENT,
          requestBody = Some(Json.toJson(expectedUpdatedFiDetail).toString)
        )

        val result = service.updateFiWithGiin(subscriptionId, fiidToUpdate, newGiin)
        Await.result(result, 2.seconds) mustBe ((): Unit)
      }

      "must throw RuntimeException if the initial viewFi call fails" in new TestContext {
        stubGetResponse(
          s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiidToUpdate",
          INTERNAL_SERVER_ERROR,
          body = errorMessage
        )

        val result = service.updateFiWithGiin(subscriptionId, fiidToUpdate, newGiin)

        val exception = the[RuntimeException] thrownBy Await.result(result, 2.seconds)
        exception.getMessage must include("Failed to view FI details")
        exception.getMessage must include(INTERNAL_SERVER_ERROR.toString)
      }

      "must throw RuntimeException if viewFi succeeds but returns an empty list" in new TestContext {
        stubGetResponse(
          s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiidToUpdate",
          OK,
          body = testViewEmptyFIDetailsBody
        )

        val result = service.updateFiWithGiin(subscriptionId, fiidToUpdate, newGiin)

        val exception = the[RuntimeException] thrownBy Await.result(result, 2.seconds)
        exception.getMessage must include(s"No FI found for $fiidToUpdate in FI-Management response")
      }

      "must throw RuntimeException if the updateFi call fails" in new TestContext {
        stubGetResponse(
          s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiidToUpdate",
          OK,
          body = testViewSingleFIDetailsBody
        )

        stubPutResponse(
          url = "/crs-fatca-fi-management/financial-institutions/update",
          status = BAD_REQUEST
        )

        val result = service.updateFiWithGiin(subscriptionId, fiidToUpdate, newGiin)

        val exception = the[RuntimeException] thrownBy Await.result(result, 2.seconds)
        exception.getMessage must include("Failed to update FI details")
        exception.getMessage must include(BAD_REQUEST.toString)
      }
    }

    trait TestContext {
      val noMatchingRecords = """{"errorDetail": {"errorCode": "001", "errorMessage": "No matching records found"}}"""

      val testFiDetails: Seq[FIDetail] =
        Seq(
          FIDetail(
            "683373339",
            "First FI",
            "[subscriptionId]",
            List(TINDetails(TINType = TINType.UTR, TIN = "1234567890", IssuedBy = "GB")),
            Some("689355555"),
            IsFIUser = true,
            IsFATCAReporting = true,
            AddressDetails = AddressDetails(
              AddressLine1 = "22",
              AddressLine2 = Some("Test Street"),
              AddressLine3 = Some("Test City"),
              AddressLine4 = Some("Test City"),
              CountryCode = Some("GB"),
              PostalCode = Some("TEST BBB")
            ),
            PrimaryContactDetails = Some(
              models.financialinstitutions.ContactDetails(
                ContactName = "Test User One",
                EmailAddress = "testuserone@example.com",
                PhoneNumber = Some("0444458888")
              )),
            SecondaryContactDetails = Some(
              models.financialinstitutions.ContactDetails(
                ContactName = "Test User Two",
                EmailAddress = "testusertwo@example.com",
                PhoneNumber = Some("0333458888")))
          ),
          FIDetail(
            "683373300",
            "Second FI",
            "[subscriptionId]",
            List(TINDetails(TINType = TINType.UTR, TIN = "1234567890", IssuedBy = "GB")),
            Some("689344444"),
            IsFIUser = false,
            IsFATCAReporting = true,
            AddressDetails = AddressDetails(
              AddressLine1 = "22",
              AddressLine2 = Some("Test Street"),
              AddressLine3 = Some("Test Town"),
              AddressLine4 = Some("Test Town"),
              CountryCode = Some("GB"),
              PostalCode = Some("TEST BBB")
            ),
            PrimaryContactDetails = Some(
              models.financialinstitutions.ContactDetails(
                ContactName = "Test User Three",
                EmailAddress = "testuserthree@example.com",
                PhoneNumber = Some("0223458888"))),
            SecondaryContactDetails = Some(
              models.financialinstitutions.ContactDetails(
                ContactName = "Test User Four",
                EmailAddress = "testuserfour@example.com",
                PhoneNumber = Some("0123456789")))
          )
        )

      private val fiDetail1Json =
        """{
            "FIID": "683373339",
            "FIName": "First FI",
            "SubscriptionID": "[subscriptionId]",
            "TINDetails": [
            {
              "TINType": "UTR",
              "TIN": "1234567890",
              "IssuedBy": "GB"
            }
           ],
            "GIIN": "689355555",
            "IsFIUser": true,
            "IsFATCAReporting": true,
            "AddressDetails": {
              "AddressLine1": "22",
              "AddressLine2": "Test Street",
              "AddressLine3": "Test City",
              "AddressLine4": "Test City",
              "CountryCode": "GB",
              "PostalCode": "TEST BBB"
            },
            "PrimaryContactDetails": {
              "ContactName": "Test User One",
              "EmailAddress": "testuserone@example.com",
              "PhoneNumber": "0444458888"
            },
            "SecondaryContactDetails": {
              "ContactName": "Test User Two",
              "EmailAddress": "testusertwo@example.com",
              "PhoneNumber": "0333458888"
            }
          }"""

      val testViewSingleFIDetailsBody =
        s"""{
            "ViewFIDetails": {
              "ResponseDetails": {
                "FIDetails": [
                  $fiDetail1Json
                ]
              }
            }
          }"""

      val testViewEmptyFIDetailsBody =
        """{
            "ViewFIDetails": {
              "ResponseDetails": {
                "FIDetails": []
              }
            }
          }"""

      val testViewFIDetailsBody =
        s"""{
            "ViewFIDetails": {
              "ResponseDetails": {
                "FIDetails": [
                  $fiDetail1Json,
                  {
                    "FIID": "683373300",
                    "FIName": "Second FI",
                    "SubscriptionID": "[subscriptionId]",
                    "TINDetails": [
                    {
              "TINType": "UTR",
              "TIN": "1234567890",
              "IssuedBy": "GB"
            }
          ],"GIIN": "689344444",
                    "IsFIUser": false,
                    "IsFATCAReporting": true,
            "AddressDetails": {
                      "AddressLine1": "22",
                      "AddressLine2": "Test Street",
                      "AddressLine3": "Test Town",
                      "AddressLine4": "Test Town",
                      "CountryCode": "GB",
                      "PostalCode": "TEST BBB"
                    },
                    "PrimaryContactDetails": {
                      "ContactName": "Test User Three",
                      "EmailAddress": "testuserthree@example.com",
                      "PhoneNumber": "0223458888"
                    },
                    "SecondaryContactDetails": {
                      "ContactName": "Test User Four",
                      "EmailAddress": "testuserfour@example.com",
                      "PhoneNumber": "0123456789"
                    }
                  }
                ]
              }
            }
          }"""
    }
  }
}