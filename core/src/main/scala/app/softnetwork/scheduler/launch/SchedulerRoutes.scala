package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import app.softnetwork.api.server.ApiRoutes
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.service.SchedulerService
import app.softnetwork.session.service.SessionService

trait SchedulerRoutes extends ApiRoutes with SchedulerGuardian { _: SchemaProvider =>

  def sessionService: ActorSystem[_] => SessionService

  def schedulerService: ActorSystem[_] => SchedulerService = system =>
    SchedulerService(system, sessionService(system))

  override def apiRoutes(system: ActorSystem[_]): Route = schedulerService(system).route

}
