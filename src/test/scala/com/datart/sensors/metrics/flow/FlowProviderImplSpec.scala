package com.datart.sensors.metrics.flow

import java.io.{File => JFile}

import better.files._
import com.codahale.metrics.Metric
import com.datart.sensors.metrics.config.Config
import com.datart.sensors.metrics.input._
import com.datart.sensors.metrics.mapping.RowMapper
import com.datart.sensors.metrics.model.input.Row.{FailedMeasurement, Ignored, SuccessfulMeasurement}
import com.datart.sensors.metrics.model.input.{Humidity, Sensor}
import com.datart.sensors.metrics.model.report.SensorReport._
import com.datart.sensors.metrics.model.report._
import com.datart.sensors.metrics.report.ReportComposer
import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.Observable
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pureconfig._
import pureconfig.generic.auto._

class FlowProviderImplSpec extends AsyncWordSpec with Matchers {
  private implicit val scheduler: Scheduler = Scheduler.global

  private val config = ConfigSource.default.loadOrThrow[Config]

  private val expectedReport = TotalReport(
    allFilesProcessed = 1,
    allMeasurementsProcessed = 1,
    failedMeasurements = 0,
    aliveSensorsReports = Set(
      AliveSensorReport(
        "s1",
        0L,
        0L,
        0L
      )
    ),
    deadSensorsReports = Set.empty[DeadSensorReport]
  )

  private val mockedDirectoryReader: DirectoryReader = _ =>
    Observable
      .fromIterable(Seq(new JFile(getClass.getResource("/valid_inputs/csv/leader-1.csv").getPath)))
  private val mockedFileLinesReader: FileLinesReader = _ =>
    Observable.fromIterable(Seq("sensor-id,humidity", "s1,0", "s2,NaN"))
  private val mockedRowMapper: RowMapper = {
    case "sensor-id,humidity" => Ignored
    case "s1,0"               => SuccessfulMeasurement(Sensor("s1"), Humidity(0))
    case _                    => FailedMeasurement(Sensor("s2"))
  }

  private val mockedReportComposer = new ReportComposer {
    override def composeReport(metrics: Map[String, Metric]): Task[TotalReport] = {
      Task(expectedReport)
    }
  }

  private val testedImplementation = new FlowProviderImpl(
    config,
    mockedDirectoryReader,
    mockedFileLinesReader,
    mockedRowMapper,
    mockedReportComposer
  )

  "A FlowProviderImpl" can {

    "provide flow from file to stream of rows" should {

      "return flow for directory" in {

        testedImplementation
          .runFlow(File.newTemporaryDirectory().toJava)
          .runToFuture
          .map(_ shouldBe expectedReport)
      }
    }
  }
}
