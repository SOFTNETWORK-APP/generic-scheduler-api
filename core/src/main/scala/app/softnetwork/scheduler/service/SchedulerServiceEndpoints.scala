package app.softnetwork.scheduler.service

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiErrors
import app.softnetwork.scheduler.config.SchedulerSettings
import app.softnetwork.scheduler.handlers.{SchedulerDao, SchedulerHandler}
import app.softnetwork.scheduler.message.{SchedulerNotFound, _}
import app.softnetwork.scheduler.model._
import app.softnetwork.session.config.Settings
import app.softnetwork.session.service.{ServiceWithSessionEndpoints, SessionMaterials}
import com.softwaremill.session.SessionConfig
import org.softnetwork.session.model.Session
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.Method
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.json.json4s.jsonBody
import sttp.tapir.server.{PartialServerEndpointWithSecurityOutput, ServerEndpoint}

import scala.concurrent.Future
import scala.language.implicitConversions

trait SchedulerServiceEndpoints
    extends ServiceWithSessionEndpoints[SchedulerCommand, SchedulerCommandResult]
    with SchedulerDao
    with SchedulerHandler { _: SessionMaterials =>

  import app.softnetwork.serialization._

  implicit def sessionConfig: SessionConfig = Settings.Session.DefaultSessionConfig

  override implicit def ts: ActorSystem[_] = system

  def secureEndpoint: PartialServerEndpointWithSecurityOutput[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    Session,
    Unit,
    Any,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Unit,
    Any,
    Future
  ] =
    ApiErrors
      .withApiErrorVariants(
        antiCsrfWithRequiredSession(sc, gt, checkMode)
      )
      .in(SchedulerSettings.SchedulerPath)

  override implicit def resultToApiError(l: SchedulerCommandResult): ApiErrors.ErrorInfo =
    l match {
      case ScheduleNotFound         => ApiErrors.NotFound(ScheduleNotFound.message)
      case CronTabNotFound          => ApiErrors.NotFound(CronTabNotFound.message)
      case SchedulerNotFound        => ApiErrors.NotFound(SchedulerNotFound.message)
      case e: SchedulerErrorMessage => ApiErrors.BadRequest(e.message)
      case _                        => ApiErrors.BadRequest("")
    }

  val rootSchedulesEndpoint: PartialServerEndpointWithSecurityOutput[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    Session,
    Unit,
    Any,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Unit,
    Any,
    Future
  ] =
    secureEndpoint
      .in("schedules")

  val addScheduleEndpoint: ServerEndpoint[Any, Future] =
    rootSchedulesEndpoint.post
      .in(jsonBody[Schedule])
      .out(jsonBody[ScheduleAdded].description("Schedule added"))
      .serverLogic { _ => schedule =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddSchedule(schedule)).map {
          case r: ScheduleAdded => Right(r)
          case other            => Left(resultToApiError(other))
        }
      }

  val removeScheduleEndpoint: ServerEndpoint[Any, Future] =
    rootSchedulesEndpoint.delete
      .in(jsonBody[RemoveSchedule])
      .out(jsonBody[ScheduleRemoved].description("Schedule removed"))
      .serverLogic { _ => cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd).map {
          case r: ScheduleRemoved => Right(r)
          case other              => Left(resultToApiError(other))
        }
      }

  val listSchedulesEndpoint: ServerEndpoint[Any, Future] =
    rootSchedulesEndpoint.get
      .out(jsonBody[Seq[ScheduleView]].description("Schedules loaded"))
      .serverLogic { _ => _ =>
        loadScheduler()(system).map {
          case Some(scheduler) =>
            Right(scheduler.schedules.map(_.view))
          case _ => Left(resultToApiError(SchedulerNotFound))
        }
      }

  val rootCronTabsEndpoint: PartialServerEndpointWithSecurityOutput[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    Session,
    Unit,
    Any,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Unit,
    Any,
    Future
  ] =
    secureEndpoint
      .in("crons")

  val addCronTabEndpoint: ServerEndpoint[Any, Future] =
    rootCronTabsEndpoint.post
      .in(jsonBody[CronTab])
      .out(jsonBody[CronTabAdded].description("Cron tab added"))
      .serverLogic { _ => cronTab =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddCronTab(cronTab)).map {
          case r: CronTabAdded => Right(r)
          case other           => Left(resultToApiError(other))
        }
      }

  val removeCronTabEndpoint: ServerEndpoint[Any, Future] =
    rootCronTabsEndpoint.delete
      .in(jsonBody[RemoveCronTab])
      .out(jsonBody[CronTabRemoved].description("Cron tab removed"))
      .serverLogic { _ => cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd).map {
          case r: CronTabRemoved => Right(r)
          case other             => Left(resultToApiError(other))
        }
      }

  val listCronTabsEndpoint: ServerEndpoint[Any, Future] =
    rootCronTabsEndpoint.get
      .out(jsonBody[Seq[CronTab]].description("Cron tabs loaded"))
      .serverLogic { _ => _ =>
        loadScheduler()(system).map {
          case Some(scheduler) => Right(scheduler.cronTabs)
          case _               => Left(resultToApiError(SchedulerNotFound))
        }
      }

  val loadSchedulerEndpoint: ServerEndpoint[Any, Future] =
    secureEndpoint.get
      .in(paths)
      .out(jsonBody[Scheduler].description("Scheduler loaded"))
      .serverLogic { _ => paths =>
        val id =
          paths match {
            case Nil => None
            case _   => Some(paths.head)
          }
        loadScheduler(id)(system).map {
          case Some(scheduler) => Right(scheduler)
          case _               => Left(resultToApiError(SchedulerNotFound))
        }
      }

  override def endpoints: List[ServerEndpoint[AkkaStreams with capabilities.WebSockets, Future]] =
    List(
      addScheduleEndpoint,
      removeScheduleEndpoint,
      listSchedulesEndpoint,
      addCronTabEndpoint,
      removeCronTabEndpoint,
      listCronTabsEndpoint,
      loadSchedulerEndpoint
    )

}
