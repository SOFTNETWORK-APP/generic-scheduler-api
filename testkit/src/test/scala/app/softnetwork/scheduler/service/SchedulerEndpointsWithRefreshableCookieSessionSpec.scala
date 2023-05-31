package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.scalatest.RefreshableCookieSessionEndpointsTestKit
import com.softwaremill.session.CsrfCheckHeader

class SchedulerEndpointsWithRefreshableCookieSessionSpec
    extends SchedulerServiceSpec
    with SchedulerEndpointsTestKit
    with RefreshableCookieSessionEndpointsTestKit
    with CsrfCheckHeader
