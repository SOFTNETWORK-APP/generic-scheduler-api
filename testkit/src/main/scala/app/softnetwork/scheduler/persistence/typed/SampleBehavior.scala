package app.softnetwork.scheduler.persistence.typed

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, TimerScheduler}
import akka.persistence.typed.scaladsl.Effect
import app.softnetwork.persistence.message.{Command, CommandResult, Event}
import app.softnetwork.persistence.typed._
import app.softnetwork.scheduler.config.SchedulerSettings
import app.softnetwork.scheduler.message.AddSchedule
import app.softnetwork.scheduler.message.SampleMessages.{
  AddSample,
  LoadSample,
  SampleAdded,
  SampleAddedEvent,
  SampleLoaded,
  SampleNotFound,
  SampleTriggered,
  SampleTriggeredEvent,
  TriggerSample
}
import app.softnetwork.scheduler.message.SchedulerEvents.ExternalEntityToSchedulerEvent
import app.softnetwork.scheduler.model.{Sample, Schedule}

trait SampleBehavior extends EntityBehavior[Command, Sample, Event, CommandResult] {
  override def persistenceId: String = "Sample"

  override protected def tagEvent(entityId: String, event: Event): Set[String] = {
    event match {
      case _: ExternalEntityToSchedulerEvent =>
        Set(SchedulerSettings.SchedulerConfig.eventStreams.entityToSchedulerTag)
      case _ => super.tagEvent(entityId, event)
    }
  }

  override def handleCommand(
    entityId: String,
    state: Option[Sample],
    command: Command,
    replyTo: Option[ActorRef[CommandResult]],
    timers: TimerScheduler[Command]
  )(implicit context: ActorContext[Command]): Effect[Event, Option[Sample]] = {
    command match {
      case AddSample =>
        Effect.persist(SampleAddedEvent(Sample(entityId))).thenRun(_ => SampleAdded ~> replyTo)
      case cmd: TriggerSample =>
        Effect
          .persist(
            List(
              SampleTriggeredEvent(state.map(_.triggered + 1).getOrElse(1)),
              ExternalEntityToSchedulerEvent(
                ExternalEntityToSchedulerEvent.Wrapped.AddSchedule(
                  AddSchedule(
                    Schedule(
                      persistenceId,
                      entityId,
                      cmd.key,
                      1
                    )
                  )
                )
              )
            )
          )
          .thenRun(_ => SampleTriggered ~> replyTo)
      case LoadSample =>
        state match {
          case Some(sample) => Effect.none.thenRun(_ => SampleLoaded(sample) ~> replyTo)
          case _            => Effect.none.thenRun(_ => SampleNotFound ~> replyTo)
        }
      case _ => super.handleCommand(entityId, state, command, replyTo, timers)
    }
  }

  override def handleEvent(state: Option[Sample], event: Event)(implicit
    context: ActorContext[_]
  ): Option[Sample] = {
    event match {
      case evt: SampleAddedEvent =>
        Some(evt.sample)
      case evt: SampleTriggeredEvent =>
        state.map(_.copy(triggered = evt.triggered))
      case _ => state
    }
  }
}

object SampleBehavior extends SampleBehavior
