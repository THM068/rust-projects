/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.cucumber.stepdefs

import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.Eventually
import uk.gov.hmrc.test.ui.driver.BrowserDriver
import io.cucumber.scala.{EN, ScalaDsl}

trait BaseStepDef extends ScalaDsl with EN with BrowserDriver with Eventually with Matchers {}
