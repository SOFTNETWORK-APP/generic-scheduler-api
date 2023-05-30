package app.softnetwork.scheduler.api

import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.launch.SchedulerEndpoints
import com.softwaremill.session.CsrfCheck

trait SchedulerEndpointsApi extends SchedulerApi with SchedulerEndpoints {
  _: SchemaProvider with CsrfCheck =>
}
