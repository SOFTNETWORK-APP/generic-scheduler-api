package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{ApiRoute, ApiRoutes}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.service.SchedulerService

trait SchedulerRoutes extends ApiRoutes { _: SchedulerGuardian with SchemaProvider =>

  def schedulerService: ActorSystem[_] => SchedulerService = system =>
    SchedulerService(system, sessionService(system))

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system =>
      List(
        schedulerService(system)
      )

}
