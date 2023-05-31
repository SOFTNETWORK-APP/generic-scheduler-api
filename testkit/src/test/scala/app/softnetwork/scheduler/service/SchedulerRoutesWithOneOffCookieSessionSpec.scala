package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerRoutesTestKit
import app.softnetwork.session.scalatest.OneOffCookieSessionServiceTestKit

class SchedulerRoutesWithOneOffCookieSessionSpec
    extends SchedulerServiceSpec
    with SchedulerRoutesTestKit
    with OneOffCookieSessionServiceTestKit
