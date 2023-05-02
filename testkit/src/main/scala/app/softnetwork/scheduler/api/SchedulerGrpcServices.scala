package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import app.softnetwork.api.server.GrpcServices
import app.softnetwork.api.server.scalatest.ServerTestKit
import app.softnetwork.scheduler.launch.SchedulerGuardian

import scala.concurrent.Future

trait SchedulerGrpcServices extends GrpcServices {
  _: SchedulerGuardian with ServerTestKit =>

  override def grpcServices
    : ActorSystem[_] => Seq[PartialFunction[HttpRequest, Future[HttpResponse]]] =
    schedulerGrpcServices

  def schedulerGrpcServices
    : ActorSystem[_] => Seq[PartialFunction[HttpRequest, Future[HttpResponse]]] = system =>
    Seq(SchedulerServiceApiHandler.partial(schedulerServer(system))(system))

  def schedulerGrpcConfig: String = s"""
                              |# Important: enable HTTP/2 in ActorSystem's config
                              |akka.http.server.preview.enable-http2 = on
                              |akka.grpc.client."${SchedulerClient.name}"{
                              |    host = $interface
                              |    port = $port
                              |    use-tls = false
                              |}
                              |""".stripMargin
}
