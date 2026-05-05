/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package services.transform
import scala.xml.*

object FatcaXmlNamespaceNormaliser {

  final case class FatcaNamespaceSpec(prefix: String, uri: String)

  val expected: Seq[FatcaNamespaceSpec] = Seq(
    FatcaNamespaceSpec("fatca", "urn:oecd:ties:fatca:v2"),
    FatcaNamespaceSpec("oecd", "urn:oecd:ties:stf:v2"),
    FatcaNamespaceSpec("iso", "urn:oecd:ties:commontypesfatcacrs:v2"),
    FatcaNamespaceSpec("xsi", "http://www.w3.org/2001/XMLSchema-instance")
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
