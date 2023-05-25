package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.scheduler.launch.SchedulerEndpoints
import app.softnetwork.scheduler.service.{
  OneOffCookieSchedulerServiceEndpoints,
  SchedulerServiceEndpoints
}
import app.softnetwork.session.scalatest.{
  OneOffCookieSessionServiceEndpoints,
  SessionServiceEndpoints
}
import org.scalatest.Suite
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait SchedulerEndpointsTestKit extends SchedulerRouteTestKit with SchedulerEndpoints { _: Suite =>

  def sessionServiceEndpoints: ActorSystem[_] => SessionServiceEndpoints = system =>
    OneOffCookieSessionServiceEndpoints(system)

  def schedulerServiceEndpoints: ActorSystem[_] => SchedulerServiceEndpoints = system =>
    OneOffCookieSchedulerServiceEndpoints(system)

  override def endpoints: ActorSystem[_] => List[ServerEndpoint[Any, Future]] = system =>
    sessionServiceEndpoints(system).endpoints ++ schedulerServiceEndpoints(system).endpoints

}
