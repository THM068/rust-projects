/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package services.transform

import models.submission.SubmissionMetaData
import models.subscription.{ContactInformation, CrfaSubscriptionDetails, OrganisationDetails}

import scala.xml.{Elem, Node}

trait XmlTransformCommonTags {
  def requestAdditionalDetail(
    meta: SubmissionMetaData,
    subscription: CrfaSubscriptionDetails
  ): Elem =
    <requestAdditionalDetail>
  {
      opt(meta.fileName.map(n => <fileName>{n}</fileName>))
    }<subscriptionID>{subscription.crfaReference}</subscriptionID>{
      opt(
        subscription.tradingName
          .filter(_.trim.nonEmpty)
          .map(tn => <tradingName>{tn}</tradingName>)
      )
    }<isManual>true</isManual>
  <isGBUser>{subscription.gbUser.toString}</isGBUser>
  <primaryContact>{contact(subscription.primaryContact)}</primaryContact>{
      opt(subscription.secondaryContact.map(sc => <secondaryContact>
  {contact(sc)}
</secondaryContact>))
    }
</requestAdditionalDetail>

  def contact(ci: ContactInformation): Seq[Node] =
    Seq(
      opt(ci.phone.map(p => <phoneNumber>{p}</phoneNumber>)),
      opt(ci.mobile.map(m => <mobileNumber>{m}</mobileNumber>)),
      Seq(
        <emailAddress>{ci.email}</emailAddress>
      ),
      ci.contactInformation match {
        case OrganisationDetails(orgName) =>
          Seq(
            <organisationDetails>
              <organisationName>{orgName}</organisationName>
            </organisationDetails>
          )
        case _ => Seq.empty
      }
    ).flatten

  def opt[A](o: Option[A]): Seq[A] = o.toSeq
}
