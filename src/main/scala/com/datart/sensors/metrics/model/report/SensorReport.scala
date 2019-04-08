package com.datart.sensors.metrics.model.report

sealed abstract class SensorReport

object SensorReport {
  final case class AliveSensorReport(sensorName: String, min: Long, avg: Long, max: Long) {
    override def toString: String = {
      s"$sensorName,$min,$avg,$max"
    }
  }

  final case class DeadSensorReport(sensorName: String) {
    override def toString: String = {
      s"$sensorName,NaN,NaN,NaN"
    }
  }
}
