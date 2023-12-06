package app.softnetwork.scheduler.launch

import app.softnetwork.api.server.ApiRoutes
import app.softnetwork.api.server.launch.Application
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.api.SchedulerGrpcServices

trait SchedulerApplication
    extends Application
    with ApiRoutes
    with SchedulerGuardian
    with SchedulerGrpcServices {
  _: SchemaProvider =>
}
