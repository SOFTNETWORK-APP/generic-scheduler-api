package app.softnetwork.scheduler.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.launch.PersistentEntity
import app.softnetwork.persistence.launch.PersistenceGuardian._
import app.softnetwork.persistence.query.EventProcessorStream
import app.softnetwork.scheduler.api.SchedulerServer
import app.softnetwork.scheduler.handlers.SchedulerDao
import app.softnetwork.scheduler.persistence.query.{
  Entity2SchedulerProcessorStream,
  Scheduler2EntityProcessorStream
}
import app.softnetwork.scheduler.persistence.typed.SchedulerBehavior
import app.softnetwork.session.launch.SessionGuardian
import org.slf4j.Logger

import scala.util.{Failure, Success, Try}

trait SchedulerGuardian extends SessionGuardian {

  def log: Logger

  def schedulerEntities: ActorSystem[_] => Seq[PersistentEntity[_, _, _, _]] = _ =>
    Seq(
      SchedulerBehavior
    )

  /** initialize all entities
    */
  override def entities: ActorSystem[_] => Seq[PersistentEntity[_, _, _, _]] = system =>
    sessionEntities(system) ++ schedulerEntities(system)

  def entity2SchedulerProcessorStream: ActorSystem[_] => Entity2SchedulerProcessorStream

  def scheduler2EntityProcessorStreams
    : ActorSystem[_] => Seq[Scheduler2EntityProcessorStream[_, _]] = _ => Seq.empty

  def schedulerEventProcessorStreams: ActorSystem[_] => Seq[EventProcessorStream[_]] = sys =>
    Seq(
      entity2SchedulerProcessorStream(sys)
    ) ++ scheduler2EntityProcessorStreams(sys)

  /** initialize all event processor streams
    */
  override def eventProcessorStreams: ActorSystem[_] => Seq[EventProcessorStream[_]] =
    schedulerEventProcessorStreams

  def initSchedulerSystem: ActorSystem[_] => Unit = system => {
    Try(SchedulerDao.start(system)) match {
      case Success(_) =>
      case Failure(f) => log.error(f.getMessage, f)
    }
  }

  /** initialize scheduler server
    */
  def schedulerServer: ActorSystem[_] => SchedulerServer = sys => SchedulerServer(sys)

  override def initSystem: ActorSystem[_] => Unit = initSchedulerSystem
}
