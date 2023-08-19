package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.scalatest.RefreshableCookieSessionEndpointsTestKit

class SchedulerEndpointsWithRefreshableCookieSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with RefreshableCookieSessionEndpointsTestKit
    with CsrfCheckHeader
