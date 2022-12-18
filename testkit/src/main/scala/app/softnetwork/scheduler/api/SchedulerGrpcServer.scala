package app.softnetwork.scheduler.api

import akka.http.scaladsl.testkit.PersistenceScalatestGrpcTest
import app.softnetwork.persistence.scalatest.InMemoryPersistenceTestKit
import app.softnetwork.scheduler.launch.SchedulerGuardian
import org.scalatest.Suite

trait SchedulerGrpcServer
    extends PersistenceScalatestGrpcTest
    with SchedulerGrpcServices
    with InMemoryPersistenceTestKit { _: Suite with SchedulerGuardian =>
  override lazy val additionalConfig: String = grpcConfig
}
