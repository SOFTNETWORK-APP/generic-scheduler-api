package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiEndpoint
import app.softnetwork.scheduler.service.SchedulerServiceEndpoints
import app.softnetwork.session.scalatest.SessionEndpointsRoutes
import com.softwaremill.session.CsrfCheck

trait SchedulerEndpointsTestKit extends SessionEndpointsRoutes { _: CsrfCheck =>

  def schedulerEndpoints: ActorSystem[_] => SchedulerServiceEndpoints = system =>
    SchedulerServiceEndpoints.apply(system, sessionEndpoints(system))

  override def endpoints: ActorSystem[_] => List[ApiEndpoint] =
    system =>
      List(
        sessionServiceEndpoints(system),
        schedulerEndpoints(system)
      )

}
