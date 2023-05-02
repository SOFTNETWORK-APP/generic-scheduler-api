package app.softnetwork.scheduler.persistence.query

import akka.Done
import akka.actor.typed.eventstream.EventStream.Publish
import akka.persistence.typed.PersistenceId
import app.softnetwork.persistence.query.{EventProcessorStream, JournalProvider, OffsetProvider}
import app.softnetwork.persistence.typed.scaladsl.EntityPattern
import app.softnetwork.scheduler.config.SchedulerSettings
import app.softnetwork.scheduler.message.SchedulerEvents.SchedulerEventWithCommand
import app.softnetwork.scheduler.message._

import scala.concurrent.Future

trait Entity2SchedulerProcessorStream extends EventProcessorStream[SchedulerEventWithCommand] {
  _: JournalProvider
    with OffsetProvider
    with EntityPattern[SchedulerCommand, SchedulerCommandResult] =>

  override lazy val tag: String =
    SchedulerSettings.SchedulerConfig.eventStreams.entityToSchedulerTag

  protected val forTests = false

  /** Processing event
    *
    * @param event
    *   - event to process
    * @param persistenceId
    *   - persistence id
    * @param sequenceNr
    *   - sequence number
    * @return
    */
  override protected def processEvent(
    event: SchedulerEventWithCommand,
    persistenceId: PersistenceId,
    sequenceNr: Long
  ): Future[Done] = {
    (this !? event.command).map {
      case r: ScheduleNotFound.type =>
        log.warn(s"${event.command} -> ${r.message}")
        Done
      case r: CronTabNotFound.type =>
        log.warn(s"${event.command} -> ${r.message}")
        Done
      case r: SchedulerErrorMessage =>
        log.error(s"${event.command} -> ${r.message}")
        throw new Throwable(s"${event.command} -> ${r.message}")
      case result =>
        if (forTests) {
          system.eventStream.tell(Publish(result))
        }
        Done
    }
  }
}
