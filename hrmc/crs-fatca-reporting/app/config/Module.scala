/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package config

import com.google.inject.AbstractModule
import services.upscan.{MongoBackedUploadProgressTracker, UploadProgressTracker}

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[UploadProgressTracker]).to(classOf[MongoBackedUploadProgressTracker])
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
    bind(classOf[AppConfig]).asEagerSingleton()
  }
}
