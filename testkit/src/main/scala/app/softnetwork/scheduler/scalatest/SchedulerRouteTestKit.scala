package app.softnetwork.scheduler.scalatest

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import app.softnetwork.api.server.ApiRoutes
import app.softnetwork.api.server.config.ServerSettings.RootPath
import app.softnetwork.scheduler.api.SchedulerGrpcServices
import app.softnetwork.scheduler.config.SchedulerSettings.SchedulerPath
import app.softnetwork.scheduler.message._
import app.softnetwork.scheduler.model.{CronTab, Schedule, Scheduler}
import app.softnetwork.serialization._
import app.softnetwork.session.scalatest.SessionTestKit
import org.scalatest.Suite

trait SchedulerRouteTestKit
    extends SessionTestKit
    with SchedulerTestKit
    with SchedulerGrpcServices {
  _: Suite with ApiRoutes =>

  lazy val path = s"/$RootPath/$SchedulerPath"

  def addSchedule(schedule: Schedule): Unit = {
    withHeaders(Post(s"$path/schedules", schedule)) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      refreshSession(headers)
    }
  }

  def removeSchedule(
    persistenceId: String,
    entityId: String,
    key: String,
    statusCode: StatusCode = StatusCodes.OK
  ): Unit = {
    withHeaders(
      Delete(
        s"$path/schedules",
        RemoveSchedule(persistenceId, entityId, key)
      )
    ) ~> routes ~> check {
      status shouldEqual statusCode
      refreshSession(headers)
    }
  }

  def addCronTab(cronTab: CronTab): Unit = {
    withHeaders(Post(s"$path/crons", cronTab)) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      refreshSession(headers)
    }
  }

  def removeCronTab(
    persistenceId: String,
    entityId: String,
    key: String,
    statusCode: StatusCode = StatusCodes.OK
  ): Unit = {
    withHeaders(
      Delete(
        s"$path/crons",
        RemoveCronTab(persistenceId, entityId, key)
      )
    ) ~> routes ~> check {
      status shouldEqual statusCode
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
