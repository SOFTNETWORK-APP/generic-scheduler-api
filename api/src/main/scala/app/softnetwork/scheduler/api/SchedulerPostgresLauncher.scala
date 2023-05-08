package app.softnetwork.scheduler.api

import app.softnetwork.persistence.jdbc.schema.JdbcSchemaTypes
import app.softnetwork.persistence.schema.SchemaType
import org.slf4j.{Logger, LoggerFactory}

object SchedulerPostgresLauncher extends SchedulerApi {

  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  override val schemaType: SchemaType = JdbcSchemaTypes.Postgres
}
