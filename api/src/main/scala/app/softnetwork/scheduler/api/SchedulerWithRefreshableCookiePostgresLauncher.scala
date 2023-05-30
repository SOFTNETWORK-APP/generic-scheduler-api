package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.jdbc.schema.{JdbcSchemaProvider, JdbcSchemaTypes}
import app.softnetwork.persistence.schema.SchemaType
import app.softnetwork.session.service.SessionEndpoints
import com.softwaremill.session.CsrfCheckHeaderAndForm
import org.slf4j.{Logger, LoggerFactory}

object SchedulerWithRefreshableCookiePostgresLauncher
    extends SchedulerEndpointsApi
    with JdbcSchemaProvider
    with CsrfCheckHeaderAndForm {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  def schemaType: SchemaType = JdbcSchemaTypes.Postgres

  override def sessionEndpoints: ActorSystem[_] => SessionEndpoints = system =>
    SessionEndpoints.refreshableCookie(system, checkHeaderAndForm)
}
