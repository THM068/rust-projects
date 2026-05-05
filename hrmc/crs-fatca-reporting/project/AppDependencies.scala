import sbt.*

object AppDependencies {
  private val bootstrapVersion = "9.19.0"
  private val hmrcMongoVersion = "2.10.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                 % "2.13.0"
  )

  val test = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalamock"          %% "scalamock"               % "7.5.2",
    "org.scalatestplus"    %% "scalacheck-1-17"         % "3.2.18.0",
    "io.github.wolfendale" %% "scalacheck-gen-regexp"   % "1.1.0"
  ).map(_ % Test)

  val it = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
  )
}
