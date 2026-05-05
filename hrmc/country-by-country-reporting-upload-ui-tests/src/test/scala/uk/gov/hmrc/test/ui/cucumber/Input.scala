/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.cucumber

import org.openqa.selenium.WebDriver
import uk.gov.hmrc.test.ui.cucumber.Find.{findByCss, findById, findByLinkText, findByName, findByXpath}
import uk.gov.hmrc.test.ui.pages.BasePage

object Input extends BasePage {

  def clickById(id: String): Unit = findById(id).click()

  def clickByLinkText(text: String): Unit = findByLinkText(text).click()

  def clickByCss(css: String): Unit = findByCss(css).click()

  def clickAndContinue(id: String): Unit = {
    findById(id).click()
    clickSubmit()
  }

  def clickSubmit() = findById("submit").click()

  def clickContinue() = findByXpath("/html/body/div/main/div/div/div/div/a/button").click()

  def clickByXpath(id: String): Unit = findByXpath(id).click()

  def sendKeysById(value: String, id: String): Unit = {
    findById(id)
    findById(id).clear()
    findById(id).sendKeys(value)
  }

  def sendKeysByName(value: String, name: String): Unit = {
    findByName(name)
    findByName(name).clear()
    findByName(name).sendKeys(value)
  }

  def fileUpload(value: String, id: String): Unit =
    findById(id).sendKeys(System.getProperty("user.dir") + "/src/test/resources/files/")

  def switchToNewWindow: WebDriver = {
    val handles   = driver.getWindowHandles.toArray().toSeq
    val newWindow = handles(1).toString
    driver.close()
    driver.switchTo().window(newWindow)
  }

  def clickBrowserBack(): Unit = driver.navigate().back()

}
