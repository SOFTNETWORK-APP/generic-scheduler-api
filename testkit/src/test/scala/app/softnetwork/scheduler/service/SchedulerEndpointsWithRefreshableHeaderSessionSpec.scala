package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.scalatest.RefreshableHeaderSessionEndpointsTestKit

class SchedulerEndpointsWithRefreshableHeaderSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with RefreshableHeaderSessionEndpointsTestKit
    with CsrfCheckHeader
