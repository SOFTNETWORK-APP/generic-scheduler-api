package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiRoute
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerRoutes

trait SchedulerRoutesApi extends SchedulerApi with SchedulerRoutes { _: SchemaProvider =>
  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system => super.apiRoutes(system) :+ schedulerSwagger(system)
}
