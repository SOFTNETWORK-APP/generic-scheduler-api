package app.softnetwork.scheduler.model

case class ScheduleView(
  persistenceId: String,
  entityId: String,
  key: String,
  delay: Long,
  repeatedly: Option[Boolean] = None,
  scheduledDate: Option[java.util.Date] = None,
  lastTriggered: Option[java.util.Date] = None,
  triggerable: Boolean,
  removable: Boolean
)

object ScheduleView {
  def apply(schedule: Schedule): ScheduleView = {
    import schedule._
    ScheduleView(
      persistenceId,
      entityId,
      key,
      delay,
      repeatedly,
      scheduledDate,
      lastTriggered,
      triggerable,
      removable
    )
  }
}
