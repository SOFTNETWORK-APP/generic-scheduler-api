package app.softnetwork.scheduler.scalatest

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.launch
import app.softnetwork.persistence.launch.PersistenceGuardian._
import app.softnetwork.persistence.query.{InMemoryJournalProvider, InMemoryOffsetProvider}
import app.softnetwork.scheduler.config.SchedulerSettings
import app.softnetwork.scheduler.persistence.query.{
  SampleScheduleTriggered,
  Scheduler2EntityProcessorStream,
  SchedulerToSampleProcessorStream
}
import app.softnetwork.scheduler.persistence.typed.{SampleBehavior, SchedulerBehavior}
import app.softnetwork.session.service.SessionMaterials
import org.scalatest.Suite

trait SchedulerWithSampleTestKit extends SchedulerTestKit { _: Suite with SessionMaterials =>
  override def schedulerEntities: ActorSystem[_] => Seq[launch.PersistentEntity[_, _, _, _]] =
    _ => Seq(SchedulerBehavior, SampleBehavior)

  override def scheduler2EntityProcessorStreams
    : ActorSystem[_] => Seq[Scheduler2EntityProcessorStream[_, _]] =
    sys =>
      Seq(
        new SchedulerToSampleProcessorStream
          with InMemoryJournalProvider
          with InMemoryOffsetProvider {
          override implicit def system: ActorSystem[_] = sys
          override val tag: String = SchedulerSettings.tag(SampleBehavior.persistenceId)
        }
      )

  val probeSampleSchedule: TestProbe[SampleScheduleTriggered] =
    createTestProbe[SampleScheduleTriggered]()
  subscribeProbe(probeSampleSchedule)

}
