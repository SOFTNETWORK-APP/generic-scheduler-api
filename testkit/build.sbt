import app.softnetwork.sbt.build.Versions

Test / parallelExecution := false

organization := "app.softnetwork.scheduler"

name := "scheduler-testkit"

libraryDependencies ++= Seq(
  "app.softnetwork.api" %% "generic-server-api-testkit" % Versions.server,
  "app.softnetwork.persistence" %% "persistence-core-testkit" % Versions.genericPersistence
)
