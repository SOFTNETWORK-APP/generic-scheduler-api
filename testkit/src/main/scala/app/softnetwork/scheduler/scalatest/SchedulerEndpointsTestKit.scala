package app.softnetwork.scheduler.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.Endpoint
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerEndpoints
import app.softnetwork.scheduler.service.SchedulerServiceEndpoints
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.model.{SessionData, SessionDataCompanion, SessionDataDecorator}
import app.softnetwork.session.scalatest.{SessionEndpointsRoutes, SessionTestKit}
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.{RefreshTokenStorage, SessionConfig, SessionManager}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait SchedulerEndpointsTestKit[SD <: SessionData with SessionDataDecorator[SD]]
    extends SchedulerEndpoints[SD]
    with SessionEndpointsRoutes[SD] {
  self: SessionTestKit[SD]
    with SchedulerTestKit
    with SchemaProvider
    with SessionMaterials[SD]
    with CsrfCheck =>

  override def schedulerEndpoints: ActorSystem[_] => SchedulerServiceEndpoints[SD] = sys =>
    new SchedulerServiceEndpoints[SD] with SessionMaterials[SD] {
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

  override def endpoints: ActorSystem[_] => List[Endpoint] =
    system =>
      List(
        sessionServiceEndpoints(system),
        schedulerEndpoints(system)
//        schedulerSwagger(system)
      )

}
