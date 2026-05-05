/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package base

import org.scalatest.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.*
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import models.financialinstitutions.{AddressDetails, FIDetail, TINType}

import java.time.{Clock, Instant, ZoneId}

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with GuiceOneAppPerSuite
    with OptionValues
    with EitherValues
    with TryValues
    with ScalaFutures
    with MockitoSugar
    with BeforeAndAfterEach
    with BeforeAndAfterAll {

  def injector: Injector = app.injector

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val fixedClock: Clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        Configuration("metrics.enabled" -> "false", "enrolmentKeys.crsfatca.key" -> "HMRC-FATCA-ORG", "enrolmentKeys.crsfatca.identifier" -> "FATCAID")
      )
      .overrides()

  val testFiDetails: Seq[FIDetail] =
    Seq(
      FIDetail(
        "683373339",
        "First FI",
        "[subscriptionId]",
        List.empty,
        Some("689355555"),
        IsFIUser = true,
        IsFATCAReporting = true,
        AddressDetails = AddressDetails(
          AddressLine1 = "22",
          AddressLine2 = Some("Test Street"),
          AddressLine3 = Some("Test City"),
          AddressLine4 = Some("Test City"),
          CountryCode = Some("GB"),
          PostalCode = Some("TEST 2RE")
        ),
        PrimaryContactDetails = Some(
          models.financialinstitutions.ContactDetails(
            ContactName = "Test User One",
            EmailAddress = "testuserone@example.com",
            PhoneNumber = Some("0444458888")
          )
        ),
        SecondaryContactDetails = Some(
          models.financialinstitutions.ContactDetails(ContactName = "Test User Two", EmailAddress = "testusertwo@example.com", PhoneNumber = Some("0333458888"))
        )
      ),
      FIDetail(
        "683373300",
        "Second FI",
        "[subscriptionId]",
        List.empty,
        Some("689344444"),
        IsFIUser = false,
        IsFATCAReporting = true,
        AddressDetails = AddressDetails(
          AddressLine1 = "22",
          AddressLine2 = Some("Test Street"),
          AddressLine3 = Some("Test Town"),
          AddressLine4 = Some("Test Town"),
          CountryCode = Some("GB"),
          PostalCode = Some("TF22 2RE")
        ),
        PrimaryContactDetails = Some(
          models.financialinstitutions.ContactDetails(ContactName = "Test User Three",
                                                      EmailAddress = "testuserthree@example.com",
                                                      PhoneNumber = Some("0223458888")
          )
        ),
        SecondaryContactDetails = Some(
          models.financialinstitutions.ContactDetails(ContactName = "Test User Four",
                                                      EmailAddress = "testuserfour@example.com",
                                                      PhoneNumber = Some("0123456789")
          )
        )
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
              "AddressLine3": "Test City",
              "AddressLine4": "Test City",
              "CountryCode": "GB",
              "PostalCode": "TEST 2RE"
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
              "PostalCode": "TF22 2RE"
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
  }
"""
}
