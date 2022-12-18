package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import app.softnetwork.api.server.GrpcServices
import app.softnetwork.schedule.api.SchedulerServiceApiHandler
import app.softnetwork.scheduler.launch.SchedulerGuardian

import scala.concurrent.Future

trait SchedulerGrpcServices extends GrpcServices {
  _: SchedulerGuardian =>

  def interface: String

  def port: Int

  final override def grpcServices
    : ActorSystem[_] => Seq[PartialFunction[HttpRequest, Future[HttpResponse]]] = system =>
    Seq(SchedulerServiceApiHandler.partial(schedulerServer(system))(system))

  def grpcConfig: String = s"""
                              |# Important: enable HTTP/2 in ActorSystem's config
                              |akka.http.server.preview.enable-http2 = on
                              |akka.grpc.client."${SchedulerClient.name}"{
                              |    host = $interface
                              |    port = $port
                              |    use-tls = false
                              |}
                              |""".stripMargin
}