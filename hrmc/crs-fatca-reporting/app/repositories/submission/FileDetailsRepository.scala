/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package repositories.submission

import config.AppConfig
import models.submission.{ConversationId, FileDetails, FileStatus}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.{Clock, LocalDateTime}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileDetailsRepository @Inject() (
  val mongo: MongoComponent,
  appConfig: AppConfig
)(implicit ec: ExecutionContext, clock: Clock)
    extends PlayMongoRepository[FileDetails](
      mongoComponent = mongo,
      collectionName = "file-details",
      domainFormat = FileDetails.mongoFormat,
      indexes = Seq(
        IndexModel(
          ascending("lastUpdated"),
          IndexOptions()
            .name("submission-last-updated-index")
            .expireAfter(appConfig.submissionTtl, TimeUnit.DAYS)
        ),
        IndexModel(ascending("enrolmentId"),
                   IndexOptions()
                     .name("enrolmentId-index")
                     .unique(false)
        ),
        IndexModel(ascending("status"),
                   IndexOptions()
                     .name("status-index")
                     .unique(false)
        )
      ),
      replaceIndexes = true
    ) {

  def updateStatus(
    conversationId: ConversationId,
    newStatus: FileStatus
  ): Future[Option[FileDetails]] = {

    val filter: Bson = equal("_id", conversationId.value)
    val modifier = Updates.combine(
      set("status", Codecs.toBson(newStatus)),
      set("lastUpdated", LocalDateTime.now(clock))
    )
    val options: FindOneAndUpdateOptions =
      FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)

    collection
      .findOneAndUpdate(filter, modifier, options)
      .toFutureOption()
  }

  def findByConversationId(conversationId: ConversationId): Future[Option[FileDetails]] = {
    val filter: Bson = equal("_id", conversationId.value)
    collection
      .find(filter)
      .first()
      .toFutureOption()
  }

  def findStatusByConversationId(conversationId: ConversationId): Future[Option[FileStatus]] = {
    val filter: Bson = equal("_id", conversationId.value)
    collection
      .find(filter)
      .first()
      .toFutureOption()
      .map(_.map(_.status))
  }

  def findByEnrolmentId(enrolmentId: String): Future[Seq[FileDetails]] = {
    val filter: Bson = equal("enrolmentId", enrolmentId)
    collection
      .find(filter)
      .toFuture()
  }

  def insert(fileDetails: FileDetails): Future[Unit] =
    collection
      .replaceOne(
        filter = equal("_id", fileDetails._id.value),
        replacement = fileDetails,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => ())

}
