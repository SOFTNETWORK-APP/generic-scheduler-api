package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import app.softnetwork.scheduler.service.SchedulerService
import app.softnetwork.session.scalatest.SessionServiceRoutes

trait SchedulerRoutesTestKit extends SessionServiceRoutes {

  def schedulerService: ActorSystem[_] => SchedulerService = system =>
    SchedulerService(system, sessionService(system))

  override def apiRoutes(system: ActorSystem[_]): Route =
    sessionServiceRoute(system).route ~ schedulerService(system).route

}
