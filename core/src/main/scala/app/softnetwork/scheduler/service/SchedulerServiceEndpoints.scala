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
    UnauthorizedError.type,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Any,
    Future
  ] =
    sessionEndpoints.antiCsrfWithRequiredSession.endpoint
      .in(SchedulerSettings.SchedulerPath)
      .out(sessionEndpoints.antiCsrfWithRequiredSession.securityOutput)
      .errorOut(
        oneOf[UnauthorizedError.type](
          oneOfVariant[UnauthorizedError.type](
            statusCode(StatusCode.Unauthorized)
              .and(jsonBody[UnauthorizedError.type].description("Unauthorized"))
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
    UnauthorizedError.type, //E
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
    UnauthorizedError.type,
    (Seq[Option[String]], Option[CookieValueWithMeta], SchedulerCommandResult),
    Any,
    Future
  ] =
    rootSchedulesEndpoint.post
      .in(jsonBody[Schedule])
      .out(
        oneOf[SchedulerCommandResult](
          oneOfVariant[ScheduleAdded](
            statusCode(StatusCode.Ok)
              .and(jsonBody[ScheduleAdded].description("Schedule added"))
          ),
          oneOfVariant[SchedulerNotFound.type](
            statusCode(StatusCode.NotFound)
              .and(jsonBody[SchedulerNotFound.type].description("Scheduler not found"))
          )
        )
      )
      .serverLogic { principal => schedule =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddSchedule(schedule)).map { r =>
          Right((principal._1._1, principal._1._2, r))
        }
      }

  val removeScheduleEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    RemoveSchedule,
    UnauthorizedError.type,
    (Seq[Option[String]], Option[CookieValueWithMeta], SchedulerCommandResult),
    Any,
    Future
  ] =
    rootSchedulesEndpoint.delete
      .in(jsonBody[RemoveSchedule])
      .out(
        oneOf[SchedulerCommandResult](
          oneOfVariant[ScheduleRemoved](
            statusCode(StatusCode.Ok)
              .and(jsonBody[ScheduleRemoved].description("Schedule removed"))
          ),
          oneOfVariant[ScheduleNotFound.type](
            statusCode(StatusCode.NotFound)
              .and(jsonBody[ScheduleNotFound.type].description("Schedule not found"))
          ),
          oneOfVariant[SchedulerNotFound.type](
            statusCode(StatusCode.NotFound)
              .and(jsonBody[SchedulerNotFound.type].description("Scheduler not found"))
          )
        )
      )
      .serverLogic { principal => cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd).map { r =>
          Right((principal._1._1, principal._1._2, r))
        }
      }

  val listSchedulesEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Unit,
    UnauthorizedError.type,
    (
      Seq[Option[String]],
      Option[CookieValueWithMeta],
      Either[SchedulerNotFound.type, Seq[ScheduleView]]
    ),
    Any,
    Future
  ] =
    rootSchedulesEndpoint.get
      .out(
        oneOf[Either[SchedulerNotFound.type, Seq[ScheduleView]]](
          oneOfVariantValueMatcher[Right[SchedulerNotFound.type, Seq[ScheduleView]]](
            statusCode(StatusCode.Ok)
              .and(
                jsonBody[Right[SchedulerNotFound.type, Seq[ScheduleView]]]
                  .description("Schedules loaded")
              )
          ) { case Right(_) =>
            true
          },
          oneOfVariantValueMatcher[Left[SchedulerNotFound.type, Seq[ScheduleView]]](
            statusCode(StatusCode.NotFound)
              .and(
                jsonBody[Left[SchedulerNotFound.type, Seq[ScheduleView]]]
                  .description("Scheduler not found")
              )
          ) { case Left(_) =>
            true
          }
        )
      )
      .serverLogic { principal => _ =>
        loadScheduler().map {
          case Some(scheduler) =>
            Right(
              (principal._1._1, principal._1._2, Right(scheduler.schedules.map(_.view)))
            )
          case _ => Right((principal._1._1, principal._1._2, Left(SchedulerNotFound)))
        }
      }

  val rootCronTabsEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Unit,
    UnauthorizedError.type,
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
    UnauthorizedError.type,
    (Seq[Option[String]], Option[CookieValueWithMeta], SchedulerCommandResult),
    Any,
    Future
  ] =
    rootCronTabsEndpoint.post
      .in(jsonBody[CronTab])
      .out(
        oneOf[SchedulerCommandResult](
          oneOfVariant[CronTabAdded](
            statusCode(StatusCode.Ok)
              .and(jsonBody[CronTabAdded].description("Cron tab added"))
          ),
          oneOfVariant[SchedulerNotFound.type](
            statusCode(StatusCode.NotFound)
              .and(jsonBody[SchedulerNotFound.type].description("Scheduler not found"))
          )
        )
      )
      .serverLogic { principal => cronTab =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), AddCronTab(cronTab)).map { r =>
          Right((principal._1._1, principal._1._2, r))
        }
      }

  val removeCronTabEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    RemoveCronTab,
    UnauthorizedError.type,
    (Seq[Option[String]], Option[CookieValueWithMeta], SchedulerCommandResult),
    Any,
    Future
  ] =
    rootCronTabsEndpoint.delete
      .in(jsonBody[RemoveCronTab])
      .out(
        oneOf[SchedulerCommandResult](
          oneOfVariant[CronTabRemoved](
            statusCode(StatusCode.Ok)
              .and(jsonBody[CronTabRemoved].description("Cron tab removed"))
          ),
          oneOfVariant[CronTabNotFound.type](
            statusCode(StatusCode.NotFound)
              .and(jsonBody[CronTabNotFound.type].description("Cron tab not found"))
          ),
          oneOfVariant[SchedulerNotFound.type](
            statusCode(StatusCode.NotFound)
              .and(jsonBody[SchedulerNotFound.type].description("Scheduler not found"))
          )
        )
      )
      .serverLogic { principal => cmd =>
        run(SchedulerSettings.SchedulerConfig.id.getOrElse("*"), cmd).map { r =>
          Right((principal._1._1, principal._1._2, r))
        }
      }

  val listCronTabsEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Unit,
    UnauthorizedError.type,
    (
      Seq[Option[String]],
      Option[CookieValueWithMeta],
      Either[SchedulerNotFound.type, Seq[CronTab]]
    ),
    Any,
    Future
  ] =
    rootCronTabsEndpoint.get
      .out(
        oneOf[Either[SchedulerNotFound.type, Seq[CronTab]]](
          oneOfVariantValueMatcher[Right[SchedulerNotFound.type, Seq[CronTab]]](
            statusCode(StatusCode.Ok)
              .and(
                jsonBody[Right[SchedulerNotFound.type, Seq[CronTab]]]
                  .description("Cron tabs loaded")
              )
          ) { case Right(_) =>
            true
          },
          oneOfVariantValueMatcher[Left[SchedulerNotFound.type, Seq[CronTab]]](
            statusCode(StatusCode.NotFound)
              .and(
                jsonBody[Left[SchedulerNotFound.type, Seq[CronTab]]]
                  .description("Scheduler not found")
              )
          ) { case Left(_) =>
            true
          }
        )
      )
      .serverLogic { principal => _ =>
        loadScheduler().map {
          case Some(scheduler) =>
            Right((principal._1._1, principal._1._2, Right(scheduler.cronTabs)))
          case _ => Right((principal._1._1, principal._1._2, Left(SchedulerNotFound)))
        }
      }

  val loadSchedulerEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    List[String],
    UnauthorizedError.type,
    (Seq[Option[String]], Option[CookieValueWithMeta], Either[SchedulerNotFound.type, Scheduler]),
    Any,
    Future
  ] =
    rootEndpoint.get
      .in(paths)
      .out(
        oneOf[Either[SchedulerNotFound.type, Scheduler]](
          oneOfVariantValueMatcher[Right[SchedulerNotFound.type, Scheduler]](
            statusCode(StatusCode.Ok)
              .and(
                jsonBody[Right[SchedulerNotFound.type, Scheduler]].description("Scheduler loaded")
              )
          ) { case Right(_) =>
            true
          },
          oneOfVariantValueMatcher[Left[SchedulerNotFound.type, Scheduler]](
            statusCode(StatusCode.NotFound)
              .and(
                jsonBody[Left[SchedulerNotFound.type, Scheduler]].description("Scheduler not found")
              )
          ) { case Left(_) =>
            true
          }
        )
      )
      .serverLogic { principal => paths =>
        val id =
          paths match {
            case Nil => None
            case _   => Some(paths.head)
          }
        loadScheduler(id).map {
          case Some(scheduler) =>
            Right((principal._1._1, principal._1._2, Right(scheduler)))
          case _ => Right((principal._1._1, principal._1._2, Left(SchedulerNotFound)))
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
