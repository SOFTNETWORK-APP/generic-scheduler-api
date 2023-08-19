package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.jdbc.schema.{JdbcSchemaProvider, JdbcSchemaTypes}
import app.softnetwork.persistence.schema.SchemaType
import app.softnetwork.session.service.SessionEndpoints
import app.softnetwork.session.CsrfCheckHeader
import org.slf4j.{Logger, LoggerFactory}

object SchedulerEndpointsWithOneOffHeaderPostgresLauncher
    extends SchedulerEndpointsApi
    with JdbcSchemaProvider
    with CsrfCheckHeader {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  def schemaType: SchemaType = JdbcSchemaTypes.Postgres

  override def sessionEndpoints: ActorSystem[_] => SessionEndpoints = system =>
    SessionEndpoints.oneOffHeader(system, checkHeaderAndForm)
}
