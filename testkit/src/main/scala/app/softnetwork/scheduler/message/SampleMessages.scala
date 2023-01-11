package app.softnetwork.scheduler.message

import app.softnetwork.persistence.message.{Command, CommandResult, ErrorMessage, Event}
import app.softnetwork.scheduler.message.SchedulerEvents.ExternalSchedulerEvent
import app.softnetwork.scheduler.model.Sample

object SampleMessages {

  trait SampleCommand extends Command
  case object AddSample extends SampleCommand
  case class TriggerSample(key: String) extends SampleCommand
  case object LoadSample extends SampleCommand
  trait SampleResult extends CommandResult
  case object SampleAdded extends SampleResult
  case object SampleTriggered extends SampleResult
  case class SampleLoaded(sample: Sample) extends SampleResult
  abstract class SampleError(message: String) extends ErrorMessage(message) with SampleResult
  case object SampleNotFound extends SampleError("SampleNotFound")
  trait SampleEvent extends ExternalSchedulerEvent
  case class SampleAddedEvent(sample: Sample) extends SampleEvent
  case class SampleTriggeredEvent(triggered: Int) extends SampleEvent

}
