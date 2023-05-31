package app.softnetwork.scheduler.api

import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerRoutes

trait SchedulerRoutesApi extends SchedulerApi with SchedulerRoutes { _: SchemaProvider => }
