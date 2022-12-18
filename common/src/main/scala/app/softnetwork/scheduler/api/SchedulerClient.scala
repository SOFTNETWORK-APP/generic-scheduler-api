package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import akka.grpc.GrpcClientSettings
import app.softnetwork.schedule.api.{
  AddScheduleRequest,
  RemoveScheduleRequest,
  SchedulerServiceApiClient
}
import org.softnetwork.akka.model.Schedule

import scala.concurrent.Future

trait SchedulerClient extends GrpcClient {

  implicit lazy val grpcClient: SchedulerServiceApiClient =
    SchedulerServiceApiClient(
      GrpcClientSettings.fromConfig(name)
    )

  def addSchedule(schedule: Schedule): Future[Boolean] = {
    grpcClient.addSchedule(AddScheduleRequest(Some(schedule))) map (_.succeeded)
  }

  def removeSchedule(persistenceId: String, entityId: String, key: String): Future[Boolean] = {
    grpcClient.removeSchedule(RemoveScheduleRequest(persistenceId, entityId, key)) map (_.succeeded)
  }

}

object SchedulerClient extends GrpcClientFactory[SchedulerClient] {
  override val name: String = "SchedulerService"
  override def init(sys: ActorSystem[_]): SchedulerClient = {
    new SchedulerClient {
      override implicit lazy val system: ActorSystem[_] = sys
      val name: String = SchedulerClient.name
    }
  }
}
