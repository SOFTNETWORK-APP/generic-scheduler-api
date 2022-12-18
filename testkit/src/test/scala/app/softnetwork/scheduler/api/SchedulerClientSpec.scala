package app.softnetwork.scheduler.api

import app.softnetwork.scheduler.scalatest.SchedulerTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import org.softnetwork.akka.model.Schedule

class SchedulerClientSpec extends AnyWordSpecLike with SchedulerTestKit {

  lazy val client: SchedulerClient = SchedulerClient(typedSystem())

  val schedule: Schedule = Schedule("p", "0", "add", 1, None, None, None)

  "Scheduler client" must {
    "add schedule" in {
      assert(client.addSchedule(schedule) complete ())
    }

    "remove schedule" in {
      assert(
        client.removeSchedule(schedule.persistenceId, schedule.entityId, schedule.key) complete ()
      )
    }
  }
}
