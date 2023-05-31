package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.scalatest.OneOffHeaderSessionEndpointsTestKit
import com.softwaremill.session.CsrfCheckHeader

class SchedulerEndpointsWithOneOffHeaderSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with OneOffHeaderSessionEndpointsTestKit
    with CsrfCheckHeader
