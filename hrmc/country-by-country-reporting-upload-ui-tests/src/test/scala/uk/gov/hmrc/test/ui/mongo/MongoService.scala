/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.ui.mongo

import java.util.concurrent.TimeUnit
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{MongoClient, MongoCollection, Observable}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.{Filters, Updates}
import org.mongodb.scala.model.Indexes.descending

import scala.Console.println
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
object MongoService {

  private val timeout: FiniteDuration = 10.seconds

  def dropMongoCollection(db: String, collection: String): Unit = {

    val mongoClient: MongoClient = MongoClient()

    try {
      println("Starting drop...")
      val res = Await.result(
        mongoClient
          .getDatabase(db)
          .getCollection(collection)
          .drop()
          .head(),
        timeout
      )
      println("Drop complete: " + res)
    } catch {
      case e: Exception =>
        println("Error: " + e)
        mongoClient.close()
    } finally {
      println("Closing connection...")
      mongoClient.close()
    }
  }

  def insertSubmissionFile(source: List[String], database: String, collection: String): Unit = {

    val mongoClient: MongoClient = MongoClient()

    try {

      val db  = mongoClient.getDatabase("country-by-country-reporting")
      val col = db.getCollection("file-details")
      source.map { e =>
        val doc = Document(e)
        Await.result(
          col.insertOne(doc).toFutureOption().map { _ =>
            println(s"-- inserted $e")
          //          Done
          },
          timeout
        )
      }
    } catch {
      case ex: Exception => println("Bummer, an exception happened.")
    } finally {
      println("-- closing connection")
      mongoClient.close()
    }
  }

  def getCollectionData(dbName: String, collectionName: String): Document = {
    val mongoClient                           = MongoClient()
    val collection: MongoCollection[Document] = mongoClient
      .getDatabase(dbName)
      .getCollection(collectionName)
    val observable: Observable[Document]      = collection.find().sort(descending("lastUpdated"))
    def document(): Document                  = Await.result(observable.head(), Duration(10, TimeUnit.SECONDS))
    val retrievedRecord                       = document()
    mongoClient.close()
    retrievedRecord
  }

  def setStatus(dbName: String, collection: String, id: String, status: String) = {
    val mongoClient: MongoClient = MongoClient()
    val query                    = Filters.eq("_id", id)
    val updateStatus             = Updates.set("status", BsonDocument(status))
    Await.result(
      mongoClient
        .getDatabase(dbName)
        .getCollection(collection)
        .updateOne(query, updateStatus)
        .head(),
      2 seconds
    )
    mongoClient.close()
  }

}
