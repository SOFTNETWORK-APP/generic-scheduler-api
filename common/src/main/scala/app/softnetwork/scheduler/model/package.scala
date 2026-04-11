package app.softnetwork.scheduler

import app.softnetwork.persistence.now

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import com.typesafe.scalalogging.StrictLogging

import java.time.ZoneId
import java.time.ZonedDateTime
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
    def scheduledDateReached: Boolean =
      scheduledDate.isDefined && (now().after(getScheduledDate) || now().equals(getScheduledDate))
    /* flag to indicate whether we could trigger this schedule
     */
    def triggerable: Boolean =
      // the schedule has never been triggered or can be triggered repeatedly and has no scheduled date
      ((lastTriggered.isEmpty || repeatedly.getOrElse(false)) && scheduledDate.isEmpty) ||
      // the schedule should be triggered at a specified date that has been reached
      // and it has not yet been triggered or has been triggered before the specified date
      (scheduledDateReached &&
      (lastTriggered.isEmpty || getLastTriggered.before(getScheduledDate)))

    /* flag to indicate whether we should remove this schedule
     */
    def removable: Boolean = {
      // the schedule could not be triggered now
      !triggerable &&
      // the schedule can not be triggered repeatedly or should be triggered at a specified date
      (!repeatedly.getOrElse(false) || scheduledDate.isDefined) &&
      // the schedule has not defined a date at which it should be triggered
      // or it has already been triggered at or after the specified date
      (scheduledDate.isEmpty || (lastTriggered.isDefined && (getLastTriggered.after(
        getScheduledDate
      ) || getLastTriggered.equals(getScheduledDate))))
    }

    def view: ScheduleView = ScheduleView(this)
  }

  private val cronDefinition =
    CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)

  private val cronParser = new CronParser(cronDefinition)

  trait CronTabItem extends StrictLogging {
    def cron: String

    lazy val executionTime: ExecutionTime = Try {
      ExecutionTime.forCron(cronParser.parse(cron))
    } match {
      case Success(s) => s
      case Failure(f) =>
        logger.error(f.getMessage + s" -> [$cron]")
        ExecutionTime.forCron(cronParser.parse("*/5 * * * *")) // By default every 5 minutes
    }

    def nextLocalDateTime(): Option[LocalDateTime] = {
      val next = executionTime.nextExecution(ZonedDateTime.now(ZoneId.systemDefault()))
      if (next.isPresent) Some(next.get().toLocalDateTime) else None
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
