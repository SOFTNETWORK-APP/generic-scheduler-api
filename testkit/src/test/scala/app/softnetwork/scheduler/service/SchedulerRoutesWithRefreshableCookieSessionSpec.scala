package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerRoutesTestKit
import app.softnetwork.session.scalatest.RefreshableCookieSessionServiceTestKit

class SchedulerRoutesWithRefreshableCookieSessionSpec
    extends SchedulerServiceSpec
    with SchedulerRoutesTestKit
    with RefreshableCookieSessionServiceTestKit
