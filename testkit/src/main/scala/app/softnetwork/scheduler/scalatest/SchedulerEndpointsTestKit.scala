package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.scheduler.service.SchedulerServiceEndpoints
import app.softnetwork.session.scalatest.SessionServiceEndpointsRoutes
import com.softwaremill.session.CsrfCheck
import org.scalatest.Suite
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait SchedulerEndpointsTestKit extends SchedulerRouteTestKit with SessionServiceEndpointsRoutes {
  _: Suite with CsrfCheck =>

  def schedulerEndpoints: ActorSystem[_] => SchedulerServiceEndpoints = system =>
    SchedulerServiceEndpoints.apply(system, sessionEndpoints(system))

  override def endpoints: ActorSystem[_] => List[ServerEndpoint[Any, Future]] = system =>
    sessionServiceEndpoints(system).endpoints ++ schedulerEndpoints(system).endpoints

}
