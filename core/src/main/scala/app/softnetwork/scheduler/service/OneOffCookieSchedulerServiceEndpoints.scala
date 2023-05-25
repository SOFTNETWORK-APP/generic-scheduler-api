package app.softnetwork.scheduler.service

import akka.actor.typed.ActorSystem
import app.softnetwork.session.service.OneOffCookieSessionEndpoints
import org.slf4j.{Logger, LoggerFactory}

trait OneOffCookieSchedulerServiceEndpoints
    extends SchedulerServiceEndpoints
    with OneOffCookieSessionEndpoints

object OneOffCookieSchedulerServiceEndpoints {
  def apply(_system: ActorSystem[_]): SchedulerServiceEndpoints =
    new OneOffCookieSchedulerServiceEndpoints {
      override implicit def system: ActorSystem[_] = _system
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
    }
}
