package app.softnetwork.scheduler.api

import app.softnetwork.scheduler.scalatest.SchedulerTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import app.softnetwork.scheduler.model.{CronTab, Schedule}

class SchedulerClientSpec extends AnyWordSpecLike with SchedulerTestKit with SchedulerGrpcServer {

  lazy val client: SchedulerClient = SchedulerClient(typedSystem())

  val schedule: Schedule = Schedule("p", "0", "add", 1, None, None, None)

  val cronTab: CronTab = CronTab("p", "1", "cron", "* * * * *", None, None)

  "Scheduler client" must {
    "add schedule" in {
      assert(client.addSchedule(schedule) complete ())
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
