package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.jdbc.schema.{JdbcSchemaProvider, JdbcSchemaTypes}
import app.softnetwork.persistence.schema.SchemaType
import app.softnetwork.session.service.SessionService
import org.slf4j.{Logger, LoggerFactory}

object SchedulerRoutesWithRefreshableCookiePostgresLauncher
    extends SchedulerRoutesApi
    with JdbcSchemaProvider {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  def schemaType: SchemaType = JdbcSchemaTypes.Postgres

  override def sessionService: ActorSystem[_] => SessionService = system =>
    SessionService.refreshableCookie(system)
}
