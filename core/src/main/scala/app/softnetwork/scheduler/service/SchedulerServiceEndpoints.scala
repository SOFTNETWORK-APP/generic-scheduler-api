package app.softnetwork.scheduler.service

import akka.http.scaladsl.server.Route
import app.softnetwork.api.server.ApiEndpoint
import app.softnetwork.persistence.service.Service
import app.softnetwork.scheduler.config.SchedulerSettings
import app.softnetwork.scheduler.handlers.{SchedulerDao, SchedulerHandler}
import app.softnetwork.scheduler.message._
import app.softnetwork.scheduler.model._
import app.softnetwork.session.service.SessionEndpoints
import com.softwaremill.session.{
  CsrfCheckHeader,
  SessionContinuityEndpoints,
  SessionResult,
  SessionTransportEndpoints
}
import org.json4s.Formats
import org.softnetwork.session.model.Session
import sttp.model.headers.CookieValueWithMeta
import sttp.model.{Method, StatusCode}
import sttp.monad.FutureMonad
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.json4s.jsonBody
import sttp.tapir.server.{
  PartialServerEndpoint,
  PartialServerEndpointWithSecurityOutput,
  ServerEndpoint
}

import scala.concurrent.{ExecutionContext, Future}

trait SchedulerServiceEndpoints
    extends SessionEndpoints
    with Service[SchedulerCommand, SchedulerCommandResult]
    with SchedulerDao
    with SchedulerHandler
    with ApiEndpoint
    with CsrfCheckHeader {
  _: SessionTransportEndpoints[Session] with SessionContinuityEndpoints[Session] =>

  override implicit lazy val ec: ExecutionContext = system.executionContext

  import app.softnetwork.serialization._

  implicit def formats: Formats = commonFormats

  val antiCsrfAndRequiredSessionEndpoint: PartialServerEndpointWithSecurityOutput[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    SessionResult[Session],
    Unit,
    Unit,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Unit,
    Any,
    Future
  ] =
    // check anti CSRF token
    hmacTokenCsrfProtectionEndpoint(
      // check if a session exists
      requiredSession
    )

  val securityEndpoint: PartialServerEndpointWithSecurityOutput[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    Session,
    Unit,
    Unit,
    Unit,
    Unit,
    Any,
    Future
  ] =
    antiCsrfAndRequiredSessionEndpoint.endpoint.serverSecurityLogicWithOutput { inputs =>
      antiCsrfAndRequiredSessionEndpoint.securityLogic(new FutureMonad())(inputs).map {
        case Left(l) => Left(l)
        case Right(r) =>
          r._2.toOption match {
            // only administrators should be allowed to access this resource
            case Some(session) if session.admin => Right((), session)
            case _                              => Left(())
          }
      }
    }

  def rootEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    Session,
    Unit,
    SchedulerCommandResult,
    Unit,
    Any,
    Future
  ] =
    securityEndpoint.endpoint
      .in(SchedulerSettings.SchedulerPath)
      .errorOut(
        oneOf[SchedulerCommandResult](
          oneOfVariant[SchedulerNotFound.type](
            statusCode(StatusCode.NotFound).and(emptyOutputAs(SchedulerNotFound))
          ),
          oneOfVariant[AuthenticationError.type](
            statusCode(StatusCode.Unauthorized).and(emptyOutputAs(AuthenticationError))
          )
        )
      )
      .serverSecurityLogic { inputs =>
        securityEndpoint.securityLogic(new FutureMonad())(inputs).map {
          case Left(_)  => Left(AuthenticationError)
          case Right(r) => Right(r._2)
        }
      }

  val rootSchedulesEndpoint =
    rootEndpoint
      .in("schedules")

  val addScheduleEndpoint =
    rootSchedulesEndpoint.post
      .in(jsonBody[Schedule])
      .out(jsonBody[ScheduleAdded])
      .serverLogic { _ => schedule =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddSchedule(schedule)).map {
          case r: ScheduleAdded => Right(r)
          case other            => Left(other)
        }
      }

  val removeScheduleEndpoint =
    rootSchedulesEndpoint.delete
      .in(jsonBody[RemoveSchedule])
      .out(jsonBody[ScheduleRemoved])
      .serverLogic { _ => cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd).map {
          case r: ScheduleRemoved => Right(r)
          case other              => Left(other)
        }
      }

  val listSchedulesEndpoint =
    rootSchedulesEndpoint.get
      .out(jsonBody[Seq[ScheduleView]])
      .serverLogic { _ => _ =>
        loadScheduler().map {
          case Some(scheduler) => Right(scheduler.schedules.map(_.view))
          case _               => Left(SchedulerNotFound)
        }
      }

  val rootCronTabsEndpoint =
    rootEndpoint
      .in("crons")

  val addCronTabEndpoint =
    rootCronTabsEndpoint.post
      .in(jsonBody[CronTab])
      .out(jsonBody[CronTabAdded])
      .serverLogic { _ => cronTab =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddCronTab(cronTab)).map {
          case r: CronTabAdded => Right(r)
          case other           => Left(other)
        }
      }

  val removeCronTabEndpoint =
    rootCronTabsEndpoint.delete
      .in(jsonBody[RemoveCronTab])
      .out(jsonBody[CronTabRemoved])
      .serverLogic { _ => cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd).map {
          case r: CronTabRemoved => Right(r)
          case other             => Left(other)
        }
      }

  val listCronTabsEndpoint =
    rootCronTabsEndpoint.get
      .out(jsonBody[Seq[CronTab]])
      .serverLogic { _ => _ =>
        loadScheduler().map {
          case Some(scheduler) => Right(scheduler.cronTabs)
          case _               => Left(SchedulerNotFound)
        }
      }

  val loadSchedulerEndpoint =
    rootEndpoint.get
      .in(paths)
      .out(jsonBody[Scheduler])
      .serverLogic { _ => paths =>
        val id =
          paths match {
            case Nil => None
            case _   => Some(paths.head)
          }
        loadScheduler(id).map {
          case Some(scheduler) => Right(scheduler)
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
