package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.scalatest.OneOffCookieSessionEndpointsTestKit

class SchedulerEndpointsWithOneOffCookieSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with OneOffCookieSessionEndpointsTestKit
    with CsrfCheckHeader
