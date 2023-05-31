package app.softnetwork.scheduler.service

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import app.softnetwork.api.server._
import app.softnetwork.persistence.service.Service
import app.softnetwork.scheduler.config.SchedulerSettings
import app.softnetwork.scheduler.handlers.{SchedulerDao, SchedulerHandler}
import app.softnetwork.scheduler.message._
import app.softnetwork.scheduler.model._
import app.softnetwork.serialization._
import app.softnetwork.session.service.SessionService
import com.softwaremill.session.CsrfDirectives.hmacTokenCsrfProtection
import com.softwaremill.session.CsrfOptions.checkHeader
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.jackson.Serialization
import org.json4s.{jackson, Formats}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

trait SchedulerService
    extends Directives
    with DefaultComplete
    with Json4sSupport
    with StrictLogging
    with Service[SchedulerCommand, SchedulerCommandResult]
    with SchedulerDao
    with SchedulerHandler {

  implicit def serialization: Serialization.type = jackson.Serialization

  override implicit def formats: Formats = commonFormats

  import Session._

  def sessionService: SessionService

  val route: Route = {
    pathPrefix(SchedulerSettings.SchedulerPath) {
      // check anti CSRF token
      hmacTokenCsrfProtection(checkHeader) {
        // check if a session exists
        sessionService.requiredSession { session =>
          // only administrators should be allowed to access this resource
          if (session.admin) {
            schedules ~ crons ~ scheduler
          } else {
            complete(HttpResponse(StatusCodes.Unauthorized))
          }
        }
      }
    }
  }

  lazy val schedules: Route = pathPrefix("schedules") {
    post {
      entity(as[Schedule]) { schedule =>
        run(
          SchedulerSettings.SchedulerConfig.id.getOrElse("*"),
          AddSchedule(schedule)
        ) completeWith {
          case r: ScheduleAdded => complete(HttpResponse(StatusCodes.OK, entity = r))
          case r: SchedulerErrorMessage =>
            complete(HttpResponse(StatusCodes.BadRequest, entity = r))
          case _ => complete(HttpResponse(StatusCodes.BadRequest))
        }
      }
    } ~ delete {
      entity(as[RemoveSchedule]) { cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd) completeWith {
          case r: ScheduleRemoved       => complete(HttpResponse(StatusCodes.OK, entity = r))
          case r: ScheduleNotFound.type => complete(HttpResponse(StatusCodes.NotFound, entity = r))
          case r: SchedulerErrorMessage =>
            complete(HttpResponse(StatusCodes.BadRequest, entity = r))
          case _ => complete(HttpResponse(StatusCodes.BadRequest))
        }
      }
    } ~ get {
      loadScheduler() completeWith {
        case Some(s) =>
          complete(
            HttpResponse(
              StatusCodes.OK,
              entity = s.schedules.map(_.view).toList
            )
          )
        case _ => complete(HttpResponse(StatusCodes.NotFound))
      }
    }
  }

  lazy val crons: Route = pathPrefix("crons") {
    post {
      entity(as[CronTab]) { cronTab =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddCronTab(cronTab)) completeWith {
          case r: CronTabAdded => complete(HttpResponse(StatusCodes.OK, entity = r))
          case r: SchedulerErrorMessage =>
            complete(HttpResponse(StatusCodes.BadRequest, entity = r))
          case _ => complete(HttpResponse(StatusCodes.BadRequest))
        }
      }
    } ~ delete {
      entity(as[RemoveCronTab]) { cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd) completeWith {
          case r: CronTabRemoved       => complete(HttpResponse(StatusCodes.OK, entity = r))
          case r: CronTabNotFound.type => complete(HttpResponse(StatusCodes.NotFound, entity = r))
          case r: SchedulerErrorMessage =>
            complete(HttpResponse(StatusCodes.BadRequest, entity = r))
          case _ => complete(HttpResponse(StatusCodes.BadRequest))
        }
      }
    } ~ get {
      loadScheduler() completeWith {
        case Some(s) =>
          complete(
            HttpResponse(
              StatusCodes.OK,
              entity = s.cronTabs.toList
            )
          )
        case _ => complete(HttpResponse(StatusCodes.NotFound))
      }
    }
  }

  lazy val scheduler: Route =
    get {
      pathSuffix(Segment) { value =>
        getScheduler(Some(value))
      } ~ pathEnd {
        getScheduler(None)
      }
    }

  private[this] def getScheduler(scheduler: Option[String]): Route = {
    loadScheduler(scheduler) completeWith {
      case Some(s) =>
        complete(
          HttpResponse(
            StatusCodes.OK,
            entity = s
          )
        )
      case _ => complete(HttpResponse(StatusCodes.NotFound))
    }
  }
}

object SchedulerService {
  def apply(_system: ActorSystem[_], _sessionService: SessionService): SchedulerService = {
    new SchedulerService {
      override def sessionService: SessionService = _sessionService
      override implicit def system: ActorSystem[_] = _system
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
    }
  }

  def oneOffCookie(system: ActorSystem[_]): SchedulerService =
    SchedulerService(system, SessionService.oneOffCookie(system))

  def oneOffHeader(system: ActorSystem[_]): SchedulerService =
    SchedulerService(system, SessionService.oneOffHeader(system))

  def refreshableCookie(system: ActorSystem[_]): SchedulerService =
    SchedulerService(system, SessionService.refreshableCookie(system))

  def refreshableHeader(system: ActorSystem[_]): SchedulerService =
    SchedulerService(system, SessionService.refreshableHeader(system))

}
