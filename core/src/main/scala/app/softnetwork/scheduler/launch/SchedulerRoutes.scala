package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import app.softnetwork.api.server.ApiRoutes
import app.softnetwork.scheduler.service.SchedulerService

trait SchedulerRoutes extends ApiRoutes with SchedulerGuardian {

  def schedulerService: ActorSystem[_] => SchedulerService = system => SchedulerService(system)

  override def apiRoutes(system: ActorSystem[_]): Route = schedulerService(system).route

}
