package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiRoute
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerRoutes
import app.softnetwork.session.scalatest.SessionServiceRoutes

trait SchedulerRoutesTestKit extends SchedulerRoutes with SessionServiceRoutes {
  _: SchemaProvider =>

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system =>
      List(
        sessionServiceRoute(system),
        schedulerService(system)
      )

}
