package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{ApiEndpoint, ApiEndpoints}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.service.SchedulerServiceEndpoints
import app.softnetwork.session.service.SessionEndpoints

trait SchedulerEndpoints extends ApiEndpoints with SchedulerGuardian { _: SchemaProvider =>

  def sessionEndpoints: ActorSystem[_] => SessionEndpoints

  def schedulerEndpoints: ActorSystem[_] => SchedulerServiceEndpoints = system =>
    SchedulerServiceEndpoints.apply(system, sessionEndpoints(system))

  override def endpoints: ActorSystem[_] => List[ApiEndpoint] = system =>
    List(schedulerEndpoints(system))
}
