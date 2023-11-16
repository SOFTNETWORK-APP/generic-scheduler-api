package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.Endpoint
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerEndpoints
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.scalatest.{SessionEndpointsRoutes, SessionTestKit}
import app.softnetwork.session.service.SessionMaterials

trait SchedulerEndpointsTestKit extends SchedulerEndpoints with SessionEndpointsRoutes {
  _: SessionTestKit
    with SchedulerTestKit
    with SchemaProvider
    with SessionMaterials
    with CsrfCheck =>

  override def endpoints: ActorSystem[_] => List[Endpoint] =
    system =>
      List(
        sessionServiceEndpoints(system),
        schedulerEndpoints(system)
//        schedulerSwagger(system)
      )

}
