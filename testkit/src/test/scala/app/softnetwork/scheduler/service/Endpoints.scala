package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerEndpointsTestKit
import app.softnetwork.session.scalatest._
import app.softnetwork.session.service._
import org.softnetwork.session.model.Session

package Endpoints {
  package OneOff {
    package Cookie {

      class SchedulerEndpointsWithOneOffCookieBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with OneOffCookieSessionEndpointsTestKit[Session]
          with BasicSessionMaterials[Session]

      class SchedulerEndpointsWithOneOffCookieJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with OneOffCookieSessionEndpointsTestKit[Session]
          with JwtSessionMaterials[Session]

    }

    package Header {

      class SchedulerEndpointsWithOneOffHeaderBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with OneOffHeaderSessionEndpointsTestKit[Session]
          with BasicSessionMaterials[Session]

      class SchedulerEndpointsWithOneOffHeaderJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with OneOffHeaderSessionEndpointsTestKit[Session]
          with JwtSessionMaterials[Session]

    }

  }

  package Refreshable {
    package Cookie {

      class SchedulerEndpointsWithRefreshableCookieBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with RefreshableCookieSessionEndpointsTestKit[Session]
          with BasicSessionMaterials[Session]

      class SchedulerEndpointsWithRefreshableCookieJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with RefreshableCookieSessionEndpointsTestKit[Session]
          with JwtSessionMaterials[Session]

    }

    package Header {
      class SchedulerEndpointsWithRefreshableHeaderBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with RefreshableHeaderSessionEndpointsTestKit[Session]
          with BasicSessionMaterials[Session]

      class SchedulerEndpointsWithRefreshableHeaderJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerEndpointsTestKit
          with RefreshableHeaderSessionEndpointsTestKit[Session]
          with JwtSessionMaterials[Session]

    }

  }

}
