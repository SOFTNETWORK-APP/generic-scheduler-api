package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiEndpoints
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.service.SchedulerServiceEndpoints
import app.softnetwork.session.service.SessionEndpoints
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait SchedulerEndpoints extends ApiEndpoints with SchedulerGuardian { _: SchemaProvider =>

  def sessionEndpoints: ActorSystem[_] => SessionEndpoints

  def schedulerEndpoints: ActorSystem[_] => SchedulerServiceEndpoints = system =>
    SchedulerServiceEndpoints.apply(system, sessionEndpoints(system))

  override def endpoints: ActorSystem[_] => List[ServerEndpoint[Any, Future]] = system =>
    schedulerEndpoints(system).endpoints
}
