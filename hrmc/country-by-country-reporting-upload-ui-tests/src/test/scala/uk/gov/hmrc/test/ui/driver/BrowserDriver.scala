/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.driver

import org.openqa.selenium.WebDriver
import uk.gov.hmrc.selenium.webdriver.Driver

trait BrowserDriver {

  implicit def driver: WebDriver = Driver.instance

}
