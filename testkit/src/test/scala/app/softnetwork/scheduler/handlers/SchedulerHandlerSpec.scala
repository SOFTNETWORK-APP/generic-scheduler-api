package app.softnetwork.scheduler.handlers

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import app.softnetwork.persistence._
import app.softnetwork.scheduler.message.SampleMessages.{AddSample, SampleAdded}
import app.softnetwork.scheduler.scalatest.SchedulerWithSampleTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import app.softnetwork.scheduler.message._
import app.softnetwork.scheduler.persistence.query.SampleScheduleTriggered
import app.softnetwork.scheduler.persistence.typed.SampleBehavior
import org.softnetwork.akka.model.{CronTab, Schedule}

import scala.concurrent.ExecutionContextExecutor

/** Created by smanciot on 19/03/2020.
  */
class SchedulerHandlerSpec
    extends SchedulerHandler
    with AnyWordSpecLike
    with SchedulerWithSampleTestKit {

  implicit lazy val system: ActorSystem[Nothing] = typedSystem()

  implicit lazy val ec: ExecutionContextExecutor = system.executionContext

  "Scheduler" must {
    "add Cron Tab" in {
      // add Sample[sample] entity
      SampleHandler ? ("sample", AddSample) assert {
        case SampleAdded => succeed
        case _           => fail()
      }
      // add cron tab for all Sample entity
      val cronTab = CronTab(SampleBehavior.persistenceId, ALL_KEY, "cron", "* * * * *")
      this !? AddCronTab(cronTab) assert {
        case _: CronTabAdded => succeed
        case other           => fail(other.getClass)
      }
      // trigger cron tab
      this !? TriggerCronTab(cronTab.persistenceId, cronTab.entityId, cronTab.key) assert {
        case _: CronTabTriggered => succeed
        case other               => fail(other.getClass)
      }
      // a schedule for the Sample[sample] entity has been added to be triggered at the next cron job date
      probeSampleSchedule.receiveMessage()
      this !? LoadScheduler assert {
        case r: SchedulerLoaded =>
          val scheduler = r.scheduler
          logger.info(scheduler.toProtoString)
          assert(scheduler.cronTabs.exists(ct => ct.uuid == cronTab.uuid))
          scheduler.schedules.find(s =>
            s.persistenceId == SampleBehavior.persistenceId && s.entityId == "sample" && s.key == cronTab.key
          ) match {
            case Some(schedule) =>
              assert(schedule.repeatedly.getOrElse(false))
              assert(schedule.getScheduledDate.equals(schedule.getLastTriggered))
            case _ => fail("schedule not found")
          }
        case _ => fail()
      }
    }
    "remove schedule that is not repeatable and has already been triggered" in {
      val schedule = Schedule("p", "0", "add", 1, None, None, None)
      // add schedule which has to be triggered
      this !? AddSchedule(schedule) assert {
        case r: ScheduleAdded => assert(r.schedule.triggerable)
        case other            => fail(other.getClass)
      }
      // trigger schedule
      this !? TriggerSchedule(schedule.persistenceId, schedule.entityId, schedule.key) assert {
        case r: ScheduleTriggered => assert(r.schedule.removable)
        case other                => fail(other.getClass)
      }
      // remove schedule that has already been triggered
      this !? AddSchedule(schedule) assert {
        case _: ScheduleRemoved => succeed
        case other              => fail(other.getClass)
      }
    }
    "add schedule that is repeatable" in {
      val schedule = Schedule("p", "1", "add", 1, Some(true), None, Some(now()))
      this !? AddSchedule(schedule) assert {
        case _: ScheduleAdded => succeed
        case other            => fail(other.getClass)
      }
    }
    "add schedule that is not repeatable and has never been triggered" in {
      val schedule = Schedule("p", "2", "add", 1)
      assert(schedule.triggerable)
      this !? AddSchedule(schedule) assert {
        case _: ScheduleAdded => succeed
        case other            => fail(other.getClass)
      }
    }
    "trigger repeatedly a schedule" in {
      val schedule = Schedule("p", "3", "add", 1, Some(true), Some(now()))
//FIXME      assert(schedule.scheduledDateReached)
      this !? AddSchedule(schedule) assert {
        case r: ScheduleAdded => assert(r.schedule.triggerable)
        case other            => fail(other.getClass)
      }
      // trigger schedule
      this !? TriggerSchedule(schedule.persistenceId, schedule.entityId, schedule.key) assert {
        case r: ScheduleTriggered => assert(!r.schedule.triggerable)
        case other                => fail(other.getClass)
      }
      // update scheduled date
      this !? AddSchedule(schedule.withScheduledDate(now())) assert {
        case r: ScheduleAdded => assert(r.schedule.triggerable)
        case other            => fail(other.getClass)
      }
      // trigger schedule
      this !? TriggerSchedule(schedule.persistenceId, schedule.entityId, schedule.key) assert {
        case r: ScheduleTriggered => assert(!r.schedule.triggerable)
        case other                => fail(other.getClass)
      }
    }
  }
}
