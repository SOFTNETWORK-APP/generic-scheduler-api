package app.softnetwork.scheduler.service

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import app.softnetwork.api.server.ApiRoutes
import app.softnetwork.scheduler.model.{CronTab, Schedule}
import app.softnetwork.scheduler.scalatest.SchedulerRouteTestKit
import app.softnetwork.serialization
import app.softnetwork.serialization.commonFormats
import app.softnetwork.session.handlers.SessionRefreshTokenDao
import app.softnetwork.session.model.SessionDataCompanion
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.RefreshTokenStorage
import org.json4s.Formats
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success, Try}

trait SchedulerServiceSpec
    extends AnyWordSpecLike
    with SchedulerRouteTestKit
    with SessionMaterials[Session] { _: ApiRoutes =>

  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  implicit def companion: SessionDataCompanion[Session] = Session

  override implicit def refreshTokenStorage: RefreshTokenStorage[Session] = SessionRefreshTokenDao(
    t
  )

  implicit lazy val t: ActorSystem[Nothing] = typedSystem()

  override implicit lazy val ec: ExecutionContextExecutor = t.executionContext

  implicit def formats: Formats = commonFormats

  val schedule: Schedule = Schedule("s", "0", "add", 1, Some(true), None, None)

  val cronTab: CronTab = CronTab("c", "*", "cron", "* * * * *")

  val json: String =
    """
      |{
      |  "persistenceId": "Order",
      |  "entityId": "de72dc88-b333-4ff0-a8a1-29aef0ec9810",
      |  "key": "CancelNonValidatedOrderTimerKey",
      |  "delay": 1,
      |  "scheduledDate": "2023-02-03T17:10:09.149Z"
      |}""".stripMargin

  override val refreshableSession: Boolean = false

  "scheduler service" must {
    "add schedule" in {
      createSession("admin", admin = Some(true))
      Try(serialization.serialization.read[Schedule](json)) match {
        case Success(value) =>
          assert(value.triggerable)
          assert(!value.removable)
        case Failure(f) =>
          fail(f.getMessage)
      }
      addSchedule(schedule)
    }
    "add cron tab" in {
      addCronTab(cronTab)
    }
    "load scheduler" in {
      val scheduler = loadScheduler()
      assert(scheduler.schedules.exists(_.uuid == schedule.uuid))
      assert(scheduler.cronTabs.exists(_.uuid == cronTab.uuid))
    }
    "remove schedule" in {
      removeSchedule(schedule.persistenceId, schedule.entityId, schedule.key)
      removeSchedule(schedule.persistenceId, schedule.entityId, schedule.key, StatusCodes.NotFound)
    }
    "remove cron tab" in {
      removeCronTab(cronTab.persistenceId, cronTab.entityId, cronTab.key)
      removeCronTab(cronTab.persistenceId, cronTab.entityId, cronTab.key, StatusCodes.NotFound)
    }
  }

}
