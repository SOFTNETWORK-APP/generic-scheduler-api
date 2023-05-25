package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiEndpoints
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.service.{
  OneOffCookieSchedulerServiceEndpoints,
  SchedulerServiceEndpoints
}
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait SchedulerEndpoints extends ApiEndpoints with SchedulerGuardian { _: SchemaProvider =>

  def schedulerEndpoints: ActorSystem[_] => SchedulerServiceEndpoints = system =>
    OneOffCookieSchedulerServiceEndpoints(system)

  override def endpoints: ActorSystem[_] => List[ServerEndpoint[Any, Future]] = system =>
    schedulerEndpoints(system).endpoints
}
