package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{ApiEndpoints, Endpoint}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.service.SchedulerServiceEndpoints
import app.softnetwork.session.model.{SessionData, SessionDataDecorator}

trait SchedulerEndpoints[SD <: SessionData with SessionDataDecorator[SD]] extends ApiEndpoints {
  self: SchedulerGuardian with SchemaProvider =>

  def schedulerEndpoints: ActorSystem[_] => SchedulerServiceEndpoints[SD]

  override def endpoints: ActorSystem[_] => List[Endpoint] = system =>
    List(schedulerEndpoints(system))
}
