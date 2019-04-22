import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

lazy val akkaHttpVersion = "10.+"
lazy val akkaVersion    = "2.5.11"
lazy val json4sVersion    = "3.5.3"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "swaggetty-yolognese",
      scalaVersion    := "2.12.4",
      version         := "0.0.1",
      mainClass in Compile := Some("swaggetty.AppEntryPoint"),
      ScalariformKeys.preferences := scalariformPref.value
    )),
    name := "swaggetty-tictactoe",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"               % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"           % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"             % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"              % akkaVersion,
      "de.heikoseeberger" %% "akka-http-json4s"        % "1.+",

      "org.json4s"        %% "json4s-native"           % json4sVersion,
      "org.json4s"        %% "json4s-ext"              % json4sVersion,

      "org.specs2"        %% "specs2-core"             % "3.8.6" % Test,
      "org.specs2"        %% "specs2-mock"             % "3.8.6" % Test,
      "org.specs2"        %% "specs2-matcher-extra"    % "3.8.6" % Test,
      "com.typesafe.akka" %% "akka-http-testkit"       % akkaHttpVersion % Test,

      "ch.qos.logback"    %  "logback-classic"         % "1.2.+",
      "com.typesafe.scala-logging" %% "scala-logging"  % "3.5.+"

    )
  )

lazy val scalariformPref = Def.setting {
  ScalariformKeys.preferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
    .setPreference(CompactStringConcatenation, true)
}
