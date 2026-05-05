/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package services.transform

import com.google.inject.{Inject, Singleton}
import models.submission.SubmissionMetaData
import models.subscription.CrfaSubscriptionDetails
import models.validation.SaxParseError
import services.validation.XMLValidationService

import java.time.format.DateTimeFormatter
import scala.xml.{Elem, NodeSeq}

@Singleton
class FatcaTransformService @Inject() (val xmlValidationService: XMLValidationService) extends XmlTransformCommonTags {
  private val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def transformAndValidate(uploadedXml: NodeSeq, meta: SubmissionMetaData, subscription: CrfaSubscriptionDetails): Either[List[SaxParseError], Elem] = {
    val payload           = extractFatcaPayload(uploadedXml)
    val normalisedPayload = FatcaXmlNamespaceNormaliser.normalise(payload)

    val transformedPayload = build(normalisedPayload, meta, subscription)
    Right(transformedPayload) // TODO: DAC6-4078- validate transformed payload
  }

  private def extractFatcaPayload(uploadedXml: NodeSeq): Elem = {
    val maybe =
      uploadedXml
        .collectFirst { case e: Elem if e.label == "FATCA_OECD" => e }
        .orElse(uploadedXml.collectFirst { case e: Elem => e })

    maybe.getOrElse {
      throw new IllegalArgumentException("Uploaded XML contained no root element to transform.")
    }
  }

  private def build(uploadedPayload: NodeSeq, meta: SubmissionMetaData, subscription: CrfaSubscriptionDetails): Elem = {
    val schemaLocation =
      Seq(
        "urn:oecd:ties:fatca:v2 FatcaXML_v2.0.xsd",
        "urn:oecd:ties:commontypesfatcacrs:v2 isofatcatypes_v1.1.xsd",
        "urn:oecd:ties:stf:v2 stffatcatypes_v2.0.xsd",
        "urn:oecd:ties:oecdtypes:v4 oecdtypes_v4.2.xsd"
      ).mkString("         ")

    <fatca:FATCASubmissionRequest
      xmlns:fatca="urn:oecd:ties:fatca:v2"
      xmlns:oecd="urn:oecd:ties:stf:v2"
      xmlns:iso="urn:oecd:ties:commontypesfatcacrs:v2"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation={schemaLocation}>
      {requestCommon(meta)}
      <requestDetail>{uploadedPayload}</requestDetail>
      {requestAdditionalDetail(meta, subscription)}
    </fatca:FATCASubmissionRequest>
  }

  private def requestCommon(meta: SubmissionMetaData): Elem =
    <requestCommon>
      <receiptDate>{dateTimeFormat.format(meta.submissionTime)}</receiptDate>
      <regime>FATCA</regime>
      <conversationID>{meta.conversationId.value}</conversationID>
      <schemaVersion>2.0</schemaVersion>
    </requestCommon>

}
