package app.softnetwork.scheduler.launch

import app.softnetwork.api.server.ApiRoutes
import app.softnetwork.api.server.launch.Application
import app.softnetwork.persistence.schema.SchemaProvider

trait SchedulerApplication extends Application with ApiRoutes with SchedulerGuardian {
  _: SchemaProvider =>
}
