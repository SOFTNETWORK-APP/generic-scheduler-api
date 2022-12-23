package app.softnetwork.scheduler.message

import app.softnetwork.persistence.message.{Command, CommandResult, Event}
import app.softnetwork.scheduler.model.Sample

object SampleMessages {

  trait SampleCommand extends Command
  case object AddSample extends SampleCommand
  trait SampleResult extends CommandResult
  case object SampleAdded extends SampleResult
  trait SampleEvent extends Event
  case class SampleAddedEvent(sample: Sample) extends SampleEvent

}
