package app.softnetwork.scheduler.api

import app.softnetwork.api.server.scalatest.ServerTestKit
import app.softnetwork.scheduler.launch.SchedulerGuardian

trait SchedulerGrpcServicesTestKit extends SchedulerGrpcServices {
  _: SchedulerGuardian with ServerTestKit =>

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
