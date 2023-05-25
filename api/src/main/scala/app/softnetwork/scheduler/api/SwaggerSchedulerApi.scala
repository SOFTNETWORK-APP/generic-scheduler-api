package app.softnetwork.scheduler.api

import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerEndpoints

trait SwaggerSchedulerApi extends SchedulerApi with SchedulerEndpoints { _: SchemaProvider => }
