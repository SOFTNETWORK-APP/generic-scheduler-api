package app.softnetwork.scheduler.api

import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerEndpoints
import app.softnetwork.session.CsrfCheck

trait SchedulerEndpointsApi extends SchedulerApi with SchedulerEndpoints {
  _: SchemaProvider with CsrfCheck =>
}
