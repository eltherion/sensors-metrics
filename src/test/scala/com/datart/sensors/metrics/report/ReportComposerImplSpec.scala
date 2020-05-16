package com.datart.sensors.metrics.report

import com.codahale.metrics.Metric
import com.datart.sensors.metrics.config.Config
import com.datart.sensors.metrics.model.report.SensorReport._
import com.datart.sensors.metrics.model.report.TotalReport
import monix.execution.Scheduler
import nl.grons.metrics4.scala.DefaultInstrumented
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pureconfig._
import pureconfig.generic.auto._

class ReportComposerImplSpec extends AsyncWordSpec with Matchers with DefaultInstrumented {

  private implicit val scheduler: Scheduler = Scheduler.global

  private val config = ConfigSource.default.loadOrThrow[Config]

  private val testedImplementation = new ReportComposerImpl(config)

  "A ReportComposerImpl" can {

    "can compose report from metrics" should {

      "return report for empty metrics set" in {
        val expectedReport = TotalReport(
          allFilesProcessed = 0L,
          allMeasurementsProcessed = 0L,
          failedMeasurements = 0L,
          aliveSensorsReports = Set.empty[AliveSensorReport],
          deadSensorsReports = Set.empty[DeadSensorReport]
        )

        testedImplementation
          .composeReport(Map.empty[String, Metric])
          .runToFuture
          .map(_ shouldBe expectedReport)
      }

      "return report for nonempty metrics set" in {
        val expectedReport = TotalReport(
          allFilesProcessed = 1L,
          allMeasurementsProcessed = 2L,
          failedMeasurements = 1L,
          aliveSensorsReports = Set(
            AliveSensorReport(
              sensorName = "s1",
              min = 0L,
              avg = 0L,
              max = 0L
            ),
            AliveSensorReport(
              sensorName = "s2",
              min = 1L,
              avg = 1L,
              max = 1L
            )
          ),
          deadSensorsReports = Set(
            DeadSensorReport(
              sensorName = "s3"
            )
          )
        )

        val allFilesMetric = metricRegistry.counter(config.allFilesMetricName)
        allFilesMetric.inc()

        val allMeasurementsMetric = metricRegistry.counter(config.allMeasurementsMetricName)
        allMeasurementsMetric.inc(2)

        val failedMeasurementsMetric = metricRegistry.counter(config.failedMeasurementsMetricName)
        failedMeasurementsMetric.inc()

        val aliveSensorHistogram1 = metricRegistry.histogram("s1")
        aliveSensorHistogram1.update(0L)

        val aliveSensorHistogram2 = metricRegistry.histogram("s2")
        aliveSensorHistogram2.update(1L)

        val deadSensorHistogram = metricRegistry.histogram(s"${config.failedSensorMetricNamePrefix}s3")

        val mockedMetrics = Map[String, Metric](
          config.allFilesMetricName                   -> allFilesMetric,
          config.allMeasurementsMetricName            -> allMeasurementsMetric,
          config.failedMeasurementsMetricName         -> failedMeasurementsMetric,
          "s1"                                        -> aliveSensorHistogram1,
          "s2"                                        -> aliveSensorHistogram2,
          s"${config.failedSensorMetricNamePrefix}s3" -> deadSensorHistogram
        )

        testedImplementation
          .composeReport(mockedMetrics)
          .runToFuture
          .map(_ shouldBe expectedReport)
      }
    }
  }
}
