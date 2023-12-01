package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.Endpoint
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerEndpoints
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.scalatest.{SessionEndpointsRoutes, SessionTestKit}
import app.softnetwork.session.service.SessionMaterials
import org.softnetwork.session.model.Session

trait SchedulerEndpointsTestKit extends SchedulerEndpoints with SessionEndpointsRoutes[Session] {
  _: SessionTestKit[Session]
    with SchedulerTestKit
    with SchemaProvider
    with SessionMaterials[Session]
    with CsrfCheck =>

  override def endpoints: ActorSystem[_] => List[Endpoint] =
    system =>
      List(
        sessionServiceEndpoints(system),
        schedulerEndpoints(system)
//        schedulerSwagger(system)
      )

}
