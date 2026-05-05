/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.pages

import org.openqa.selenium.support.ui.Select
import uk.gov.hmrc.test.ui.conf.TestConfiguration
import uk.gov.hmrc.test.ui.cucumber._

object AuthLoginPage extends BasePage {
  val url: String                          = TestConfiguration.url("auth-login-stub") + "/gg-sign-in"
  val redirectUrlField: String             = "redirectionUrl"
  val uploadUrl: String                    = TestConfiguration.url("country-by-country-reporting-frontend")
  val enrolmentKeyField                    = "enrolment[0].name"
  val enrolmentKeyFieldValue               = "HMRC-CBC-ORG"
  val identifierNameField                  = "enrolment[0].taxIdentifier[0].name"
  val identifierNameValue                  = "cbcId"
  val identifierValueField                 = "enrolment[0].taxIdentifier[0].value"
  val identifierValueNew                   = "XACBC0000123777"
  val identifierValueExistingWith2contacts = "XACBC0000123778"
  val identifierValueExistingWith1contact  = "XACBC0000123779"

  val enrolmentKeyFieldAgent                    = "enrolment[1].name"
  val enrolmentKeyFieldValueAgent               = "HMRC-AS-AGENT"
  val identifierNameFieldAgent                  = "enrolment[1].taxIdentifier[0].name"
  val identifierNameValueAgent                  = "AgentReferenceNumber"
  val identifierValueFieldAgent                 = "enrolment[1].taxIdentifier[0].value"
  val identifierValueNewAgent                   = "ARN123777"
  val identifierValueExistingAgentWith2contacts = "ARN123778"
  val identifierValueExistingAgentWith1contact  = "ARN123779"

  val identifierDelegatedEnrolmentKeyField    = "delegatedEnrolment[0].key"
  val identifierDelegatedEnrolmentKey         = "HMRC-CBC-ORG"
  val identifierDelegatedNonUKEnrolmentKey    = "HMRC-CBC-NONUK-ORG"
  val identifierNameFieldDelegated            = "delegatedEnrolment[0].taxIdentifier[0].name"
  val identifierNameDelegate                  = "cbcId"
  val identifierValueFieldDelegated           = "delegatedEnrolment[0].taxIdentifier[0].value"
  val identifierValueDelegatedFirstTimeClient = "XACBC0000123777"
  val identifierValueDelegatedUpdatedClient   = "XACBC0000123778"

  val delegatedAuthRuleField = "delegatedEnrolment[0].delegatedAuthRule"
  val delegatedAuthRuleValue = "cbc-auth"

  def loginWithNewUserUpload(name: String): Unit = {
    Nav.navigateTo(url)
    Input.sendKeysByName(uploadUrl, redirectUrlField)
    Input.sendKeysByName(enrolmentKeyFieldValue, enrolmentKeyField)
    Input.sendKeysByName(identifierNameValue, identifierNameField)
    Input.sendKeysByName(identifierValueNew, identifierValueField)
    selectAffinityGroupOrg()
    clickSubmitButton()
  }

  def loginWithOldUserUploadWith2Contacts(name: String): Unit = {
    Nav.navigateTo(url)
    Input.sendKeysByName(uploadUrl, redirectUrlField)
    Input.sendKeysByName(enrolmentKeyFieldValue, enrolmentKeyField)
    Input.sendKeysByName(identifierNameValue, identifierNameField)
    Input.sendKeysByName(identifierValueExistingWith2contacts, identifierValueField)
    selectAffinityGroupOrg()
    clickSubmitButton()
  }

  def loginWithOldUserUploadWith1Contact(name: String): Unit = {
    Nav.navigateTo(url)
    Input.sendKeysByName(uploadUrl, redirectUrlField)
    Input.sendKeysByName(enrolmentKeyFieldValue, enrolmentKeyField)
    Input.sendKeysByName(identifierNameValue, identifierNameField)
    Input.sendKeysByName(identifierValueExistingWith1contact, identifierValueField)
    selectAffinityGroupOrg()
    clickSubmitButton()
  }

  def loginWithUserUpload(name: String): Unit = {
    Nav.navigateTo(url)
    Input.sendKeysByName(uploadUrl, redirectUrlField)
    Input.sendKeysByName(enrolmentKeyFieldValue, enrolmentKeyField)
    Input.sendKeysByName(identifierNameValue, identifierNameField)
    Input.sendKeysByName(identifierValueExistingWith1contact, identifierValueField)
    selectAffinityGroupOrg()
    clickSubmitButton()
  }

  def loginWithNewAgentWithNewClient(name: String): Unit = {
    Nav.navigateTo(url)
    Input.sendKeysByName(uploadUrl, redirectUrlField)
    selectAffinityGroupAgent()
    Input.sendKeysByName(enrolmentKeyFieldValueAgent, enrolmentKeyFieldAgent)
    Input.sendKeysByName(identifierNameValueAgent, identifierNameFieldAgent)
    Input.sendKeysByName(identifierValueNewAgent, identifierValueFieldAgent)

    addDelegatedEnrolment()
    Input.sendKeysByName(identifierDelegatedEnrolmentKey, identifierDelegatedEnrolmentKeyField)
    Input.sendKeysByName(identifierNameDelegate, identifierNameFieldDelegated)
    Input.sendKeysByName(identifierValueDelegatedFirstTimeClient, identifierValueFieldDelegated)
    Input.sendKeysByName(delegatedAuthRuleValue, delegatedAuthRuleField)

    clickSubmitButton()
  }

