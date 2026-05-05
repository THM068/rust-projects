/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.cucumber

import org.scalatest.Assertion
import uk.gov.hmrc.test.ui.pages.BasePage

object Check extends BasePage {

  val url = ""

  def checkH1(h1: String): Assertion = Find.findByTagName("h1").getText should include(h1)

  def checkID(id: String): Boolean = Find.findById(id).isDisplayed

  def checkUrlContains(url: String): Assertion = Find.findURL() should include(url)

  def checkIDContains(id: String, text: String): Assertion = Find.findById(id).getText should include(text)

  def checkBodyText(t: String): Assertion = Find.findByTagName("body").getText should include(t)

  def checkSubHeading(p: String) =
    Find.findByCss("#main-content > div > div > p:nth-child(2)").getText should include(p)

  def checkUploadSubheading(p: String) =
    Find.findByCss("#processing > h2").getText should include(p)

}
