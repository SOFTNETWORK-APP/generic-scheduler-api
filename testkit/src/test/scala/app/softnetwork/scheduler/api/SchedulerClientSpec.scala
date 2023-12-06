package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.scheduler.scalatest.SchedulerTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import app.softnetwork.scheduler.model.{CronTab, Schedule, Scheduler}
import app.softnetwork.session.config.Settings
import app.softnetwork.session.handlers.SessionRefreshTokenDao
import app.softnetwork.session.service.BasicSessionMaterials
import com.softwaremill.session.RefreshTokenStorage
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.util.{Failure, Success}

class SchedulerClientSpec
    extends AnyWordSpecLike
    with SchedulerTestKit
    with SchedulerGrpcServerTestKit
    with BasicSessionMaterials[Session] {

  override implicit def refreshTokenStorage: RefreshTokenStorage[Session] = SessionRefreshTokenDao(
    ts
  )

  override implicit def ts: ActorSystem[_] = typedSystem()

  override protected def sessionType: Session.SessionType =
    Settings.Session.SessionContinuityAndTransport

  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  lazy val client: SchedulerClient = SchedulerClient(typedSystem())

  val schedule: Schedule = Schedule("p", "0", "add", 1, None, None, None)

  val cronTab: CronTab = CronTab("p", "1", "cron", "* * * * *", None, None)

  "Scheduler client" must {
    "add schedule" in {
      assert(client.addSchedule(schedule) complete ())
    }

    "load scheduler" in {
      val scheduler: Option[Scheduler] =
        client.loadScheduler(Some("my-scheduler")) complete () match {
          case Success(value) => value
          case Failure(_)     => None
        }
      assert(scheduler.isDefined)
      val schedules = scheduler.get.schedules
      assert(schedules.nonEmpty)
      assert(schedules.exists(_.uuid == schedule.uuid))
    }

    "remove schedule" in {
      assert(
        client.removeSchedule(schedule.persistenceId, schedule.entityId, schedule.key) complete ()
      )
    }

    "add cron tab" in {
      assert(client.addCronTab(cronTab) complete ())
    }

    "remove cron tab" in {
      assert(
        client.removeCronTab(cronTab.persistenceId, cronTab.entityId, cronTab.key) complete ()
      )
    }
  }
}
