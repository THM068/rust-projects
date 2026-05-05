/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.cucumber

import org.openqa.selenium.support.ui.{ExpectedConditions, FluentWait}
import org.openqa.selenium.{By, WebDriver, WebElement}
import uk.gov.hmrc.test.ui.pages.BasePage

import java.time.Duration

object Wait extends BasePage {
  val url = ""

  def fluentWait: FluentWait[WebDriver] = new FluentWait[WebDriver](driver)
    .withTimeout(Duration.ofSeconds(20))
    .pollingEvery(Duration.ofMillis(250))
    .ignoring(classOf[org.openqa.selenium.NoSuchElementException])

  def waitForElement(id: String): WebElement = waitForElement(By.id(id))

  def waitForElements(h1: String): WebElement = waitForElement(By.tagName(h1))

  private def waitForElement(by: By): WebElement = fluentWait.until(ExpectedConditions.presenceOfElementLocated(by))

  def secondsWait(secs: Int): Unit = Thread.sleep(secs.*(1000))

}
