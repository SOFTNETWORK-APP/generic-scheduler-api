package app.softnetwork.scheduler.scalatest

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.query.{InMemoryJournalProvider, InMemoryOffsetProvider}
import app.softnetwork.persistence.scalatest.InMemoryPersistenceTestKit
import app.softnetwork.scheduler.config.SchedulerSettings
import app.softnetwork.scheduler.handlers.SchedulerHandler
import app.softnetwork.scheduler.launch.SchedulerGuardian
import app.softnetwork.scheduler.message.{
  CronTabAdded,
  CronTabRemoved,
  ScheduleAdded,
  ScheduleRemoved
}
import app.softnetwork.scheduler.persistence.query.Entity2SchedulerProcessorStream
import org.scalatest.Suite
import org.slf4j.{Logger, LoggerFactory}

trait SchedulerTestKit extends SchedulerGuardian with InMemoryPersistenceTestKit {
  _: Suite =>

  /** @return
    *   roles associated with this node
    */
  override def roles: Seq[String] = Seq(SchedulerSettings.SchedulerConfig.akkaNodeRole)

  override def entity2SchedulerProcessorStream: ActorSystem[_] => Entity2SchedulerProcessorStream =
    sys =>
      new Entity2SchedulerProcessorStream
        with SchedulerHandler
        with InMemoryJournalProvider
        with InMemoryOffsetProvider {
        lazy val log: Logger = LoggerFactory getLogger getClass.getName
        override protected val forTests: Boolean = true

        override implicit def system: ActorSystem[_] = sys

        log.info(tag)
      }

  lazy val probeScheduleAdded: TestProbe[ScheduleAdded] = createTestProbe[ScheduleAdded]()

  lazy val probeScheduleRemoved: TestProbe[ScheduleRemoved] = createTestProbe[ScheduleRemoved]()

  lazy val probeCronTabAdded: TestProbe[CronTabAdded] = createTestProbe[CronTabAdded]()

  lazy val probeCronTabRemoved: TestProbe[CronTabRemoved] = createTestProbe[CronTabRemoved]()

  def subscribeSchedulerProbes(): Unit = {
    subscribeProbe(probeScheduleAdded)
    subscribeProbe(probeScheduleRemoved)
    subscribeProbe(probeCronTabAdded)
    subscribeProbe(probeCronTabRemoved)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    subscribeSchedulerProbes()
  }

}