  def loginWithNewAgentWithUpdatedClient(name: String): Unit = {
    Nav.navigateTo(url)
    Input.sendKeysByName(uploadUrl, redirectUrlField)
    selectAffinityGroupAgent()
    Input.sendKeysByName(enrolmentKeyFieldValueAgent, enrolmentKeyFieldAgent)
    Input.sendKeysByName(identifierNameValueAgent, identifierNameFieldAgent)
    Input.sendKeysByName(identifierValueNewAgent, identifierValueFieldAgent)

    addDelegatedEnrolment()
    Input.sendKeysByName(identifierDelegatedEnrolmentKey, identifierDelegatedEnrolmentKeyField)
    Input.sendKeysByName(identifierNameDelegate, identifierNameFieldDelegated)
    Input.sendKeysByName(identifierValueDelegatedUpdatedClient, identifierValueFieldDelegated)
    Input.sendKeysByName(delegatedAuthRuleValue, delegatedAuthRuleField)

    clickSubmitButton()
  }

  def loginWithAgentForFileUpload(name: String): Unit = {

    Nav.navigateTo(url)
    Input.sendKeysByName(uploadUrl, redirectUrlField)
    selectAffinityGroupAgent()
    Input.sendKeysByName(enrolmentKeyFieldValueAgent, enrolmentKeyFieldAgent)
    Input.sendKeysByName(identifierNameValueAgent, identifierNameFieldAgent)
    Input.sendKeysByName(identifierValueExistingAgentWith2contacts, identifierValueFieldAgent)

    addDelegatedEnrolment()
    Input.sendKeysByName(identifierDelegatedEnrolmentKey, identifierDelegatedEnrolmentKeyField)
    Input.sendKeysByName(identifierNameDelegate, identifierNameFieldDelegated)
    Input.sendKeysByName(identifierValueDelegatedUpdatedClient, identifierValueFieldDelegated)
    Input.sendKeysByName(delegatedAuthRuleValue, delegatedAuthRuleField)

    clickSubmitButton()
  }

  def loginWithIndividualUser(name: String): Unit = {
    Nav.navigateTo(url)
    Input.sendKeysByName(uploadUrl, redirectUrlField)
    Input.sendKeysByName(enrolmentKeyFieldValue, enrolmentKeyField)
    Input.sendKeysByName(identifierNameValue, identifierNameField)
    Input.sendKeysByName(identifierValueExistingWith2contacts, identifierValueField)

    Input.sendKeysByName(enrolmentKeyFieldValueAgent, enrolmentKeyFieldAgent)
    Input.sendKeysByName(identifierNameValueAgent, identifierNameFieldAgent)
    Input.sendKeysByName(identifierValueNewAgent, identifierValueFieldAgent)
    selectAffinityGroupInd()
    clickSubmitButton()
  }

  def OrgUserWithAgentAffinity(name: String): Unit = {
    Nav.navigateTo(url)
    Input.sendKeysByName(uploadUrl, redirectUrlField)
    Input.sendKeysByName(enrolmentKeyFieldValue, enrolmentKeyField)
    Input.sendKeysByName(identifierNameValue, identifierNameField)
    Input.sendKeysByName(identifierValueExistingWith2contacts, identifierValueField)
    selectAffinityGroupAgent()
    clickSubmitButton()
  }

  def loginWithAgentForNonUKFileUpload(name: String): Unit = {

    Nav.navigateTo(url)
    Input.sendKeysByName(uploadUrl, redirectUrlField)
    selectAffinityGroupAgent()
    Input.sendKeysByName(enrolmentKeyFieldValueAgent, enrolmentKeyFieldAgent)
    Input.sendKeysByName(identifierNameValueAgent, identifierNameFieldAgent)
    Input.sendKeysByName(identifierValueExistingAgentWith2contacts, identifierValueFieldAgent)

    addDelegatedEnrolment()
    Input.sendKeysByName(identifierDelegatedNonUKEnrolmentKey, identifierDelegatedEnrolmentKeyField)
    Input.sendKeysByName(identifierNameDelegate, identifierNameFieldDelegated)
    Input.sendKeysByName(identifierValueDelegatedUpdatedClient, identifierValueFieldDelegated)
    Input.sendKeysByName(delegatedAuthRuleValue, delegatedAuthRuleField)

    clickSubmitButton()
  }

  private def selectAffinityGroupAgent() =
    new Select(findAffinityGroup()).selectByVisibleText("Agent")

  private def selectAffinityGroupOrg() =
    new Select(findAffinityGroup()).selectByVisibleText("Organisation")

  private def selectAffinityGroupInd() =
    new Select(findAffinityGroup()).selectByVisibleText("Individual")

  private def findAffinityGroup() = Find.findByName("affinityGroup")

  def addDelegatedEnrolment() = Find.findById("js-add-delegated-enrolment").click()

  def clickSubmitButton() = Find.findById("submit").click()

}
