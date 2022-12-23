package app.softnetwork.scheduler.persistence.query

import akka.actor.typed.eventstream.EventStream.Publish
import app.softnetwork.persistence.message.{Command, CommandResult}
import app.softnetwork.persistence.query.JournalProvider
import app.softnetwork.scheduler.handlers.SampleHandler
import org.softnetwork.akka.model.Schedule

import scala.concurrent.Future

trait SchedulerToSampleProcessorStream
    extends Scheduler2EntityProcessorStream[Command, CommandResult]
    with SampleHandler {
  _: JournalProvider =>

  override def forTests: Boolean = true

  /** @param schedule
    *   - the schedule to trigger
    * @return
    *   true if the schedule has been successfully triggered, false otherwise
    */
  override protected def triggerSchedule(schedule: Schedule): Future[Boolean] = {
    system.eventStream.tell(Publish(SampleScheduleTriggered(schedule)))
    Future.successful(true)
  }
}
