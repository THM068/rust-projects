/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.cucumber

import org.junit.Assert
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}
import uk.gov.hmrc.test.ui.pages.BasePage
import uk.gov.hmrc.test.ui.cucumber.Wait.fluentWait

object Find extends BasePage {

  val url = ""

  private def find(by: By): WebElement = {
    fluentWait.until(ExpectedConditions.presenceOfElementLocated(by))
    driver.findElement(by)
  }

  def elementIsNotDisplayed(id: String): Unit = {
    val checkId = driver.getPageSource
    Assert.assertFalse(checkId.contains(id))
  }

  def findById(id: String): WebElement = find(By.id(id))

  def findByName(name: String): WebElement = find(By.name(name))

  def findByCss(css: String): WebElement = find(By.cssSelector(css))

  def findByXpath(id: String): WebElement = find(By.xpath(id))

  def findByTagName(tagName: String): WebElement = find(By.tagName(tagName))

  def findByLinkText(text: String): WebElement = find(By.linkText(text))

  def findURL(): String = driver.getCurrentUrl

  def findH1(): WebElement = findByTagName("h1")

}
