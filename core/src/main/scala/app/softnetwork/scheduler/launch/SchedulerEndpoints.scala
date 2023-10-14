package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{ApiEndpoints, Endpoint}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.service.SchedulerServiceEndpoints

trait SchedulerEndpoints extends ApiEndpoints { _: SchedulerGuardian with SchemaProvider =>

  def schedulerEndpoints: ActorSystem[_] => SchedulerServiceEndpoints = system =>
    SchedulerServiceEndpoints.apply(system, sessionEndpoints(system))

  override def endpoints: ActorSystem[_] => List[Endpoint] = system =>
    List(schedulerEndpoints(system))
}
