import app.softnetwork.sbt.build.Versions

Test / parallelExecution := false

organization := "app.softnetwork.scheduler"

name := "scheduler-common"

libraryDependencies ++= Seq(
  "com.markatta" %% "akron" % "1.2" excludeAll(ExclusionRule(organization = "com.typesafe.akka"), ExclusionRule(organization = "org.scala-lang.modules")),
  // session
  "app.softnetwork.session" %% "session-core" % Versions.session,
  "app.softnetwork.api" %% "generic-server-api" % Versions.server,
  "app.softnetwork.protobuf" %% "scalapb-extensions" % "0.1.5"
)

Compile / unmanagedResourceDirectories += baseDirectory.value / "src/main/protobuf"
