package app.softnetwork.scheduler.api

import akka.http.scaladsl.testkit.PersistenceScalatestGrpcTest
import app.softnetwork.persistence.scalatest.InMemoryPersistenceTestKit
import app.softnetwork.scheduler.launch.SchedulerGuardian
import org.scalatest.Suite

trait SchedulerGrpcServerTestKit
    extends PersistenceScalatestGrpcTest
    with SchedulerGrpcServicesTestKit
    with InMemoryPersistenceTestKit { _: Suite with SchedulerGuardian =>
  override lazy val additionalConfig: String = schedulerGrpcConfig
}
