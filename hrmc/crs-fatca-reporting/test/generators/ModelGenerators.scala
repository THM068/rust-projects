/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package generators

import models.sdes.{Algorithm, Audit, Checksum, FileTransferNotification, Property}
import models.submission.{ConversationId, MessageSpecData, MessageType, SubmissionDetails}
import models.subscription.{ContactInformation, CrfaSubscriptionDetails, OrganisationDetails}
import models.upscan.UploadId
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

trait ModelGenerators {
  self: Generators =>

  implicit val arbitraryAudit: Arbitrary[Audit] = Arbitrary {
    nonEmptyString.map(Audit.apply)
  }

  implicit val arbitraryProperty: Arbitrary[Property] = Arbitrary {
    for {
      name  <- nonEmptyString
      value <- nonEmptyString
    } yield Property(name, value)
  }

  implicit val arbitraryAlgorithm: Arbitrary[Algorithm] = Arbitrary(Gen.oneOf(Algorithm.values))

  implicit val arbitraryCheckSum: Arbitrary[Checksum] = Arbitrary {
    for {
      algorithm <- Arbitrary.arbitrary[Algorithm]
      value     <- nonEmptyString
    } yield Checksum(algorithm, value)
  }

  implicit val arbitraryFile: Arbitrary[models.sdes.File] = Arbitrary {
    for {
      recipientOrSender <- Arbitrary.arbitrary[Option[String]]
      name              <- nonEmptyString
      location          <- arbitrary[Option[String]]
      checksum          <- arbitrary[Checksum]
      size              <- arbitrary[Int]
      properties        <- arbitrary[List[Property]]
    } yield models.sdes.File(recipientOrSender, name, location, checksum, size, properties)
  }

  implicit val arbitraryFileTransferNotification: Arbitrary[FileTransferNotification] = Arbitrary {
    for {
      informationType <- nonEmptyString
      file            <- arbitrary[models.sdes.File]
      audit           <- arbitrary[Audit]
    } yield FileTransferNotification(informationType, file, audit)
  }

  implicit val arbitraryOrganisationDetails: Arbitrary[OrganisationDetails] = Arbitrary {
    for {
      orgName <- nonEmptyString
    } yield OrganisationDetails(orgName)
  }

  implicit val arbitraryContactInformation: Arbitrary[ContactInformation] = Arbitrary {
    for {
      contactType <- arbitrary[OrganisationDetails]
      email       <- validEmailAddress
      phone       <- Gen.option(validContactNumber)
      mobile      <- Gen.option(validContactNumber)
    } yield ContactInformation(contactType, email, phone, mobile)
  }

  implicit val arbitraryConversationId: Arbitrary[ConversationId] = Arbitrary {
    Gen.uuid.map(uuid => ConversationId.fromUploadId(UploadId.apply(uuid.toString)))
  }

  implicit val arbitraryResponseDetail: Arbitrary[CrfaSubscriptionDetails] = Arbitrary {
    for {
      subscriptionId   <- validSubscriptionID
      tradingName      <- Gen.option(nonEmptyString)
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[ContactInformation]
      secondaryContact <- Gen.option(arbitrary[ContactInformation])
    } yield CrfaSubscriptionDetails(subscriptionId, tradingName, isGBUser, primaryContact, secondaryContact)
  }

  implicit val arbitraryMessageSpecData: Arbitrary[MessageSpecData] = Arbitrary {
    for {
      messageType       <- Gen.oneOf(MessageType.values)
      sendingCompanyIN  <- nonEmptyString
      messageRefId      <- nonEmptyString
      reportingFIName   <- nonEmptyString
      reportingPeriod   <- arbitrary[LocalDate]
      giin              <- Gen.option(nonEmptyString)
      fiNameFromFim     <- nonEmptyString
      electionsRequired <- arbitrary[Boolean]

    } yield MessageSpecData(messageType, sendingCompanyIN, messageRefId, reportingFIName, reportingPeriod, giin, fiNameFromFim, electionsRequired)
  }

  implicit val arbitrarySubmissionDetails: Arbitrary[SubmissionDetails] = Arbitrary {
    for {
      file            <- arbitrary[models.sdes.File]
      uploadId        <- nonEmptyString
      enrolmentId     <- nonEmptyString
      documentUrl     <- nonEmptyString
      messageSpecData <- arbitrary[MessageSpecData]
    } yield SubmissionDetails(file.name, UploadId(uploadId), enrolmentId, file.size, documentUrl, file.checksum.value, messageSpecData)
  }

  implicit val arbitraryMessageType: Arbitrary[MessageType] = Arbitrary {
    Gen.oneOf(MessageType.values)
  }
}
