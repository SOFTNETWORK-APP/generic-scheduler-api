package app.softnetwork.scheduler.persistence.query

import akka.Done
import akka.persistence.typed.PersistenceId
import app.softnetwork.persistence.message.{Command, CommandResult}
import app.softnetwork.persistence.query.{EventProcessorStream, JournalProvider, OffsetProvider}
import app.softnetwork.persistence.typed.scaladsl.EntityPattern
import app.softnetwork.scheduler.message.SchedulerEvents.{
  CronTabTriggeredEvent,
  CronTabsResetedEvent,
  ScheduleTriggeredEvent,
  SchedulerEvent
}
import app.softnetwork.scheduler.model.{CronTab, Schedule}

import scala.concurrent.Future

/** Created by smanciot on 04/09/2020.
  */
trait Scheduler2EntityProcessorStream[C <: Command, R <: CommandResult]
    extends EventProcessorStream[SchedulerEvent] {
  _: JournalProvider with OffsetProvider with EntityPattern[C, R] =>

  def forTests: Boolean = false

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
  override protected final def processEvent(
    event: SchedulerEvent,
    persistenceId: PersistenceId,
    sequenceNr: Long
  ): Future[Done] = {
    event match {
      case evt: ScheduleTriggeredEvent =>
        import evt._
        if (schedule.entityId == ALL_KEY) {
          currentPersistenceIds().runForeach(persistenceId => {
            if (persistenceId.startsWith(schedule.persistenceId)) {
              val entityId = persistenceId.split("\\|").last
              if (entityId != ALL_KEY) {
                triggerSchedule(schedule.withEntityId(entityId))
              } else {
                Future.successful(true)
              }
            } else {
              Future.successful(true)
            }
          })
        } else {
          triggerSchedule(schedule).map {
            case true => Done
            case _ =>
              throw new Exception(
                s"event ${persistenceId.id} for sequence $sequenceNr could not be processed by $platformEventProcessorId"
              )
          }
        }
      case evt: CronTabTriggeredEvent =>
        import evt._
        if (cronTab.entityId == ALL_KEY) {
          val maybeSchedule: Option[Schedule] = cronTab
          maybeSchedule match {
            case Some(schedule) =>
              currentPersistenceIds().runForeach(persistenceId => {
                if (persistenceId.startsWith(cronTab.persistenceId)) {
                  val entityId = persistenceId.split("\\|").last
                  if (entityId != ALL_KEY) {
                    triggerSchedule(schedule.withEntityId(entityId))
                  } else {
                    Future.successful(true)
                  }
                } else {
                  Future.successful(true)
                }
              })
            case _ => Future.successful(Done)
          }
        } else {
          triggerCronTab(cronTab).map {
            case true => Done
            case _ =>
              throw new Exception(
                s"event ${persistenceId.id} for sequence $sequenceNr could not be processed by $platformEventProcessorId"
              )
          }
        }
      case evt: CronTabsResetedEvent =>
        resetCronTabs(evt.entityId, evt.keys).map {
          case true => Done
          case _ =>
            throw new Exception(
              s"event ${persistenceId.id} for sequence $sequenceNr could not be processed by $platformEventProcessorId"
            )
        }
      case _ => Future.successful(Done)
    }
  }

  /** @param schedule
    *   - the schedule to trigger
    * @return
    *   true if the schedule has been successfully triggered, false otherwise
    */
  protected def triggerSchedule(schedule: Schedule): Future[Boolean] = Future.successful(false)

  /** @param cronTab
    *   - the cron tab to trigger
    * @return
    *   true if the cron tab has been successfully triggered, false otherwise
    */
  protected def triggerCronTab(cronTab: CronTab): Future[Boolean] = Future.successful(false)

  /** @param entityId
    *   - the persistence entity id
    * @return
    *   true if the cron tabs have been successfully reseted for this entity, false otherwise
    */
  protected def resetCronTabs(entityId: String, keys: Seq[String] = Seq.empty): Future[Boolean] =
    Future.successful(false)
}
