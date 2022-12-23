package app.softnetwork.scheduler.persistence.typed

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, TimerScheduler}
import akka.persistence.typed.scaladsl.Effect
import app.softnetwork.persistence.message.{Command, CommandResult, Event}
import app.softnetwork.persistence.typed._
import app.softnetwork.scheduler.message.SampleMessages.{AddSample, SampleAdded, SampleAddedEvent}
import app.softnetwork.scheduler.model.Sample

trait SampleBehavior extends EntityBehavior[Command, Sample, Event, CommandResult] {
  override def persistenceId: String = "Sample"

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
      case _ => super.handleCommand(entityId, state, command, replyTo, timers)
    }
  }

  override def handleEvent(state: Option[Sample], event: Event)(implicit
    context: ActorContext[_]
  ): Option[Sample] = {
    event match {
      case evt: SampleAddedEvent =>
        Some(evt.sample)
      case _ => super.handleEvent(state, event)
    }
  }
}

object SampleBehavior extends SampleBehavior
