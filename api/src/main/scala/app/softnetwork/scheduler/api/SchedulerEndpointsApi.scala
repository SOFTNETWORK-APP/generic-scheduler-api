package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{Endpoint, SwaggerApiEndpoint}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerEndpoints
import app.softnetwork.scheduler.service.SchedulerServiceEndpoints
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.config.Settings
import app.softnetwork.session.handlers.SessionRefreshTokenDao
import app.softnetwork.session.model.SessionDataCompanion
import app.softnetwork.session.service.BasicSessionMaterials
import com.softwaremill.session.RefreshTokenStorage
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait SchedulerEndpointsApi extends SchedulerApi with SchedulerEndpoints[Session] {
  self: SchemaProvider with CsrfCheck =>

  override def schedulerEndpoints: ActorSystem[_] => SchedulerServiceEndpoints[Session] = sys =>
    new SchedulerServiceEndpoints[Session]
      with SwaggerApiEndpoint
      with BasicSessionMaterials[Session] {
      override implicit def system: ActorSystem[_] = sys
      override lazy val ec: ExecutionContext = sys.executionContext
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
      override protected def sessionType: Session.SessionType =
        Settings.Session.SessionContinuityAndTransport
      override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
        SessionRefreshTokenDao(sys)
      override implicit def companion: SessionDataCompanion[Session] = Session
      override val applicationVersion: String = systemVersion()
    }

  override def endpoints: ActorSystem[_] => List[Endpoint] =
    system => super.endpoints(system)
}
