/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services.validation

import models.validation.{InvalidXmlError, SaxParseError, SubmissionValidationResult}
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler

import java.io.StringReader
import java.net.{URI, URL}
import javax.inject.Inject
import javax.xml.XMLConstants
import javax.xml.parsers.{SAXParser, SAXParserFactory}
import javax.xml.validation.Schema
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq}
import scala.xml.factory.XMLLoader
import scala.xml.parsing.NoBindingFactoryAdapter

class XMLValidationService @Inject() ()(implicit ec: ExecutionContext) {

  private val schemaLang: String = javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI

  def loadXml(upScanUrl: String): Future[Either[SubmissionValidationResult, Elem]] = parseXml(upScanUrl)
    .map {
      case Right(elem) => Right(elem)
      case Left(_)     => Left(InvalidXmlError("Invalid Xml"))
    }
    .recover { case t: Throwable =>
      Left(InvalidXmlError(s"XML parsing failed. The XML parser has thrown the exception: $t"))
    }

  def validate(xml: NodeSeq, filePath: String): Either[List[SaxParseError], Elem] = {
    val list: ListBuffer[SaxParseError] = new ListBuffer[SaxParseError]
    val loadedXML                       = xmlLoader(filePath, list).load(new StringReader(xml.mkString))
    if (list.isEmpty) Right(loadedXML)
    else Left(list.toList)
  }

  private def xmlValidatingParser(schema: Schema): SAXParser = {

    val factory: SAXParserFactory = SAXParserFactory.newInstance()
    factory.setSchema(schema)
    factory.setNamespaceAware(true)
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    factory.setXIncludeAware(false)
    factory.newSAXParser()
  }

  private def xmlLoader(filePath: String, errorList: ListBuffer[SaxParseError]): XMLLoader[Elem] = {

    val url: URL       = getClass.getResource(filePath)
    val schema: Schema = javax.xml.validation.SchemaFactory.newInstance(schemaLang).newSchema(url)
    trait AccumulatorState extends DefaultHandler {
      override def warning(e: SAXParseException): Unit    = errorList += SaxParseError(e.getLineNumber, e.getMessage)
      override def error(e: SAXParseException): Unit      = errorList += SaxParseError(e.getLineNumber, e.getMessage)
      override def fatalError(e: SAXParseException): Unit = errorList += SaxParseError(e.getLineNumber, e.getMessage)
    }

    new scala.xml.factory.XMLLoader[scala.xml.Elem] {
      override def parser: SAXParser = xmlValidatingParser(schema)
      override def adapter           = new scala.xml.parsing.NoBindingFactoryAdapter with AccumulatorState
    }
  }

  private def nonValidatingParser(): SAXParser = {
    val f = SAXParserFactory.newInstance()
    f.setNamespaceAware(true)
    f.setValidating(false)

    def tryFeature(uri: String, value: Boolean): Unit = try f.setFeature(uri, value)
    catch {
      case _: Throwable => ()
    }

    tryFeature(XMLConstants.FEATURE_SECURE_PROCESSING, value = true)
    tryFeature("http://apache.org/xml/features/disallow-doctype-decl", value = true)
    tryFeature("http://xml.org/sax/features/external-general-entities", value = false)
    tryFeature("http://xml.org/sax/features/external-parameter-entities", value = false)
    tryFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", value = false)
    f.setXIncludeAware(false)
    f.newSAXParser()
  }

  private def parseXml(upScanUrl: String): Future[Either[List[SaxParseError], Elem]] = Future {
    val errors = ListBuffer.empty[SaxParseError]

    val loader: XMLLoader[Elem] = new XMLLoader[Elem] {
      override def parser: SAXParser = nonValidatingParser()

      override def adapter: NoBindingFactoryAdapter =
        new NoBindingFactoryAdapter {
          override def warning(e: SAXParseException): Unit =
            errors += SaxParseError(e.getLineNumber, e.getMessage)

          override def error(e: SAXParseException): Unit =
            errors += SaxParseError(e.getLineNumber, e.getMessage)

          override def fatalError(e: SAXParseException): Unit =
            errors += SaxParseError(e.getLineNumber, e.getMessage)
        }
    }

    val xml = loader.load(URI.create(upScanUrl).toURL)
    if (errors.isEmpty) Right(xml) else Left(errors.toList)
  }
}
