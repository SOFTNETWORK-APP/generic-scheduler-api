package app.softnetwork.scheduler.service

import app.softnetwork.scheduler.scalatest.SchedulerRoutesTestKit
import app.softnetwork.session.scalatest._
import app.softnetwork.session.service._
import org.softnetwork.session.model.Session

package Directives {
  package OneOff {
    package Cookie {

      class SchedulerRoutesWithOneOffCookieBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit[Session]
          with OneOffCookieSessionServiceTestKit[Session]
          with BasicSessionMaterials[Session]

      class SchedulerRoutesWithOneOffCookieJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit[Session]
          with OneOffCookieSessionServiceTestKit[Session]
          with JwtSessionMaterials[Session]

    }
    package Header {

      class SchedulerRoutesWithOneOffHeaderBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit[Session]
          with OneOffHeaderSessionServiceTestKit[Session]
          with BasicSessionMaterials[Session]

      class SchedulerRoutesWithOneOffHeaderJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit[Session]
          with OneOffHeaderSessionServiceTestKit[Session]
          with JwtSessionMaterials[Session]

    }
  }

  package Refreshable {
    package Cookie {

      class SchedulerRoutesWithRefreshableCookieBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit[Session]
          with RefreshableCookieSessionServiceTestKit[Session]
          with BasicSessionMaterials[Session]

      class SchedulerRoutesWithRefreshableCookieJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit[Session]
          with RefreshableCookieSessionServiceTestKit[Session]
          with JwtSessionMaterials[Session]

    }

    package Header {
      class SchedulerRoutesWithRefreshableHeaderBasicSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit[Session]
          with RefreshableHeaderSessionServiceTestKit[Session]
          with BasicSessionMaterials[Session]

      class SchedulerRoutesWithRefreshableHeaderJwtSessionSpec
          extends SchedulerServiceSpec
          with SchedulerRoutesTestKit[Session]
          with RefreshableHeaderSessionServiceTestKit[Session]
          with JwtSessionMaterials[Session]

    }

  }
}
