/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services.transform

import scala.xml.*

object CrsXmlNamespaceNormaliser {

  final case class CrsNamespaceSpec(prefix: String, uri: String)

  val expected: Seq[CrsNamespaceSpec] = Seq(
    CrsNamespaceSpec("cadx", "http://www.hmrc.gsi.gov.uk/fatca/cadx"),
    CrsNamespaceSpec("crs", "urn:oecd:ties:crs:v3"),
    CrsNamespaceSpec("cfc", "urn:oecd:ties:commontypesfatcacrs:v2"),
    CrsNamespaceSpec("stf", "urn:oecd:ties:crsstf:v5"),
    CrsNamespaceSpec("xsi", "http://www.w3.org/2001/XMLSchema-instance"),
    CrsNamespaceSpec("sfa", "urn:oecd:ties:isocrstypes:v1"),
    CrsNamespaceSpec("ftc", "urn:oecd:ties:fatca:v1")
  )

  private val uriToPrefix: Map[String, String] =
    expected.map(ns => ns.uri -> ns.prefix).toMap

  def expectedScope: NamespaceBinding =
    expected.foldRight(TopScope: NamespaceBinding) { (ns, acc) =>
      NamespaceBinding(ns.prefix, ns.uri, acc)
    }

  def normalise(elem: Elem): Elem =
    def loop(n: Node, inheritedPrefix: Option[String]): Node =
      n match
        case e: Elem =>
          val uri = e.namespace
          val forcedPrefix =
            Option(uri).flatMap(uriToPrefix.get).orElse(inheritedPrefix).orNull

          val newChildren = e.child.map(ch => loop(ch, Option(forcedPrefix)))

          e.copy(
            prefix = forcedPrefix,
            scope = expectedScope,
            child = newChildren
          )

        case other => other

    loop(elem, None).asInstanceOf[Elem]
}
