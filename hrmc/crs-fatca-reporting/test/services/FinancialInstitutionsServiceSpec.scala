/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services;

import base.SpecBase
import connectors.FinancialInstitutionsConnector
import models.financialinstitutions.FIDetail
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers._
import play.api.http.Status.OK
import play.api.libs.json.JsResultException
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class FinancialInstitutionsServiceSpec extends SpecBase {
  val mockConnector: FinancialInstitutionsConnector = mock[FinancialInstitutionsConnector]
  val testObj                                       = new FinancialInstitutionsService(mockConnector)

  implicit override val hc: HeaderCarrier = HeaderCarrier()

  "FinancialInstitutionsService" - {
    "getListOfFinancialInstitutions extracts list of FI details" in {
      val subscriptionId = "XE5123456789"
      val mockResponse   = Future.successful(HttpResponse(OK, testViewFIDetailsBody))

      when(mockConnector.viewFis(subscriptionId)).thenReturn(mockResponse)
      val result: Future[Seq[FIDetail]] = testObj.getListOfFinancialInstitutions(subscriptionId)
      result.futureValue mustBe testFiDetails
    }

    "getListOfFinancialInstitutions returns empty list when 422 with error code 001" in {
      val subscriptionId = "XE5123456789"
      val body           = """{"errorDetail": {"errorCode": "001", "errorMessage": "No matching records found"}}"""
      val mockResponse   = Future.successful(HttpResponse(422, body))

      when(mockConnector.viewFis(subscriptionId)).thenReturn(mockResponse)
      val result: Future[Seq[FIDetail]] = testObj.getListOfFinancialInstitutions(subscriptionId)
      result.futureValue mustBe Seq.empty
    }

    "getListOfFinancialInstitutions throws exception when non 2xx or 422 response" in {

      val subscriptionId = "XE5123456789"
      val body           = """{"errorDetail": {"errorCode": "002", "errorMessage": "Some other error"}}"""
      val mockResponse   = Future.successful(HttpResponse(500, body))

      when(mockConnector.viewFis(subscriptionId)).thenReturn(mockResponse)
      val result: Future[Seq[FIDetail]] = testObj.getListOfFinancialInstitutions(subscriptionId)
      an[RuntimeException] must be thrownBy result.futureValue
    }

    "getListOfFinancialInstitutions throws JsResultException" in {
      val subscriptionId = "XE5123456789"
      val mockResponse = Future.successful(
        HttpResponse(
          OK,
          """{
                                                                |  "ViewFIDetails": "string instead of object",
                                                                |  "FIDetails": {
                                                                |    "ResponseDetails": null
                                                                |  }
                                                                |}""".stripMargin
        )
      )

      when(mockConnector.viewFis(subscriptionId)).thenReturn(mockResponse)
      val exception = intercept[JsResultException] {
        Await.result(testObj.getListOfFinancialInstitutions(subscriptionId), 10.seconds)
      }
      exception.errors should not be empty
    }
  }
}
