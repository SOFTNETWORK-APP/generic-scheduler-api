package app.softnetwork.scheduler.api

import app.softnetwork.persistence.jdbc.schema.{JdbcSchemaProvider, JdbcSchemaTypes}
import app.softnetwork.persistence.schema.SchemaType
import org.slf4j.{Logger, LoggerFactory}

object SwaggerSchedulerPostgresLauncher extends SwaggerSchedulerApi with JdbcSchemaProvider {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  def schemaType: SchemaType = JdbcSchemaTypes.Postgres
}
