package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{GrpcService, GrpcServices}
import app.softnetwork.scheduler.launch.SchedulerGuardian

trait SchedulerGrpcServices extends GrpcServices { _: SchedulerGuardian =>

  override def grpcServices: ActorSystem[_] => Seq[GrpcService] = system =>
    schedulerGrpcServices(system)

}
