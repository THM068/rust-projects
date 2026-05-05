/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.cucumber.stepdefs

import io.cucumber.datatable.DataTable
import uk.gov.hmrc.test.ui.cucumber.Find.findById
import uk.gov.hmrc.test.ui.cucumber.{Check, Input, Nav, Wait}
import uk.gov.hmrc.test.ui.mongo._
import uk.gov.hmrc.test.ui.pages.AuthLoginPage

class StepDef extends BaseStepDef {

  Given("""^(.*) logs in to access file upload page$""") { name: String =>
    name match {
      case "New User"                          => AuthLoginPage.loginWithNewUserUpload(name)
      case "Existing User with 2 contact"      => AuthLoginPage.loginWithOldUserUploadWith2Contacts(name)
      case "Existing User with 1 contact"      => AuthLoginPage.loginWithOldUserUploadWith1Contact(name)
      case "CBC User"                          => AuthLoginPage.loginWithUserUpload(name)
      case "CBC New Agent with New Client"     => AuthLoginPage.loginWithNewAgentWithNewClient(name)
      case "CBC Agent for fileUpload"          => AuthLoginPage.loginWithAgentForFileUpload(name)
      case "CBC New Agent with Updated Client" => AuthLoginPage.loginWithNewAgentWithUpdatedClient(name)
      case "Individual User"                   => AuthLoginPage.loginWithIndividualUser(name)
      case "Org User with agent affinity"      => AuthLoginPage.OrgUserWithAgentAffinity(name)
      case "CBC Agent for Non-UK fileUpload"   => AuthLoginPage.loginWithAgentForNonUKFileUpload(name)
    }
  }

  Given("""^The Heading should be (.*)$""") { header: String =>
    Check.checkH1(header)
  }

  Then("""^organisation user trying to access agent contact details page$""") { page: String =>
    Nav.navigateTo(
      "http://localhost:10024/send-a-country-by-country-report/agent/agent-contact-details/contact-needed"
    )

  }

  And(
    """^(click Continue button|click Send button|click Confirm and send|click Upload an XML file button)$"""
  ) { (negate: String) =>
    Input.clickSubmit()
  }

  Then("""^I enter (.*) in (.*)$""") { (text: String, id: String) =>
    Input.sendKeysById(text, id)
  }

  And("""^I select (.*) and continue$""") { (id: String) =>
    Input.clickById(id)
    Input.clickSubmit()
  }

  And("""^I click (.*)$""") { (id: String) =>
    Input.clickByLinkText(id)
  }

  And("""^click (.*) element$""") { (id: String) =>
    Input.clickById(id)
  }

  Then("""^The Page should include (.*)$""") { text: String =>
    Check.checkBodyText(text)

  }

  Then("""wait for (.*) seconds$""") { (secs: Int) =>
    Wait.secondsWait(secs)
  }

  Then("""^The Subheading should be (.*)$""") { header: String =>
    Check.checkSubHeading(header)
  }

  Then("""^The subheading becomes (.*)$""") { header: String =>
    Check.checkUploadSubheading(header)
  }

  Given("""^the user should be on the new window with title "([^"]*)" page""") { (title: String) =>
    Input.switchToNewWindow
    Check.checkH1(title)
  }

  Then("""^The error table should show the following errors$""") { data: DataTable =>
    val row = data.asMaps(classOf[String], classOf[String]).iterator
    while (row.hasNext) {
      val map   = row.next
      val line  = map.get("line").toString
      val error = map.get("error").toString

      findById(s"lineNumber_$line").getText   shouldBe line
      findById(s"errorMessage_$line").getText shouldBe error

    }
  }

  When("""^I browse and upload "([^"]*)"$""") { (file: String) =>
    if (file != "")
      findById("file-upload").sendKeys(System.getProperty("user.dir") + "/src/test/resources/files/" + file)

  }

  Then("""^The Business rule errors table should show the following errors$""") { data: DataTable =>
    val row = data.asMaps(classOf[String], classOf[String]).iterator
    while (row.hasNext) {
      val map      = row.next
      val code     = map.get("code").toString
      val docRefId = map.get("docRefId").toString
      val error    = map.get("errorMessage").toString

      findById(s"code_$code").getText         shouldBe code
      findById(s"docRefId_$code").getText     shouldBe docRefId
      findById(s"errorMessage_$code").getText shouldBe error

    }
  }

  And("""^the mongo query is run to insert collections for Submission-file with (.*)$""") { (fileType: String) =>
    val database   = "country-by-country-reporting"
    val collection = "file-details"

    if (fileType == "Pending Status") {
      MongoService.insertSubmissionFile(PendingStatus.data, database, collection)
    } else if (fileType == "Received Status") {
      MongoService.insertSubmissionFile(ReceivedStatus.data, database, collection)
    } else if (fileType == "Rejected Status") {
      MongoService.insertSubmissionFile(RejectedStatus.data, database, collection)
    } else if (fileType == "All Status") {
      MongoService.insertSubmissionFile(PendingStatus.data, database, collection)
      MongoService.insertSubmissionFile(ReceivedStatus.data, database, collection)
      MongoService.insertSubmissionFile(RejectedStatus.data, database, collection)
      MongoService.insertSubmissionFile(ProblemStatus.data, database, collection)
    } else if (fileType == "All Status - Organisation") {
      MongoService.insertSubmissionFile(PendingStatusOrganisation.data, database, collection)
      MongoService.insertSubmissionFile(ReceivedStatusOrganisation.data, database, collection)
      MongoService.insertSubmissionFile(RejectedStatusOrganisation.data, database, collection)
      MongoService.insertSubmissionFile(ProblemStatusOrganisation.data, database, collection)
    }
  }

  And("""^set the file status to (.*) with ([^"]*)$""") { (status: String, code: String) =>
    val database       = "country-by-country-reporting"
    val collection     = "file-details"
    val conversationId = MongoService.getCollectionData(database, collection)("_id").asString().getValue
    if (status == "accepted") {
      MongoService.setStatus(database, collection, conversationId, Accepted.data)
    } else if (status == "rejected") {
      MongoService.setStatus(
        database,
        collection,
        conversationId,
        code match {
          case "FileErrors"                                => FileErrors.data
          case "RecordErrors"                              => RecordErrors.data
          case "CustomErrors"                              => CustomErrors.data
          case "CustomErrorsWithNewMessageOrNoErrorDetail" => CustomErrorsWithNewMessageOrNoErrorDetail.data
          case "AllErrors"                                 => AllErrors.data
        }
      )
    } else if (status == "problem") {
      MongoService.setStatus(database, collection, conversationId, ProblemStatusErrors.data)
    }
  }

  And("""^the mongo query to drop the submission-detail""") {
    val database   = "country-by-country-reporting"
    val collection = "file-details"
    MongoService.dropMongoCollection(database, collection)
  }

  Given("""^the user should be on the new window with heading (.*)""") { (title: String) =>
    Input.switchToNewWindow
    Check.checkH1(title)
  }

  Then("""^I Browser Back""") {
    Input.clickBrowserBack()
  }

  Then("""^The URL should include (.*)$""") { text: String =>
    Check.checkUrlContains(text)
  }

}
