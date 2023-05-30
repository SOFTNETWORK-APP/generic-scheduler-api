package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import app.softnetwork.api.server.config.ServerSettings.RootPath
import app.softnetwork.scheduler.api.SchedulerGrpcServices
import app.softnetwork.scheduler.config.SchedulerSettings.SchedulerPath
import app.softnetwork.scheduler.launch.SchedulerRoutes
import app.softnetwork.scheduler.message.{RemoveCronTab, RemoveSchedule}
import app.softnetwork.scheduler.model.{CronTab, Schedule, Scheduler}
import app.softnetwork.serialization._
import app.softnetwork.session.scalatest.{SessionServiceRoute, SessionTestKit}
import org.scalatest.Suite

trait SchedulerRouteTestKit
    extends SessionTestKit
    with SchedulerTestKit
    with SchedulerRoutes
    with SchedulerGrpcServices {
  _: Suite =>

  lazy val path = s"/$RootPath/$SchedulerPath"

  override def apiRoutes(system: ActorSystem[_]): Route =
    SessionServiceRoute(system).route ~ schedulerService(system).route

  def addSchedule(schedule: Schedule): Unit = {
    withHeaders(Post(s"$path/schedules", schedule)) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      refreshSession(headers)
    }
  }

  def removeSchedule(persistenceId: String, entityId: String, key: String): Unit = {
    withHeaders(
      Delete(
        s"$path/schedules",
        RemoveSchedule(persistenceId, entityId, key)
      )
    ) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      refreshSession(headers)
    }
  }

  def addCronTab(cronTab: CronTab): Unit = {
    withHeaders(Post(s"$path/crons", cronTab)) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      refreshSession(headers)
    }
  }

  def removeCronTab(persistenceId: String, entityId: String, key: String): Unit = {
    withHeaders(
      Delete(
        s"$path/crons",
        RemoveCronTab(persistenceId, entityId, key)
      )
    ) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      refreshSession(headers)
    }
  }

  def loadScheduler(): Scheduler = {
    withHeaders(Get(s"$path")) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      refreshSession(headers)
      responseAs[Scheduler]
    }
  }
}
