package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerRoutesTestKit
import app.softnetwork.session.scalatest.OneOffHeaderSessionServiceTestKit

class SchedulerRoutesWithOneOffHeaderSessionSpec
    extends SchedulerServiceSpec
    with SchedulerRoutesTestKit
    with OneOffHeaderSessionServiceTestKit
