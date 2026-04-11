Test / parallelExecution := false

organization := "app.softnetwork.scheduler"

name := "scheduler-common"

libraryDependencies ++= Seq(
  "com.cronutils" % "cron-utils" % "9.2.1",
  // session
  "app.softnetwork.session" %% "session-core" % Versions.genericPersistence,
  "app.softnetwork.api" %% "generic-server-api" % Versions.genericPersistence,
  "app.softnetwork.protobuf" %% "scalapb-extensions" % "0.2.0"
)

Compile / unmanagedResourceDirectories += baseDirectory.value / "src/main/protobuf"
