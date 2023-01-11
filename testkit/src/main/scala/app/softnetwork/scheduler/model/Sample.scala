package app.softnetwork.scheduler.model

import app.softnetwork.persistence.model.State

case class Sample(uuid: String, triggered: Int = 0) extends State
