package com.datart.sensors.metrics.mapping

import cats.implicits.catsStdInstancesForTry
import com.datart.sensors.metrics.model.input.Row._
import com.datart.sensors.metrics.model.input._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class RowMapperImplSpec extends AnyWordSpec with Matchers {

  private val testedImplementation: RowMapperImpl[Try] = new RowMapperImpl()

  "A RowMapperImpl" can {

    "map raw row to domain model" should {

      "map measurement row correctly" in {
        val sensorId               = "s1"
        val humidity               = 10
        val expectedMeasurementRow = SuccessfulMeasurement(Sensor(sensorId), Humidity(humidity))

        testedImplementation.toRow(s"$sensorId,${humidity.toString}") shouldBe Success(expectedMeasurementRow)
        testedImplementation.toRow(s" $sensorId , ${humidity.toString} ") shouldBe Success(expectedMeasurementRow)
        testedImplementation.toRow(s" $sensorId , {ignored_chars}${humidity.toString}{ignored_chars}") shouldBe Success(
          expectedMeasurementRow)
      }

      "map failed measurement row correctly" in {
        val sensorId               = "s1"
        val humidity               = "NaN"
        val expectedMeasurementRow = FailedMeasurement(Sensor(sensorId))

        testedImplementation.toRow(s"$sensorId,$humidity") shouldBe Success(expectedMeasurementRow)
        testedImplementation.toRow(s" $sensorId , $humidity ") shouldBe Success(expectedMeasurementRow)
        testedImplementation.toRow(s" $sensorId , {ignored_chars}$humidity{ignored_chars}") shouldBe Success(
          expectedMeasurementRow)
      }

      "map ignored row correctly" in {
        testedImplementation.toRow("sensor-id,humidity") shouldBe Success(Ignored)
        testedImplementation.toRow("") shouldBe Success(Ignored)
        testedImplementation.toRow(" ") shouldBe Success(Ignored)
        testedImplementation.toRow(" ,") shouldBe Success(Ignored)
        testedImplementation.toRow(", ") shouldBe Success(Ignored)
        testedImplementation.toRow(" , ") shouldBe Success(Ignored)
        testedImplementation.toRow("s1") shouldBe Success(Ignored)
        testedImplementation.toRow("0") shouldBe Success(Ignored)
        testedImplementation.toRow("s1,") shouldBe Success(Ignored)
        testedImplementation.toRow("s1, ") shouldBe Success(Ignored)
        testedImplementation.toRow("s1,a") shouldBe Success(Ignored)
        testedImplementation.toRow("s1,-1") shouldBe Success(Ignored)
        testedImplementation.toRow("s1,101") shouldBe Success(Ignored)
        testedImplementation.toRow(",0") shouldBe Success(Ignored)
        testedImplementation.toRow(" ,0") shouldBe Success(Ignored)
        testedImplementation.toRow(",-1") shouldBe Success(Ignored)
        testedImplementation.toRow(" ,-1") shouldBe Success(Ignored)
        testedImplementation.toRow(",101") shouldBe Success(Ignored)
        testedImplementation.toRow(" ,101") shouldBe Success(Ignored)
        testedImplementation.toRow("s1,") shouldBe Success(Ignored)
      }
    }
  }
}
