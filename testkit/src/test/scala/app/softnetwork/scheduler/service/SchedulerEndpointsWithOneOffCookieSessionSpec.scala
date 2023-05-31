package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.scalatest.OneOffCookieSessionEndpointsTestKit
import com.softwaremill.session.CsrfCheckHeader

class SchedulerEndpointsWithOneOffCookieSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with OneOffCookieSessionEndpointsTestKit
    with CsrfCheckHeader
