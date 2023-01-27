package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import akka.grpc.GrpcClientSettings
import app.softnetwork.api.server.client.{GrpcClient, GrpcClientFactory}
import app.softnetwork.scheduler.model.{CronTab, Schedule, Scheduler}

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

  def addCronTab(cronTab: CronTab): Future[Boolean] = {
    grpcClient.addCronTab(AddCronTabRequest(Some(cronTab))) map (_.succeeded)
  }

  def removeCronTab(persistenceId: String, entityId: String, key: String): Future[Boolean] = {
    grpcClient.removeCronTab(RemoveCronTabRequest(persistenceId, entityId, key)) map (_.succeeded)
  }

  def loadScheduler(schedulerId: Option[String]): Future[Option[Scheduler]] = {
    grpcClient.loadScheduler(LoadSchedulerRequest(schedulerId)) map (_.scheduler)
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
