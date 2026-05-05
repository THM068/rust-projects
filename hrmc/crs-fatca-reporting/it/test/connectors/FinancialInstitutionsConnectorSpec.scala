/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package connectors

import models.financialinstitutions.{AddressDetails, FIDetail, TINDetails, TINType}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.*
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.crsfatcareporting.utils.ISpecBase

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class FinancialInstitutionsConnectorSpec extends AnyFreeSpec with ISpecBase  {

  override lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.crs-fatca-fi-management.port" -> server.port(),
      "microservice.services.crs-fatca-fi-management.bearer-token" -> "local-token",
      "auditing.enabled"                                           -> "false",
      "mongodb.uri"                                                -> mongoUri
    )
    .build()

  lazy val connector: FinancialInstitutionsConnector =
    app.injector.instanceOf[FinancialInstitutionsConnector]

  "FinancialInstitutionsConnector" - {
    "must return status as OK for viewFIs" in new TestContext {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId",
        OK,
        body = testViewFIDetailsBody
      )
      val result = Await.result(connector.viewFis(subscriptionId), 2.seconds)
      result.status mustBe OK
    }

    "must return status as OK for viewFi on success" in new TestContext {
      val subscriptionId = "XE512345678"
      val fiid = testFiDetails.head.FIID
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiid",
        OK,
        body = testViewFIDetailsBody
      )
      val result = Await.result(connector.viewFi(subscriptionId, fiid), 2.seconds)
      result.status mustBe OK
    }

    "must return HttpResponse with error status for viewFi on failure" in new TestContext {
      val subscriptionId = "XE512345678"
      val fiid = testFiDetails.head.FIID
      val errorBody = "Error details"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiid",
        INTERNAL_SERVER_ERROR,
        body = errorBody
      )
      val result = Await.result(connector.viewFi(subscriptionId, fiid), 2.seconds)
      result.status mustBe INTERNAL_SERVER_ERROR
      result.body mustBe errorBody
    }

    "must return status as NoContent for updateFi on success" in new TestContext {
      val fiToUpdate = testFiDetails.head.copy(GIIN = Some("NEWUPDATEDGIIN"))
      val updateUrl = "/crs-fatca-fi-management/financial-institutions/update"

      stubPutResponse(
        updateUrl,
        NO_CONTENT,
        body = "",
        requestBody = Some(toJson(fiToUpdate).toString())
      )

      val result = Await.result(connector.updateFi(fiToUpdate), 2.seconds)

      result.status mustBe NO_CONTENT
    }

    "must return HttpResponse with error status for updateFi on failure" in new TestContext {
      val fiToUpdate = testFiDetails.head.copy(GIIN = Some("NEWUPDATEDGIIN"))
      val updateUrl = "/crs-fatca-fi-management/financial-institutions/update"
      val errorBody = "Update failed: Bad Request"

      stubPutResponse(
        updateUrl,
        BAD_REQUEST,
        body = errorBody,
        requestBody = Some(toJson(fiToUpdate).toString())
      )

      val result = Await.result(connector.updateFi(fiToUpdate), 2.seconds)

      result.status mustBe BAD_REQUEST
      result.body mustBe errorBody
    }

  }

  trait TestContext {

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

    val testViewFIDetailsBody =
      """{
    "ViewFIDetails": {
      "ResponseDetails": {
        "FIDetails": [
          {
            "FIID": "683373339",
            "FIName": "First FI",
            "SubscriptionID": "[subscriptionId]",
            "TINDetails": [],
            "GIIN": "689355555",
            "IsFIUser": true,
             "IsFATCAReporting": true,
            "AddressDetails": {
              "AddressLine1": "22",
              "AddressLine2": "Test Street",
              "AddressLine3": "Test Town",
              "AddressLine4": "Test Town",
              "CountryCode": "GB",
              "PostalCode": "Test 2RE"
            },
            "PrimaryContactDetails": {
              "ContactName": "User one",
              "EmailAddress": "userone@example.com",
              "PhoneNumber": "0444458888"
            },
            "SecondaryContactDetails": {
              "ContactName": "User Two",
              "EmailAddress": "usertwo@example.com",
              "PhoneNumber": "0333458888"
            }
          },
          {
            "FIID": "683373300",
            "FIName": "Second FI",
            "SubscriptionID": "[subscriptionId]",
            "TINDetails": [],
            "GIIN": "689344444",
            "IsFIUser": false,
            "IsFATCAReporting": true,
            "AddressDetails": {
              "AddressLine1": "22",
              "AddressLine2": "Test Street",
              "AddressLine3": "Test Town",
              "AddressLine4": "Test Town",
              "CountryCode": "GB",
              "PostalCode": "Test 2RE"
            },
            "PrimaryContactDetails": {
              "ContactName": "User two",
              "EmailAddress": "usertwo@example.com",
              "PhoneNumber": "0223458888"
            },
            "SecondaryContactDetails": {
              "ContactName": "User Three",
              "EmailAddress": "userthree@example.com",
              "PhoneNumber": "0123456789"
            }
          }
        ]
      }
    }
  }
"""
  }
}
