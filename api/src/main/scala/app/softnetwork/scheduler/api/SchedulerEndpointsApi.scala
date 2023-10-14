package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.Endpoint
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerEndpoints
import app.softnetwork.session.CsrfCheck

trait SchedulerEndpointsApi extends SchedulerApi with SchedulerEndpoints {
  _: SchemaProvider with CsrfCheck =>

  override def endpoints: ActorSystem[_] => List[Endpoint] =
    system => super.endpoints(system) :+ schedulerSwagger(system)
}
