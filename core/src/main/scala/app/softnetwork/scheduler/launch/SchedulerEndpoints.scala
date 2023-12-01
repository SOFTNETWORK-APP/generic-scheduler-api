package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{ApiEndpoints, Endpoint}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.service.SchedulerServiceEndpoints
import app.softnetwork.session.handlers.SessionRefreshTokenDao
import app.softnetwork.session.model.SessionDataCompanion
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.{RefreshTokenStorage, SessionConfig, SessionManager}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait SchedulerEndpoints extends ApiEndpoints { self: SchedulerGuardian with SchemaProvider =>

  def schedulerEndpoints: ActorSystem[_] => SchedulerServiceEndpoints = sys =>
    new SchedulerServiceEndpoints with SessionMaterials[Session] {
      override implicit def system: ActorSystem[_] = sys
      override lazy val ec: ExecutionContext = sys.executionContext
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
      override protected def sessionType: Session.SessionType = self.sessionType
      override implicit def manager(implicit
        sessionConfig: SessionConfig,
        companion: SessionDataCompanion[Session]
      ): SessionManager[Session] = self.manager
      override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
        SessionRefreshTokenDao(system)
    }

  override def endpoints: ActorSystem[_] => List[Endpoint] = system =>
    List(schedulerEndpoints(system))
}
