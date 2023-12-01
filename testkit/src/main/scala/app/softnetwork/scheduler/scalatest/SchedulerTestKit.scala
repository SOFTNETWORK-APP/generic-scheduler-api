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
import app.softnetwork.session.service.SessionMaterials
import org.scalatest.Suite
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

trait SchedulerTestKit extends SchedulerGuardian with InMemoryPersistenceTestKit {
  _: Suite with SessionMaterials[Session] =>

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

  val probeScheduleAdded: TestProbe[ScheduleAdded] = createTestProbe[ScheduleAdded]()
  subscribeProbe(probeScheduleAdded)

  val probeScheduleRemoved: TestProbe[ScheduleRemoved] = createTestProbe[ScheduleRemoved]()
  subscribeProbe(probeScheduleRemoved)

  val probeCronTabAdded: TestProbe[CronTabAdded] = createTestProbe[CronTabAdded]()
  subscribeProbe(probeCronTabAdded)

  val probeCronTabRemoved: TestProbe[CronTabRemoved] = createTestProbe[CronTabRemoved]()
  subscribeProbe(probeCronTabRemoved)

}
