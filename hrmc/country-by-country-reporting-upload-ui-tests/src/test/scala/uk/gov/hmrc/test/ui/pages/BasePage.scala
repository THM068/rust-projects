/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.pages

import org.openqa.selenium.By
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.test.ui.driver.BrowserDriver

trait BasePage extends BrowserDriver with Matchers {
  val continueButton = "continue-button"

  def submitPage(): Unit =
    driver.findElement(By.id(continueButton)).click()

  def onPage(pageTitle: String): Unit =
    if (driver.getTitle != pageTitle)
      throw PageNotFoundException(
        s"Expected '$pageTitle' page, but found '${driver.getTitle}' page."
      )
}

case class PageNotFoundException(s: String) extends Exception(s)
