package app.softnetwork.scheduler.service

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import app.softnetwork.api.server._
import app.softnetwork.scheduler.config.SchedulerSettings
import app.softnetwork.scheduler.handlers.{SchedulerDao, SchedulerHandler}
import app.softnetwork.scheduler.message._
import app.softnetwork.scheduler.model._
import app.softnetwork.serialization._
import app.softnetwork.session.config.Settings
import app.softnetwork.session.service.{ServiceWithSessionDirectives, SessionMaterials}
import com.softwaremill.session.CsrfDirectives.hmacTokenCsrfProtection
import com.softwaremill.session.CsrfOptions.checkHeader
import com.softwaremill.session.SessionConfig
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.jackson.Serialization
import org.json4s.{jackson, Formats}

trait SchedulerService
    extends Directives
    with DefaultComplete
    with Json4sSupport
    with StrictLogging
    with ServiceWithSessionDirectives[SchedulerCommand, SchedulerCommandResult]
    with SchedulerDao
    with SchedulerHandler { _: SessionMaterials =>

  implicit def sessionConfig: SessionConfig = Settings.Session.DefaultSessionConfig

  override implicit def ts: ActorSystem[_] = system

  implicit def serialization: Serialization.type = jackson.Serialization

  override implicit def formats: Formats = commonFormats

  val route: Route = {
    pathPrefix(SchedulerSettings.SchedulerPath) {
      // check anti CSRF token
      hmacTokenCsrfProtection(checkHeader) {
        // check if a session exists
        requiredSession(sc, gt) { session =>
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
      loadScheduler()(system) completeWith {
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
      loadScheduler()(system) completeWith {
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
    loadScheduler(scheduler)(system) completeWith {
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
