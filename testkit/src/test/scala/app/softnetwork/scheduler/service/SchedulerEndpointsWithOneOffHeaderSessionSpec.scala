package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.scalatest.OneOffHeaderSessionEndpointsTestKit

class SchedulerEndpointsWithOneOffHeaderSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with OneOffHeaderSessionEndpointsTestKit
    with CsrfCheckHeader
