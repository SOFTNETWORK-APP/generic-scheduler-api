package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import app.softnetwork.api.server.launch.HealthCheckApplication
import app.softnetwork.persistence.query.SchemaProvider
import app.softnetwork.schedule.api.SchedulerServiceApiHandler

import scala.concurrent.Future

trait SchedulerApplication extends HealthCheckApplication with SchedulerGuardian {
  _: SchemaProvider =>

  override def grpcServices
    : ActorSystem[_] => Seq[PartialFunction[HttpRequest, Future[HttpResponse]]] = system =>
    Seq(SchedulerServiceApiHandler.partial(schedulerServer(system))(system))

}
