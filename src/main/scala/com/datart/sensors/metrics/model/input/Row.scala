package com.datart.sensors.metrics.model.input

sealed abstract class Row

object Row {
  final case class SuccessfulMeasurement(sensor: Sensor, humidity: Humidity) extends Row
  final case class FailedMeasurement(sensor: Sensor)                         extends Row
  final case object Ignored                                                  extends Row
}
