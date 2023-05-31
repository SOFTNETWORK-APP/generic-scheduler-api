package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.scalatest.RefreshableHeaderSessionEndpointsTestKit
import com.softwaremill.session.CsrfCheckHeader

class SchedulerEndpointsWithRefreshableHeaderSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with RefreshableHeaderSessionEndpointsTestKit
    with CsrfCheckHeader
