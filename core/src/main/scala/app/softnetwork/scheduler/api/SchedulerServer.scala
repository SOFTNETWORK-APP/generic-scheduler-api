package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.scheduler.handlers.SchedulerHandler
import app.softnetwork.scheduler.message.{
  AddCronTab,
  AddSchedule,
  CronTabAdded,
  CronTabRemoved,
  LoadScheduler,
  RemoveCronTab,
  RemoveSchedule,
  ScheduleAdded,
  ScheduleRemoved,
  SchedulerLoaded
}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContextExecutor, Future}

trait SchedulerServer extends SchedulerServiceApi with SchedulerHandler {

  implicit def system: ActorSystem[_]

  implicit lazy val ec: ExecutionContextExecutor = system.executionContext

  override def addSchedule(in: AddScheduleRequest): Future[AddScheduleResponse] = {
    in.schedule match {
      case Some(schedule) =>
        !?(AddSchedule(schedule)) map {
          case _: ScheduleAdded => AddScheduleResponse(true)
          case _                => AddScheduleResponse()
        }
      case _ => Future.successful(AddScheduleResponse())
    }
  }

  override def removeSchedule(in: RemoveScheduleRequest): Future[RemoveScheduleResponse] = {
    !?(RemoveSchedule(in.persistenceId, in.entityId, in.key)) map {
      case _: ScheduleRemoved => RemoveScheduleResponse(true)
      case _                  => RemoveScheduleResponse()
    }
  }

  override def addCronTab(in: AddCronTabRequest): Future[AddCronTabResponse] = {
    in.cronTab match {
      case Some(cronTab) =>
        !?(AddCronTab(cronTab)) map {
          case _: CronTabAdded => AddCronTabResponse(true)
          case _               => AddCronTabResponse()
        }
    }
  }

  override def removeCronTab(in: RemoveCronTabRequest): Future[RemoveCronTabResponse] = {
    !?(RemoveCronTab(in.persistenceId, in.entityId, in.key)) map {
      case _: CronTabRemoved => RemoveCronTabResponse(true)
      case _                 => RemoveCronTabResponse()
    }
  }

  override def loadScheduler(in: LoadSchedulerRequest): Future[LoadSchedulerResponse] = {
    in.schedulerId match {
      case Some(value) =>
        ??(value, LoadScheduler) map {
          case r: SchedulerLoaded => LoadSchedulerResponse(Some(r.scheduler))
          case _                  => LoadSchedulerResponse()
        }
      case None =>
        !?(LoadScheduler) map {
          case r: SchedulerLoaded => LoadSchedulerResponse(Some(r.scheduler))
          case _                  => LoadSchedulerResponse()
        }
    }
  }
}

object SchedulerServer {
  def apply(sys: ActorSystem[_]): SchedulerServer = {
    new SchedulerServer {
      override implicit val system: ActorSystem[_] = sys
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
    }
  }
}
