package app.softnetwork.scheduler.service

import akka.actor.typed.ActorSystem
import app.softnetwork.session.service.RefreshableCookieSessionEndpoints
import org.slf4j.{Logger, LoggerFactory}

trait RefreshableCookieSchedulerServiceEndpoints
    extends SchedulerServiceEndpoints
    with RefreshableCookieSessionEndpoints

object RefreshableCookieSchedulerServiceEndpoints {
  def apply(_system: ActorSystem[_]): SchedulerServiceEndpoints =
    new RefreshableCookieSchedulerServiceEndpoints {
      override implicit def system: ActorSystem[_] = _system
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
    }
}
