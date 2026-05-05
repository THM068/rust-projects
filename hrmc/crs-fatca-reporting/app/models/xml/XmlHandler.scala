/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.xml

import com.google.inject.Inject

import java.net.URI
import javax.inject.Singleton
import scala.util.Try
import scala.xml.Elem
import scala.xml.factory.XMLLoader
@Singleton
class XmlHandler @Inject() {

  def load(url: String): Try[Elem] = {
    val loader: XMLLoader[Elem] = new XMLLoader[Elem] {
      override def adapter = new scala.xml.parsing.NoBindingFactoryAdapter
    }
    Try(loader.load(URI.create(url).toURL))
  }
}
