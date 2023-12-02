package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{ApiRoute, ApiRoutes}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.service.SchedulerService
import app.softnetwork.session.model.{SessionData, SessionDataDecorator}

trait SchedulerRoutes[SD <: SessionData with SessionDataDecorator[SD]] extends ApiRoutes {
  self: SchedulerGuardian with SchemaProvider =>

  def schedulerService: ActorSystem[_] => SchedulerService[SD]

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system =>
      List(
        schedulerService(system)
      )

}
