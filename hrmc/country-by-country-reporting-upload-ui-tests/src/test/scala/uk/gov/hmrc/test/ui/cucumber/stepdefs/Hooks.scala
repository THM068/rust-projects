/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.cucumber.stepdefs

import io.cucumber.scala.{EN, ScalaDsl, Scenario}
import org.openqa.selenium.{OutputType, TakesScreenshot}
import uk.gov.hmrc.selenium.webdriver.Browser
import uk.gov.hmrc.test.ui.driver.BrowserDriver

object Hooks extends ScalaDsl with EN with Browser with BrowserDriver {
  Before {
    startBrowser()
  }

  After { scenario: Scenario =>
    if (scenario.isFailed) {
      val screenshotName = scenario.getName.replaceAll(" ", "_")
      val screenshot     = driver.asInstanceOf[TakesScreenshot].getScreenshotAs(OutputType.BYTES)
      scenario.attach(screenshot, "image/png", screenshotName)
    }
    quitBrowser()
  }

}
