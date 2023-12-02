package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{ApiRoute, SwaggerEndpoint}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerRoutes
import app.softnetwork.scheduler.service.{SchedulerService, SchedulerServiceEndpoints}
import app.softnetwork.session.config.Settings
import app.softnetwork.session.handlers.SessionRefreshTokenDao
import app.softnetwork.session.model.SessionDataCompanion
import app.softnetwork.session.service.BasicSessionMaterials
import com.softwaremill.session.RefreshTokenStorage
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait SchedulerRoutesApi extends SchedulerApi with SchedulerRoutes[Session] { _: SchemaProvider =>

  override def schedulerService: ActorSystem[_] => SchedulerService[Session] = sys =>
    new SchedulerService[Session] with BasicSessionMaterials[Session] {
      override implicit def system: ActorSystem[_] = sys
      override lazy val ec: ExecutionContext = sys.executionContext
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
      override protected def sessionType: Session.SessionType =
        Settings.Session.SessionContinuityAndTransport
      override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
        SessionRefreshTokenDao(system)
      override implicit def companion: SessionDataCompanion[Session] = Session
    }

  private def schedulerSwagger: ActorSystem[_] => SwaggerEndpoint =
    sys =>
      new SchedulerServiceEndpoints[Session]
        with SwaggerEndpoint
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

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system => super.apiRoutes(system) :+ schedulerSwagger(system)
}
