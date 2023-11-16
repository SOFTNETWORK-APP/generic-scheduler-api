package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerRoutesTestKit
import app.softnetwork.session.scalatest._
import app.softnetwork.session.service._

package Directives {
  package OneOff {
    package Cookie {

      class SchedulerRoutesWithOneOffCookieBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit
          with OneOffCookieSessionServiceTestKit
          with BasicSessionMaterials

      class SchedulerRoutesWithOneOffCookieJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit
          with OneOffCookieSessionServiceTestKit
          with JwtSessionMaterials

    }
    package Header {
      class SchedulerRoutesWithOneOffHeaderBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit
          with OneOffHeaderSessionServiceTestKit
          with BasicSessionMaterials

      class SchedulerRoutesWithOneOffHeaderJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit
          with OneOffHeaderSessionServiceTestKit
          with JwtSessionMaterials

    }
  }

  package Refreshable {
    package Cookie {

      class SchedulerRoutesWithRefreshableCookieBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit
          with RefreshableCookieSessionServiceTestKit
          with BasicSessionMaterials

      class SchedulerRoutesWithRefreshableCookieJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit
          with RefreshableCookieSessionServiceTestKit
          with JwtSessionMaterials

    }

    package Header {
      class SchedulerRoutesWithRefreshableHeaderBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit
          with RefreshableHeaderSessionServiceTestKit
          with BasicSessionMaterials

      class SchedulerRoutesWithRefreshableHeaderJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit
          with RefreshableHeaderSessionServiceTestKit
          with JwtSessionMaterials

    }

  }
}
