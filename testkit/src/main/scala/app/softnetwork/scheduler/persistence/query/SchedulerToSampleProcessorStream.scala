package app.softnetwork.scheduler.persistence.query

import akka.actor.typed.eventstream.EventStream.Publish
import app.softnetwork.persistence.message.{Command, CommandResult}
import app.softnetwork.persistence.query.{JournalProvider, OffsetProvider}
import app.softnetwork.scheduler.handlers.SampleHandler
import app.softnetwork.scheduler.message.SampleMessages.{SampleTriggered, TriggerSample}
import app.softnetwork.scheduler.model.Schedule

import scala.concurrent.Future

trait SchedulerToSampleProcessorStream
    extends Scheduler2EntityProcessorStream[Command, CommandResult]
    with SampleHandler {
  _: JournalProvider with OffsetProvider =>

  override def forTests: Boolean = true

  /** @param schedule
    *   - the schedule to trigger
    * @return
    *   true if the schedule has been successfully triggered, false otherwise
    */
  override protected def triggerSchedule(schedule: Schedule): Future[Boolean] = {
    ?(schedule.entityId, TriggerSample(schedule.key)) map {
      case SampleTriggered =>
        system.eventStream.tell(Publish(SampleScheduleTriggered(schedule)))
        true
      case _ => false
    }
  }
}
