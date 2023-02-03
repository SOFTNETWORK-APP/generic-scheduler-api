package app.softnetwork.scheduler.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import app.softnetwork.persistence.jdbc.query.{JdbcJournalProvider, JdbcSchema, JdbcSchemaProvider}
import app.softnetwork.scheduler.handlers.SchedulerHandler
import app.softnetwork.scheduler.launch.SchedulerApplication
import app.softnetwork.scheduler.persistence.query.Entity2SchedulerProcessorStream

import scala.concurrent.Future

trait SchedulerApi extends SchedulerApplication with JdbcSchemaProvider {

  override def entity2SchedulerProcessorStream: ActorSystem[_] => Entity2SchedulerProcessorStream =
    sys =>
      new Entity2SchedulerProcessorStream()
        with SchedulerHandler
        with JdbcJournalProvider
        with JdbcSchemaProvider {
        override lazy val schemaType: JdbcSchema.SchemaType = SchedulerApi.this.schemaType

        override implicit def system: ActorSystem[_] = sys
      }

  override def grpcServices
    : ActorSystem[_] => Seq[PartialFunction[HttpRequest, Future[HttpResponse]]] = system =>
    Seq(SchedulerServiceApiHandler.partial(schedulerServer(system))(system))

}
