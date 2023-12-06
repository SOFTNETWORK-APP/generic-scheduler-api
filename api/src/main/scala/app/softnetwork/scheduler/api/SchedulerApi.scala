package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.jdbc.query.{JdbcJournalProvider, JdbcOffsetProvider}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.scheduler.handlers.SchedulerHandler
import app.softnetwork.scheduler.launch.SchedulerApplication
import app.softnetwork.scheduler.persistence.query.Entity2SchedulerProcessorStream
import app.softnetwork.session.config.Settings
import com.typesafe.config.Config
import org.softnetwork.session.model.Session

trait SchedulerApi extends SchedulerApplication { _: SchemaProvider =>

  override def entity2SchedulerProcessorStream: ActorSystem[_] => Entity2SchedulerProcessorStream =
    sys =>
      new Entity2SchedulerProcessorStream()
        with SchedulerHandler
        with JdbcJournalProvider
        with JdbcOffsetProvider {

        override def config: Config = SchedulerApi.this.config

        override implicit def system: ActorSystem[_] = sys
      }

  override protected def sessionType: Session.SessionType =
    Settings.Session.SessionContinuityAndTransport

}
