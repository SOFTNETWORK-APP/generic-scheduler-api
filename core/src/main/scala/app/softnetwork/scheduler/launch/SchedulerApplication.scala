package app.softnetwork.scheduler.launch

import app.softnetwork.api.server.launch.Application

trait SchedulerApplication extends Application with SchedulerRoutes
