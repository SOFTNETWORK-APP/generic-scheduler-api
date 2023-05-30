package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.scalatest.RefreshableHeaderSessionTestKit
import com.softwaremill.session.CsrfCheckHeader

class SchedulerEndpointsWithRefreshableHeaderSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with RefreshableHeaderSessionTestKit
    with CsrfCheckHeader
