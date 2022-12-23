package app.softnetwork.scheduler.persistence.typed

import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import app.softnetwork.persistence.message.Command
import app.softnetwork.persistence.typed.CommandTypeKey

import scala.reflect.ClassTag

trait SampleTypedKey extends CommandTypeKey[Command] {
  override def TypeKey(implicit tTag: ClassTag[Command]): EntityTypeKey[Command] =
    SampleBehavior.TypeKey
}
