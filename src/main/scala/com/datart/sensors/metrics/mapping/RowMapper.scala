package com.datart.sensors.metrics.mapping

import cats.MonadError
import com.datart.sensors.metrics.model.input.Row._
import com.datart.sensors.metrics.model.input._

trait RowMapper[F[_]] {
  def toRow(rawRow: String): F[Row]
}

class RowMapperImpl[F[_]](implicit monadError: MonadError[F, Throwable]) extends RowMapper[F] {

  private val failedMeasureR = ".*?(\\S+).*?,.*?(?:NaN).*?".r
  private val measureR       = ".*?(\\S+).*?,.*?(-?\\d+).*?".r

  private val minHumidity        = 0
  private val maxHumidity        = 100
  private val validHumidityRange = Range(minHumidity, maxHumidity)

  override def toRow(rawRow: String): F[Row] = monadError.pure {
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
