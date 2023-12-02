package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiRoute
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerRoutes
import app.softnetwork.scheduler.service.SchedulerService
import app.softnetwork.session.model.{SessionData, SessionDataCompanion, SessionDataDecorator}
import app.softnetwork.session.scalatest.{SessionServiceRoutes, SessionTestKit}
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.{RefreshTokenStorage, SessionConfig, SessionManager}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait SchedulerRoutesTestKit[SD <: SessionData with SessionDataDecorator[SD]]
    extends SchedulerRoutes[SD]
    with SessionServiceRoutes[SD] {
  self: SessionTestKit[SD] with SchedulerTestKit with SchemaProvider with SessionMaterials[SD] =>

  override def schedulerService: ActorSystem[_] => SchedulerService[SD] = sys =>
    new SchedulerService[SD] with SessionMaterials[SD] {
      override implicit def system: ActorSystem[_] = sys

      override lazy val ec: ExecutionContext = sys.executionContext
      lazy val log: Logger = LoggerFactory getLogger getClass.getName

      override protected def sessionType: Session.SessionType = self.sessionType

      override implicit def manager(implicit
        sessionConfig: SessionConfig,
        companion: SessionDataCompanion[SD]
      ): SessionManager[SD] = self.manager

      override implicit def refreshTokenStorage: RefreshTokenStorage[SD] =
        self.refreshTokenStorage

      override implicit def companion: SessionDataCompanion[SD] = self.companion
    }

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system =>
      List(
        sessionServiceRoute(system),
        schedulerService(system)
//        schedulerSwagger(system)
      )

}
