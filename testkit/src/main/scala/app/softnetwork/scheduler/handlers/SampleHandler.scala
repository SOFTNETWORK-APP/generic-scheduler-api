package app.softnetwork.scheduler.handlers

import app.softnetwork.persistence.message.{Command, CommandResult}
import app.softnetwork.persistence.typed.scaladsl.EntityPattern
import app.softnetwork.scheduler.persistence.typed.SampleTypedKey

trait SampleHandler extends EntityPattern[Command, CommandResult] with SampleTypedKey {}

object SampleHandler extends SampleHandler
