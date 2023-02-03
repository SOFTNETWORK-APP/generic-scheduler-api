package app.softnetwork.scheduler

import app.softnetwork.persistence.now

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import com.markatta.akron.CronExpression
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/** Created by smanciot on 11/05/2021.
  */
package object model {

  trait SchedulerItem {
    def persistenceId: String

    def entityId: String

    def key: String

    val uuid = s"$persistenceId#$entityId#$key"
  }

  trait ScheduleDecorator {
    _: Schedule =>
    /* flag to indicate whether the scheduled date has been reached or not
     */
    val scheduledDateReached: Boolean =
      scheduledDate.isDefined && (now().after(getScheduledDate) || now().equals(getScheduledDate))
    /* flag to indicate whether we could trigger this schedule
     */
    val triggerable: Boolean =
      // the schedule has never been triggered or can be triggered repeatedly and has no scheduled date
      ((lastTriggered.isEmpty || repeatedly.getOrElse(false)) && scheduledDate.isEmpty) ||
      // the schedule should be triggered at a specified date that has been reached
      // and it has not yet been triggered or has been triggered before the specified date
      (scheduledDateReached &&
      (lastTriggered.isEmpty || getLastTriggered.before(getScheduledDate)))

    /* flag to indicate whether we should remove this schedule
     */
    val removable: Boolean = {
      // the schedule could not be triggered now
      !triggerable &&
      // the schedule can not be triggered repeatedly
      !repeatedly.getOrElse(false) &&
      // the schedule has not defined a date at which it should be triggered
      // or it has already been triggered at or after the specified date
      (scheduledDate.isEmpty || (lastTriggered.isDefined && (getLastTriggered.after(
        getScheduledDate
      ) || getLastTriggered.equals(getScheduledDate))))
    }

    def view: ScheduleView = ScheduleView(this)
  }

  trait CronTabItem extends StrictLogging {
    def cron: String

    lazy val cronExpression: CronExpression = Try {
      CronExpression(cron)
    } match {
      case Success(s) => s
      case Failure(f) =>
        logger.error(f.getMessage + s" -> [$cron]")
        CronExpression("*/5 * * * *") // By default every 5 minutes
    }

    def nextLocalDateTime(): Option[LocalDateTime] = {
      cronExpression.nextTriggerTime(LocalDateTime.now())
    }

    def next(from: Option[Date] = None): Option[FiniteDuration] = {
      (from match {
        case Some(s) => Some(new Timestamp(s.getTime).toLocalDateTime)
        case _       => nextLocalDateTime()
      }) match {
        case Some(ldt) =>
          val diff = LocalDateTime.now().until(ldt, ChronoUnit.SECONDS)
          if (diff < 0) {
            Some(Math.max(1, 60 - Math.abs(diff)).seconds)
          } else {
            Some(diff.seconds)
          }

        case _ => None
      }
    }
  }

  implicit def cronTabToSchedule(cronTab: CronTab): Option[Schedule] = {
    cronTab.nextTriggered match {
      case Some(date) =>
        Some(
          Schedule.defaultInstance
            .withPersistenceId(cronTab.persistenceId)
            .withEntityId(cronTab.entityId)
            .withKey(cronTab.key)
            .withScheduledDate(date)
//            .withRepeatedly(true)
            .withCronTab(cronTab.uuid)
        )
      case _ => None
    }
  }
}
