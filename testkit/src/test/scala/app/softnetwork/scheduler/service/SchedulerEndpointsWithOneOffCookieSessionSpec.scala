package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.scalatest.OneOffCookieSessionTestKit
import com.softwaremill.session.CsrfCheckHeader

class SchedulerEndpointsWithOneOffCookieSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with OneOffCookieSessionTestKit
    with CsrfCheckHeader
