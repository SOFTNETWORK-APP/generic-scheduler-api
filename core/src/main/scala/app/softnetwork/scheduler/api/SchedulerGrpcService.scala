package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import app.softnetwork.api.server.GrpcService

import scala.concurrent.Future

class SchedulerGrpcService(server: SchedulerServer) extends GrpcService {
  override def grpcService: ActorSystem[_] => PartialFunction[HttpRequest, Future[HttpResponse]] =
    system => SchedulerServiceApiHandler.partial(server)(system)
}
