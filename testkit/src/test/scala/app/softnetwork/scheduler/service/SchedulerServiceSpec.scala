package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.model.{CronTab, Schedule}
import app.softnetwork.scheduler.scalatest.SchedulerRouteTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class SchedulerServiceSpec extends AnyWordSpecLike with SchedulerRouteTestKit {

  val schedule: Schedule = Schedule("s", "0", "add", 1, Some(true), None, None)

  val cronTab: CronTab = CronTab("c", "*", "cron", "* * * * *")

  "scheduler service" must {
    "add schedule" in {
      createSession("admin", admin = Some(true))
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
    }
    "remove cron tab" in {
      removeCronTab(cronTab.persistenceId, cronTab.entityId, cronTab.key)
    }
  }
}
