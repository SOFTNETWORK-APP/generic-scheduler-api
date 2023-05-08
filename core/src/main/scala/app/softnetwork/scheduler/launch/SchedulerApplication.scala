package app.softnetwork.scheduler.launch

import app.softnetwork.api.server.launch.Application
import app.softnetwork.persistence.schema.SchemaProvider

trait SchedulerApplication extends Application with SchedulerRoutes { _: SchemaProvider => }
