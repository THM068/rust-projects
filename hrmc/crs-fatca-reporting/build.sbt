import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.5"

lazy val microservice = Project("crs-fatca-reporting", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    PlayKeys.playDefaultPort := 10037,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-release",
      "11",
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=.*/Routes.scala:s",
      "-Wconf:src=.*/RoutesPrefix.scala:s",
      "-Wconf:src=.*/ReverseRoutes.scala:s",
      "-Wconf:src=.*/test/.*:s",
      "-Wconf:cat=deprecation:s"
    ),
    scalacOptions := scalacOptions.value.distinct
  )
  .settings(CodeCoverageSettings.settings: _*)
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    DefaultBuildSettings.itSettings(),
    scalacOptions := scalacOptions.value.distinct
  )
  .settings(libraryDependencies ++= AppDependencies.it)
