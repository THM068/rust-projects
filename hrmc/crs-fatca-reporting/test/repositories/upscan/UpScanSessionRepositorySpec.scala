/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package repositories.upscan

import base.SpecBase
import config.AppConfig
import models.upscan._
import org.bson.types.ObjectId
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalatest.matchers.must.Matchers._
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Clock, Instant, ZoneId}
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class UpScanSessionRepositorySpec extends SpecBase with DefaultPlayMongoRepositorySupport[UploadSessionDetails] {

  private val uploadId         = UploadId(UUID.randomUUID().toString)
  private val instant          = Instant.now.truncatedTo(java.time.temporal.ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val uploadDetails = UploadSessionDetails(
    ObjectId.get(),
    uploadId,
    Reference("xxxx"),
    Quarantined,
    Instant.ofEpochSecond(1)
  )

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  override protected val repository: UpScanSessionRepository = new UpScanSessionRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  ".insert" - {

    "must insert UploadStatus" in {

      val setResult = repository.insert(uploadDetails).futureValue
      val record    = find(Filters.equal("_id", uploadDetails._id)).futureValue.headOption.value

      setResult mustEqual true
      record mustEqual uploadDetails
    }

    ".findByUploadId" - {

      "when there is a record for this id" - {

        "must get the record" in {

          insert(uploadDetails).futureValue

          val result = repository.findByUploadId(uploadId).futureValue

          result.value mustEqual uploadDetails
        }
      }

      "when there is no record for this id" - {

        "must return None" in {

          repository.findByUploadId(UploadId("id that does not exist")).futureValue must not be defined
        }
      }
    }
  }

  ".updateStatus" - {
    "must update the status and the lastUpdated time" in {
      insert(uploadDetails).futureValue

      val result = repository.updateStatus(Reference("xxxx"), Failed).futureValue
      result mustEqual true

      val expectedResult = uploadDetails copy (status = Failed, lastUpdated = instant)
      val record         = find(Filters.equal("_id", uploadDetails._id)).futureValue.headOption.value
      record mustEqual expectedResult
    }
  }
}
