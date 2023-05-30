package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.scalatest.OneOffHeaderSessionTestKit
import com.softwaremill.session.CsrfCheckHeader

class SchedulerEndpointsWithOneOffHeaderSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with OneOffHeaderSessionTestKit
    with CsrfCheckHeader
