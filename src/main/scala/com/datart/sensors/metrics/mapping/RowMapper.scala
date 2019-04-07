package com.datart.sensors.metrics.mapping

import com.datart.sensors.metrics.model.Row._
import com.datart.sensors.metrics.model._

trait RowMapper {
  def toRow(rawRow: String): Row
}

class RowMapperImpl extends RowMapper {

  private val failedMeasureR = ".*?(\\S+).*?,.*?(?:NaN).*?".r
  private val measureR       = ".*?(\\S+).*?,.*?(-?\\d+).*?".r

  private val minHumidity        = 0
  private val maxHumidity        = 100
  private val validHumidityRange = Range(minHumidity, maxHumidity)

  override def toRow(rawRow: String): Row = {
    rawRow match {
      case measureR(sensorId, humidity) =>
        val h = humidity.toInt
        if (validHumidityRange.contains(h)) {
          SuccessfulMeasurement(Sensor(sensorId.trim), Humidity(h))
        } else {
          Ignored
        }
      case failedMeasureR(sensorId) =>
        FailedMeasurement(Sensor(sensorId.trim))
      case _ =>
        Ignored
    }
  }
}
