package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiEndpoint
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerEndpoints
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.scalatest.SessionEndpointsRoutes

trait SchedulerEndpointsTestKit extends SchedulerEndpoints with SessionEndpointsRoutes {
  _: SchemaProvider with CsrfCheck =>

  override def endpoints: ActorSystem[_] => List[ApiEndpoint] =
    system =>
      List(
        sessionServiceEndpoints(system),
        schedulerEndpoints(system)
      )

}
