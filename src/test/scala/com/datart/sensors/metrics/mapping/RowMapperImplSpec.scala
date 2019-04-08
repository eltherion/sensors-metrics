package com.datart.sensors.metrics.mapping

import com.datart.sensors.metrics.model.input.Row._
import com.datart.sensors.metrics.model.input._
import org.scalatest._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class RowMapperImplSpec extends WordSpec with Matchers {

  private val testedImplementation = new RowMapperImpl()

  "A RowMapperImpl" can {

    "map raw row to domain model" should {

      "map measurement row correctly" in {
        val sensorId               = "s1"
        val humidity               = 10
        val expectedMeasurementRow = SuccessfulMeasurement(Sensor(sensorId), Humidity(humidity))

        testedImplementation.toRow(s"$sensorId,$humidity") shouldBe expectedMeasurementRow
        testedImplementation.toRow(s" $sensorId , $humidity ") shouldBe expectedMeasurementRow
        testedImplementation.toRow(s" $sensorId , {ignored_chars}$humidity{ignored_chars}") shouldBe expectedMeasurementRow
      }

      "map failed measurement row correctly" in {
        val sensorId               = "s1"
        val humidity               = "NaN"
        val expectedMeasurementRow = FailedMeasurement(Sensor(sensorId))

        testedImplementation.toRow(s"$sensorId,$humidity") shouldBe expectedMeasurementRow
        testedImplementation.toRow(s" $sensorId , $humidity ") shouldBe expectedMeasurementRow
        testedImplementation.toRow(s" $sensorId , {ignored_chars}$humidity{ignored_chars}") shouldBe expectedMeasurementRow
      }

      "map ignored row correctly" in {
        testedImplementation.toRow("sensor-id,humidity") shouldBe Ignored
        testedImplementation.toRow("") shouldBe Ignored
        testedImplementation.toRow(" ") shouldBe Ignored
        testedImplementation.toRow(" ,") shouldBe Ignored
        testedImplementation.toRow(", ") shouldBe Ignored
        testedImplementation.toRow(" , ") shouldBe Ignored
        testedImplementation.toRow("s1") shouldBe Ignored
        testedImplementation.toRow("0") shouldBe Ignored
        testedImplementation.toRow("s1,") shouldBe Ignored
        testedImplementation.toRow("s1, ") shouldBe Ignored
        testedImplementation.toRow("s1,a") shouldBe Ignored
        testedImplementation.toRow("s1,-1") shouldBe Ignored
        testedImplementation.toRow("s1,101") shouldBe Ignored
        testedImplementation.toRow(",0") shouldBe Ignored
        testedImplementation.toRow(" ,0") shouldBe Ignored
        testedImplementation.toRow(",-1") shouldBe Ignored
        testedImplementation.toRow(" ,-1") shouldBe Ignored
        testedImplementation.toRow(",101") shouldBe Ignored
        testedImplementation.toRow(" ,101") shouldBe Ignored
        testedImplementation.toRow("s1,") shouldBe Ignored
      }
    }
  }
}
