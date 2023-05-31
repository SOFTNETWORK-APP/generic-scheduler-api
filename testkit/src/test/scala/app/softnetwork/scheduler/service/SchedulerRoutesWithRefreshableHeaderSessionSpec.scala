package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerRoutesTestKit
import app.softnetwork.session.scalatest.RefreshableHeaderSessionServiceTestKit

class SchedulerRoutesWithRefreshableHeaderSessionSpec
    extends SchedulerServiceSpec
    with SchedulerRoutesTestKit
    with RefreshableHeaderSessionServiceTestKit
