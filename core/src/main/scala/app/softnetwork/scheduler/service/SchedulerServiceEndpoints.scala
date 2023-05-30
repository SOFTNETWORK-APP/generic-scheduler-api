package app.softnetwork.scheduler.service

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import app.softnetwork.api.server.ApiEndpoint
import app.softnetwork.persistence.service.Service
import app.softnetwork.scheduler.config.SchedulerSettings
import app.softnetwork.scheduler.handlers.{SchedulerDao, SchedulerHandler}
import app.softnetwork.scheduler.message._
import app.softnetwork.scheduler.model._
import app.softnetwork.session.service.SessionEndpoints
import org.json4s.Formats
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session
import sttp.model.headers.CookieValueWithMeta
import sttp.model.{Method, StatusCode}
import sttp.monad.FutureMonad
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.json4s.jsonBody
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.server.{PartialServerEndpoint, ServerEndpoint}

import scala.concurrent.Future

trait SchedulerServiceEndpoints
    extends Service[SchedulerCommand, SchedulerCommandResult]
    with SchedulerDao
    with SchedulerHandler
    with ApiEndpoint {

  import app.softnetwork.serialization._

  implicit def formats: Formats = commonFormats

  def sessionEndpoints: SessionEndpoints

  def rootEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Unit,
    SchedulerCommandResult,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Any,
    Future
  ] =
    sessionEndpoints.antiCsrfWithRequiredSession.endpoint
      .in(SchedulerSettings.SchedulerPath)
      .out(sessionEndpoints.antiCsrfWithRequiredSession.securityOutput)
      .errorOut(
        oneOf[SchedulerCommandResult](
          oneOfVariant[SchedulerNotFound.type](
            statusCode(StatusCode.NotFound)
              .and(emptyOutputAs(SchedulerNotFound).description("Scheduler not found"))
          ),
          oneOfVariant[ScheduleNotFound.type](
            statusCode(StatusCode.NotFound)
              .and(emptyOutputAs(ScheduleNotFound).description("Schedule not found"))
          ),
          oneOfVariant[CronTabNotFound.type](
            statusCode(StatusCode.NotFound)
              .and(emptyOutputAs(CronTabNotFound).description("Cron tab not found"))
          ),
          oneOfVariant[UnauthorizedError.type](
            statusCode(StatusCode.Unauthorized)
              .and(emptyOutputAs(UnauthorizedError).description("Unauthorized"))
          )
        )
      )
      .serverSecurityLogic { inputs =>
        sessionEndpoints.antiCsrfWithRequiredSession.securityLogic(new FutureMonad())(inputs).map {
          case Left(_)  => Left(UnauthorizedError)
          case Right(r) => Right((r._1, r._2))
        }
      }

  val rootSchedulesEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Method, Option[String], Option[String]), //SI
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session), //PRINCIPAL
    Unit, //INPUT
    SchedulerCommandResult, //E
    (Seq[Option[String]], Option[CookieValueWithMeta]), //OUTPUT
    Any,
    Future
  ] =
    rootEndpoint
      .in("schedules")

  val addScheduleEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Schedule,
    SchedulerCommandResult,
    (Seq[Option[String]], Option[CookieValueWithMeta], ScheduleAdded),
    Any,
    Future
  ] =
    rootSchedulesEndpoint.post
      .in(jsonBody[Schedule])
      .out(jsonBody[ScheduleAdded])
      .serverLogic { principal => schedule =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddSchedule(schedule)).map {
          case r: ScheduleAdded => Right((principal._1._1, principal._1._2, r))
          case other            => Left(other)
        }
      }

  val removeScheduleEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    RemoveSchedule,
    SchedulerCommandResult,
    (Seq[Option[String]], Option[CookieValueWithMeta], ScheduleRemoved),
    Any,
    Future
  ] =
    rootSchedulesEndpoint.delete
      .in(jsonBody[RemoveSchedule])
      .out(jsonBody[ScheduleRemoved])
      .serverLogic { principal => cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd).map {
          case r: ScheduleRemoved => Right((principal._1._1, principal._1._2, r))
          case other              => Left(other)
        }
      }

  val listSchedulesEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Unit,
    SchedulerCommandResult,
    (Seq[Option[String]], Option[CookieValueWithMeta], Seq[ScheduleView]),
    Any,
    Future
  ] =
    rootSchedulesEndpoint.get
      .out(jsonBody[Seq[ScheduleView]])
      .serverLogic { principal => _ =>
        loadScheduler().map {
          case Some(scheduler) =>
            Right((principal._1._1, principal._1._2, scheduler.schedules.map(_.view)))
          case _ => Left(SchedulerNotFound)
        }
      }

  val rootCronTabsEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Unit,
    SchedulerCommandResult,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Any,
    Future
  ] =
    rootEndpoint
      .in("crons")

  val addCronTabEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    CronTab,
    SchedulerCommandResult,
    (Seq[Option[String]], Option[CookieValueWithMeta], CronTabAdded),
    Any,
    Future
  ] =
    rootCronTabsEndpoint.post
      .in(jsonBody[CronTab])
      .out(jsonBody[CronTabAdded])
      .serverLogic { principal => cronTab =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddCronTab(cronTab)).map {
          case r: CronTabAdded => Right((principal._1._1, principal._1._2, r))
          case other           => Left(other)
        }
      }

  val removeCronTabEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    RemoveCronTab,
    SchedulerCommandResult,
    (Seq[Option[String]], Option[CookieValueWithMeta], CronTabRemoved),
    Any,
    Future
  ] =
    rootCronTabsEndpoint.delete
      .in(jsonBody[RemoveCronTab])
      .out(jsonBody[CronTabRemoved])
      .serverLogic { principal => cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd).map {
          case r: CronTabRemoved => Right((principal._1._1, principal._1._2, r))
          case other             => Left(other)
        }
      }

  val listCronTabsEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Unit,
    SchedulerCommandResult,
    (Seq[Option[String]], Option[CookieValueWithMeta], Seq[CronTab]),
    Any,
    Future
  ] =
    rootCronTabsEndpoint.get
      .out(jsonBody[Seq[CronTab]])
      .serverLogic { principal => _ =>
        loadScheduler().map {
          case Some(scheduler) => Right((principal._1._1, principal._1._2, scheduler.cronTabs))
          case _               => Left(SchedulerNotFound)
        }
      }

  val loadSchedulerEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    List[String],
    SchedulerCommandResult,
    (Seq[Option[String]], Option[CookieValueWithMeta], Scheduler),
    Any,
    Future
  ] =
    rootEndpoint.get
      .in(paths)
      .out(jsonBody[Scheduler])
      .serverLogic { principal => paths =>
        val id =
          paths match {
            case Nil => None
            case _   => Some(paths.head)
          }
        loadScheduler(id).map {
          case Some(scheduler) => Right((principal._1._1, principal._1._2, scheduler))
          case _               => Left(SchedulerNotFound)
        }
      }

  override def endpoints: List[ServerEndpoint[Any, Future]] = List(
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
