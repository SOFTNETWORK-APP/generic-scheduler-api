package app.softnetwork.scheduler.handlers

import app.softnetwork.persistence.message.{Command, CommandResult}
import app.softnetwork.persistence.typed.scaladsl.EntityPattern
import app.softnetwork.scheduler.persistence.typed.SampleTypedKey
import org.slf4j.{Logger, LoggerFactory}

trait SampleHandler extends EntityPattern[Command, CommandResult] with SampleTypedKey {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName
}

object SampleHandler extends SampleHandler
