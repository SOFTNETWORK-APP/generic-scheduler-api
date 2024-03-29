package app.softnetwork.scheduler.config

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import configs.Configs

/** Created by smanciot on 16/12/2020.
  */
object SchedulerSettings extends StrictLogging {

  lazy val config: Config =
    ConfigFactory.load().withFallback(ConfigFactory.load("softnetwork-scheduler.conf"))

  lazy val SchedulerConfig: SchedulerConfig =
    Configs[SchedulerConfig].get(config, "softnetwork.scheduler").toEither match {
      case Left(configError) =>
        logger.error(s"Something went wrong with the provided arguments $configError")
        throw configError.configException
      case Right(schedulerConfig) => schedulerConfig
    }

  val SchedulerPath: String = config.getString("softnetwork.scheduler.path")

  def tag(persistenceId: String): String = s"scheduler-to-$persistenceId"
}

case class SchedulerConfig(
  id: Option[String],
  resetScheduler: ResetScheduler,
  eventStreams: SchedulerEventStreams,
  akkaNodeRole: String
)

case class ResetScheduler(initialDelay: Int, delay: Int)

case class SchedulerEventStreams(entityToSchedulerTag: String)
