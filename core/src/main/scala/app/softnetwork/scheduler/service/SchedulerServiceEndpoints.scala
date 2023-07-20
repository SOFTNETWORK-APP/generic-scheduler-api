package app.softnetwork.scheduler.service

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import app.softnetwork.api.server.{ApiEndpoint, ApiErrors}
import app.softnetwork.api.server.ApiErrors.Unauthorized
import app.softnetwork.persistence.service.Service
import app.softnetwork.scheduler.config.SchedulerSettings
import app.softnetwork.scheduler.handlers.{SchedulerDao, SchedulerHandler}
import app.softnetwork.scheduler.message.{SchedulerNotFound, _}
import app.softnetwork.scheduler.model._
import app.softnetwork.session.service.SessionEndpoints
import com.softwaremill.session.{
  GetSessionTransport,
  SetSessionTransport,
  TapirCsrfCheckMode,
  TapirSessionContinuity
}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.headers.CookieValueWithMeta
import sttp.model.Method
import sttp.monad.FutureMonad
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.json4s.jsonBody
import sttp.tapir.server.{PartialServerEndpoint, ServerEndpoint}

import scala.concurrent.Future

trait SchedulerServiceEndpoints
    extends Service[SchedulerCommand, SchedulerCommandResult]
    with SchedulerDao
    with SchedulerHandler
    with ApiEndpoint {

  import app.softnetwork.serialization._

  def sessionEndpoints: SessionEndpoints

  def sc: TapirSessionContinuity[Session] = sessionEndpoints.sc

  def st: SetSessionTransport = sessionEndpoints.st

  def gt: GetSessionTransport = sessionEndpoints.gt

  def checkMode: TapirCsrfCheckMode[Session] = sessionEndpoints.checkMode

  def rootEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Unit,
    ApiErrors.ErrorInfo,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Any,
    Future
  ] = {
    val partial = sessionEndpoints.antiCsrfWithRequiredSession(sc, gt, checkMode)
    partial.endpoint
      .in(SchedulerSettings.SchedulerPath)
      .out(partial.securityOutput)
      .errorOut(errors)
      .serverSecurityLogic { inputs =>
        partial.securityLogic(new FutureMonad())(inputs).map {
          case Left(_)  => Left(Unauthorized("Unauthorized"))
          case Right(r) => Right((r._1, r._2))
        }
      }
  }

  def error(l: SchedulerCommandResult): ApiErrors.ErrorInfo =
    l match {
      case ScheduleNotFound         => ApiErrors.NotFound(ScheduleNotFound.message)
      case CronTabNotFound          => ApiErrors.NotFound(CronTabNotFound.message)
      case SchedulerNotFound        => ApiErrors.NotFound(SchedulerNotFound.message)
      case e: SchedulerErrorMessage => ApiErrors.BadRequest(e.message)
      case _                        => ApiErrors.BadRequest("")
    }

  val rootSchedulesEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session), //PRINCIPAL
    Unit, //SECURITY_INPUT
    ApiErrors.ErrorInfo,
    (Seq[Option[String]], Option[CookieValueWithMeta]), //OUTPUT
    Any,
    Future
  ] =
    rootEndpoint
      .in("schedules")

  val addScheduleEndpoint: ServerEndpoint[Any, Future] =
    rootSchedulesEndpoint.post
      .in(jsonBody[Schedule])
      .out(jsonBody[ScheduleAdded].description("Schedule added"))
      .serverLogic { principal => schedule =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddSchedule(schedule)).map {
          case r: ScheduleAdded => Right((principal._1._1, principal._1._2, r))
          case other            => Left(error(other))
        }
      }

  val removeScheduleEndpoint: ServerEndpoint[Any, Future] =
    rootSchedulesEndpoint.delete
      .in(jsonBody[RemoveSchedule])
      .out(jsonBody[ScheduleRemoved].description("Schedule removed"))
      .serverLogic { principal => cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd).map {
          case r: ScheduleRemoved => Right((principal._1._1, principal._1._2, r))
          case other              => Left(error(other))
        }
      }

  val listSchedulesEndpoint: ServerEndpoint[Any, Future] =
    rootSchedulesEndpoint.get
      .out(jsonBody[Seq[ScheduleView]].description("Schedules loaded"))
      .serverLogic { principal => _ =>
        loadScheduler().map {
          case Some(scheduler) =>
            Right((principal._1._1, principal._1._2, scheduler.schedules.map(_.view)))
          case _ => Left(error(SchedulerNotFound))
        }
      }

  val rootCronTabsEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Unit,
    ApiErrors.ErrorInfo,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Any,
    Future
  ] =
    rootEndpoint
      .in("crons")

  val addCronTabEndpoint: ServerEndpoint[Any, Future] =
    rootCronTabsEndpoint.post
      .in(jsonBody[CronTab])
      .out(jsonBody[CronTabAdded].description("Cron tab added"))
      .serverLogic { principal => cronTab =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddCronTab(cronTab)).map {
          case r: CronTabAdded => Right((principal._1._1, principal._1._2, r))
          case other           => Left(error(other))
        }
      }

  val removeCronTabEndpoint: ServerEndpoint[Any, Future] =
    rootCronTabsEndpoint.delete
      .in(jsonBody[RemoveCronTab])
      .out(jsonBody[CronTabRemoved].description("Cron tab removed"))
      .serverLogic { principal => cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd).map {
          case r: CronTabRemoved => Right((principal._1._1, principal._1._2, r))
          case other             => Left(error(other))
        }
      }

  val listCronTabsEndpoint: ServerEndpoint[Any, Future] =
    rootCronTabsEndpoint.get
      .out(jsonBody[Seq[CronTab]].description("Cron tabs loaded"))
      .serverLogic { principal => _ =>
        loadScheduler().map {
          case Some(scheduler) => Right((principal._1._1, principal._1._2, scheduler.cronTabs))
          case _               => Left(error(SchedulerNotFound))
        }
      }

  val loadSchedulerEndpoint: ServerEndpoint[Any, Future] =
    rootEndpoint.get
      .in(paths)
      .out(jsonBody[Scheduler].description("Scheduler loaded"))
      .serverLogic { principal => paths =>
        val id =
          paths match {
            case Nil => None
            case _   => Some(paths.head)
          }
        loadScheduler(id).map {
          case Some(scheduler) => Right((principal._1._1, principal._1._2, scheduler))
          case _               => Left(error(SchedulerNotFound))
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

  lazy val route: Route = apiRoute
}

object SchedulerServiceEndpoints {
  def apply(
    _system: ActorSystem[_],
    _sessionEndpoints: SessionEndpoints
  ): SchedulerServiceEndpoints = new SchedulerServiceEndpoints {
    override implicit def system: ActorSystem[_] = _system
    lazy val log: Logger = LoggerFactory getLogger getClass.getName
    override def sessionEndpoints: SessionEndpoints = _sessionEndpoints
  }
}
