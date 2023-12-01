package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import app.softnetwork.persistence.jdbc.query.{JdbcJournalProvider, JdbcOffsetProvider}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.handlers.SchedulerHandler
import app.softnetwork.scheduler.launch.SchedulerApplication
import app.softnetwork.scheduler.persistence.query.Entity2SchedulerProcessorStream
import app.softnetwork.session.config.Settings
import app.softnetwork.session.model.{SessionDataCompanion, SessionManagers}
import com.softwaremill.session.{SessionConfig, SessionManager}
import com.typesafe.config.Config
import org.softnetwork.session.model.Session

import scala.concurrent.Future

trait SchedulerApi extends SchedulerApplication { _: SchemaProvider =>

  override def entity2SchedulerProcessorStream: ActorSystem[_] => Entity2SchedulerProcessorStream =
    sys =>
      new Entity2SchedulerProcessorStream()
        with SchedulerHandler
        with JdbcJournalProvider
        with JdbcOffsetProvider {

        override def config: Config = SchedulerApi.this.config

        override implicit def system: ActorSystem[_] = sys
      }

  override def grpcServices
    : ActorSystem[_] => Seq[PartialFunction[HttpRequest, Future[HttpResponse]]] = system =>
    Seq(SchedulerServiceApiHandler.partial(schedulerServer(system))(system))

  override protected def sessionType: Session.SessionType =
    Settings.Session.SessionContinuityAndTransport

  override protected def manager(implicit
    sessionConfig: SessionConfig,
    companion: SessionDataCompanion[Session]
  ): SessionManager[Session] =
    SessionManagers.basic

}
