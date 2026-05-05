/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.cucumber

import uk.gov.hmrc.test.ui.pages.BasePage

object Nav extends BasePage {
  val url = ""

  def navigateTo(url: String): Unit =
    driver.navigate.to(url)

  def browserBack(): Unit =
    driver.navigate().back()
}
