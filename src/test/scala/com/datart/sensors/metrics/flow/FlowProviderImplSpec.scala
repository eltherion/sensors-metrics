package com.datart.sensors.metrics.flow

import java.io.{File => JFile}

import better.files._
import cats.implicits.catsStdInstancesForTry
import monix.eval._
import com.codahale.metrics.Metric
import com.datart.sensors.metrics.config.Config
import com.datart.sensors.metrics.input._
import com.datart.sensors.metrics.mapping.RowMapper
import com.datart.sensors.metrics.model.input.Row.{FailedMeasurement, Ignored, SuccessfulMeasurement}
import com.datart.sensors.metrics.model.input.{Humidity, Sensor}
import com.datart.sensors.metrics.model.report.SensorReport._
import com.datart.sensors.metrics.model.report._
import com.datart.sensors.metrics.report.ReportComposer
import monix.execution.Scheduler
import monix.reactive.Observable
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.duration.Duration
import scala.util._

class FlowProviderImplSpec extends AnyWordSpec with Matchers {
  private implicit val scheduler: Scheduler = Scheduler.global
  private implicit val taskLift: TaskLift[Try] = new TaskLift[Try] {
    override def apply[A](task: Task[A]): Try[A] = Try(task.runSyncUnsafe(Duration.Inf))
  }

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
  private val mockedRowMapper: RowMapper[Try] = {
    case "sensor-id,humidity" => Success(Ignored)
    case "s1,0"               => Success(SuccessfulMeasurement(Sensor("s1"), Humidity(0)))
    case _                    => Success(FailedMeasurement(Sensor("s2")))
  }

  private val mockedReportComposer = new ReportComposer[Try] {
    override def composeReport(metrics: Map[String, Metric]): Try[TotalReport] = {
      Try(expectedReport)
    }
  }

  private val testedImplementation: FlowProviderImpl[Try] = new FlowProviderImpl(
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
          .runFlow(File.newTemporaryDirectory().toJava) shouldBe Success(expectedReport)
      }
    }
  }
}
