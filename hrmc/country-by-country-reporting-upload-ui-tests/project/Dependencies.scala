import sbt._

object Dependencies {

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "ui-test-runner"     % "0.42.0" % Test,
    "ch.qos.logback"       % "logback-classic"    % "1.5.6"  % Test,
    "com.vladsch.flexmark" % "flexmark-all"       % "0.64.8" % Test,
    "org.scalatest"       %% "scalatest"          % "3.2.19" % Test,
    "io.cucumber"         %% "cucumber-scala"     % "8.23.1" % Test,
    "io.cucumber"          % "cucumber-junit"     % "7.18.1" % Test,
    "junit"                % "junit"              % "4.13.2" % Test,
    "com.novocode"         % "junit-interface"    % "0.11"   % Test,
    "com.typesafe"         % "config"             % "1.4.3"  % Test,
    "org.mongodb.scala"   %% "mongo-scala-driver" % "5.1.0"  % Test
  )

}
