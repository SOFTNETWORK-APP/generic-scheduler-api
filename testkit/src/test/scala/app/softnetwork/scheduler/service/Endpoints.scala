package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.scalatest._
import app.softnetwork.session.service._

package Endpoints {
  package OneOff {
    package Cookie {

      class SchedulerEndpointsWithOneOffCookieBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with OneOffCookieSessionEndpointsTestKit
          with BasicSessionMaterials

      class SchedulerEndpointsWithOneOffCookieJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with OneOffCookieSessionEndpointsTestKit
          with JwtSessionMaterials

    }

    package Header {

      class SchedulerEndpointsWithOneOffHeaderBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with OneOffHeaderSessionEndpointsTestKit
          with BasicSessionMaterials

      class SchedulerEndpointsWithOneOffHeaderJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with OneOffHeaderSessionEndpointsTestKit
          with JwtSessionMaterials

    }

  }

  package Refreshable {
    package Cookie {

      class SchedulerEndpointsWithRefreshableCookieBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with RefreshableCookieSessionEndpointsTestKit
          with BasicSessionMaterials

      class SchedulerEndpointsWithRefreshableCookieJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with RefreshableCookieSessionEndpointsTestKit
          with JwtSessionMaterials

    }

    package Header {
      class SchedulerEndpointsWithRefreshableHeaderBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with RefreshableHeaderSessionEndpointsTestKit
          with BasicSessionMaterials

      class SchedulerEndpointsWithRefreshableHeaderJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with RefreshableHeaderSessionEndpointsTestKit
          with JwtSessionMaterials

    }

  }

}
