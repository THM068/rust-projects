/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.subscription

import base.SpecBase
import org.scalatest.matchers.must.Matchers.*
import play.api.libs.json.Json

class ContactTypeSpec extends SpecBase {

  "ContactInformation" - {
    "must serialize with Organisation Details" in {
      val contactInformation = ContactInformation(OrganisationDetails("testUser"), "testemail@test.com", Some("01234567890"), Some("09876543210"))
      val expectedJson =
        """{"organisation":{"name":"testUser"},"email":"testemail@test.com","phone":"01234567890","mobile":"09876543210"}"""
      Json.toJson(contactInformation).toString() mustBe expectedJson
    }

    "must deserialize with Organisation Details" in {
      val json =
        """{"organisation":{"name":"testUser"},"email":"testemail@test.com"}"""
      val expected = ContactInformation(OrganisationDetails("testUser"), "testemail@test.com", None, None)

      Json.parse(json).as[ContactInformation] mustEqual expected
    }

    "must serialize with Individual Details" in {
      val contactInformation = ContactInformation(IndividualDetails("testFirstName", "testLastName"), "testemail@test.com", None, None)
      val expectedJson =
        """{"individual":{"firstName":"testFirstName","lastName":"testLastName"},"email":"testemail@test.com"}"""
      Json.toJson(contactInformation).toString() mustBe expectedJson
    }

    "must deserialize with Individual Details" in {
      val json =
        """{"individual":{"firstName":"testFirstName","lastName":"testLastName"},"email":"testemail@test.com","phone":"01234567890"}"""
      val expected = ContactInformation(IndividualDetails("testFirstName", "testLastName"), "testemail@test.com", Some("01234567890"), None)

      Json.parse(json).as[ContactInformation] mustEqual expected
    }

  }
}
