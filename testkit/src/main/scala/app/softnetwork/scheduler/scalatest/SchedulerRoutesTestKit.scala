package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiRoute
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerRoutes
import app.softnetwork.session.scalatest.{SessionServiceRoutes, SessionTestKit}
import app.softnetwork.session.service.SessionMaterials
import org.softnetwork.session.model.Session

trait SchedulerRoutesTestKit extends SchedulerRoutes with SessionServiceRoutes[Session] {
  _: SessionTestKit[Session]
    with SchedulerTestKit
    with SchemaProvider
    with SessionMaterials[Session] =>

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system =>
      List(
        sessionServiceRoute(system),
        schedulerService(system)
//        schedulerSwagger(system)
      )

}
