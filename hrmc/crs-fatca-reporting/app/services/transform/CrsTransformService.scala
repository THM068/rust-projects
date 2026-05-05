/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services.transform

import com.google.inject.{Inject, Singleton}
import models.submission.SubmissionMetaData
import models.subscription.CrfaSubscriptionDetails
import models.validation.SaxParseError
import services.validation.XMLValidationService

import java.time.format.DateTimeFormatter
import scala.xml.*

@Singleton
class CrsTransformService @Inject() (val xmlValidationService: XMLValidationService) extends XmlTransformCommonTags {

  private val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def transformAndValidate(uploadedXml: NodeSeq, meta: SubmissionMetaData, subscription: CrfaSubscriptionDetails): Either[List[SaxParseError], Elem] = {
    val payload           = extractCrsPayload(uploadedXml)
    val normalisedPayload = CrsXmlNamespaceNormaliser.normalise(payload)

    val transformedPayload = build(normalisedPayload, meta, subscription)
    Right(transformedPayload) // TODO: DAC6-4075 - validate transformed payload
  }

  private def extractCrsPayload(uploadedXml: NodeSeq): Elem =
    val maybe =
      uploadedXml
        .collectFirst { case e: Elem if e.label == "CRS_OECD" => e }
        .orElse(uploadedXml.collectFirst { case e: Elem => e })

    maybe.getOrElse {
      throw new IllegalArgumentException("Uploaded XML contained no root element to transform.")
    }

  private def build(uploadedPayload: NodeSeq, meta: SubmissionMetaData, subscription: CrfaSubscriptionDetails): Elem = {

    val schemaLocation =
      Seq(
        "http://www.hmrc.gsi.gov.uk/fatca/cadx CrsXML_v3.0.xsd",
        "urn:oecd:ties:crs:v3 CrsXML_v3.0.xsd",
        "urn:oecd:ties:commontypesfatcacrs:v2 CommonTypesFatcaCrs_v2.0.xsd",
        "urn:oecd:ties:isocrstypes:v1 isocrstypes_v1.1.xsd",
        "urn:oecd:ties:fatca:v1 FatcaTypes_v1.2.xsd",
        "urn:oecd:ties:crsstf:v5 oecdcrstypes_v5.0.xsd"
      ).mkString("         ")

    <cadx:CRSSubmissionRequest
      xmlns:cadx="http://www.hmrc.gsi.gov.uk/fatca/cadx"
      xmlns:crs="urn:oecd:ties:crs:v3"
      xmlns:cfc="urn:oecd:ties:commontypesfatcacrs:v2"
      xmlns:stf="urn:oecd:ties:crsstf:v5"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation={schemaLocation}>
      {requestCommon(meta)}
      <requestDetail>{uploadedPayload}</requestDetail>
      {requestAdditionalDetail(meta, subscription)}
    </cadx:CRSSubmissionRequest>
  }

  private def requestCommon(meta: SubmissionMetaData): Elem =
    <requestCommon>
      <receiptDate>
        {dateTimeFormat.format(meta.submissionTime)}
      </receiptDate>
      <regime>CRS</regime>
      <conversationID>
        {meta.conversationId.value}
      </conversationID>
      <schemaVersion>
        3.0
      </schemaVersion>
    </requestCommon>

}
