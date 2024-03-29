package app.softnetwork.scheduler.handlers

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence._
import app.softnetwork.scheduler.message.SampleMessages.{
  AddSample,
  LoadSample,
  SampleAdded,
  SampleLoaded
}
import app.softnetwork.scheduler.scalatest.SchedulerWithSampleTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import app.softnetwork.scheduler.message._
import app.softnetwork.scheduler.persistence.typed.SampleBehavior
import app.softnetwork.scheduler.model.{CronTab, Schedule}
import app.softnetwork.session.config.Settings
import app.softnetwork.session.handlers.SessionRefreshTokenDao
import app.softnetwork.session.service.BasicSessionMaterials
import com.softwaremill.session.RefreshTokenStorage
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContextExecutor

/** Created by smanciot on 19/03/2020.
  */
class SchedulerHandlerSpec
    extends SchedulerHandler
    with AnyWordSpecLike
    with SchedulerWithSampleTestKit
    with BasicSessionMaterials[Session] {

  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  override implicit def refreshTokenStorage: RefreshTokenStorage[Session] = SessionRefreshTokenDao(
    ts
  )

  override implicit def ts: ActorSystem[_] = typedSystem()

  override implicit lazy val ec: ExecutionContextExecutor = ts.executionContext

  override protected def sessionType: Session.SessionType =
    Settings.Session.SessionContinuityAndTransport

  "Scheduler" must {
    val cronTab = CronTab(SampleBehavior.persistenceId, ALL_KEY, "cron", "* * * * *")
    "add Cron Tab" in {
      // add Sample[sample] entity
      SampleHandler ? ("sample", AddSample) assert {
        case SampleAdded => succeed
        case _           => fail()
      }
      // add cron tab for all Sample entity
      this !? AddCronTab(cronTab) assert {
        case _: CronTabAdded => succeed
        case other           => fail(other.getClass)
      }
      // trigger cron tab
      this !? TriggerCronTab(cronTab.persistenceId, cronTab.entityId, cronTab.key) assert {
        case _: CronTabTriggered => succeed
        case other               => fail(other.getClass)
      }
      // a schedule for the Sample[sample] entity has been triggered
      probeSampleSchedule.receiveMessage()
      SampleHandler ? ("sample", LoadSample) assert {
        case r: SampleLoaded => assert(r.sample.triggered == 1)
        case other           => fail(other.getClass)
      }
      this !? LoadScheduler assert {
        case r: SchedulerLoaded =>
          val scheduler = r.scheduler
          log.info(scheduler.toProtoString)
          assert(scheduler.cronTabs.exists(ct => ct.uuid == cronTab.uuid))
          scheduler.schedules.find(s =>
            s.persistenceId == SampleBehavior.persistenceId && s.entityId == "sample" && s.key == cronTab.key
          ) match {
            case None => succeed
            case _    => fail("schedule found")
          }
        case _ => fail()
      }
      // a schedule for the Sample[sample] entity has been added
      probeScheduleAdded.receiveMessage()
      // the schedule for the Sample[sample] entity has been triggered
      probeSampleSchedule.receiveMessage()
      SampleHandler ? ("sample", LoadSample) assert {
        case r: SampleLoaded => assert(r.sample.triggered == 2)
        case other           => fail(other.getClass)
      }
    }
    "remove Cron Tab" in {
      this !? RemoveCronTab(cronTab.persistenceId, cronTab.entityId, cronTab.key) assert {
        case _: CronTabRemoved => succeed
        case other             => fail(other.getClass)
      }
      // the schedule for the Sample[sample] entity has been removed
      probeScheduleRemoved.receiveMessage()
      this !? LoadScheduler assert {
        case r: SchedulerLoaded =>
          val scheduler = r.scheduler
          log.info(scheduler.toProtoString)
          assert(!scheduler.cronTabs.exists(ct => ct.uuid == cronTab.uuid))
          scheduler.schedules.find(s =>
            s.persistenceId == SampleBehavior.persistenceId && s.entityId == "sample" && s.key == cronTab.key
          ) match {
            case Some(_) => fail()
            case _       => succeed
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
