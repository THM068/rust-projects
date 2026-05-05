/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.cucumber.runner

import io.cucumber.junit.{Cucumber, CucumberOptions}
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("src/test/resources/features"),
  glue = Array("uk.gov.hmrc.test.ui.cucumber.stepdefs"),
  plugin = Array("pretty", "html:target/cucumber.html", "json:target/cucumber.json"),
  tags = "@solo"
)
class SoloRunner {}
