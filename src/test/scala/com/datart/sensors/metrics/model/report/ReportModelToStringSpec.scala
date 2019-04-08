package com.datart.sensors.metrics.model.report

import com.datart.sensors.metrics.model.report.SensorReport._
import org.scalatest._
import better.files._

class ReportModelToStringSpec extends WordSpec with Matchers {
  "An AliveSensorReport can be formatted correctly" in {
    AliveSensorReport("s1", 0L, 0L, 0L).toString shouldBe "s1,0,0,0"
  }

  "A DeadSensorReport can be formatted correctly" in {
    DeadSensorReport("s1").toString shouldBe "s1,NaN,NaN,NaN"
  }

  "A TotalReport can be formatted correctly" in {
    TotalReport(
      allFilesProcessed = 2L,
      allMeasurementsProcessed = 7L,
      failedMeasurements = 2L,
      aliveSensorsReports = Set(
        AliveSensorReport(
          sensorName = "s2",
          min = 78L,
          avg = 82L,
          max = 88L
        ),
        AliveSensorReport(
          sensorName = "s1",
          min = 10L,
          avg = 54L,
          max = 98L
        )
      ),
      deadSensorsReports = Set(
        DeadSensorReport(
          sensorName = "s3"
        ),
        DeadSensorReport(
          sensorName = "s4"
        )
      )
    ).toString shouldBe Resource.getAsString("expected_output.txt")
  }
}
