package com.datart.sensors.metrics.flow

import java.io.File

import better.files._
import cats.MonadError
import cats.syntax.functor._
import cats.syntax.flatMap._
import com.codahale.metrics.Metric
import com.datart.sensors.metrics.config.Config
import com.datart.sensors.metrics.input._
import com.datart.sensors.metrics.mapping.RowMapper
import com.datart.sensors.metrics.model.input.Row
import com.datart.sensors.metrics.model.input.Row._
import com.datart.sensors.metrics.model.report.TotalReport
import com.datart.sensors.metrics.report.ReportComposer
import monix.eval._
import nl.grons.metrics4.scala.DefaultInstrumented

import scala.jdk.CollectionConverters._

trait FlowProvider[F[_]] {
  def runFlow(directory: File): F[TotalReport]
}

class FlowProviderImpl[F[_]](
    config: Config,
    directoryReader: DirectoryReader,
    fileLinesReader: FileLinesReader,
    rowMapper: RowMapper[F],
    reportComposer: ReportComposer[F]
)(implicit taskLike: TaskLike[F], taskLift: TaskLift[F], monadError: MonadError[F, Throwable])
    extends FlowProvider[F]
    with DefaultInstrumented {

  def runFlow(directory: File): F[TotalReport] = {
    for {
      _          <- processDirectory(directory)
      metricsMap <- getMetricsMap
      report     <- reportComposer.composeReport(metricsMap)
    } yield report
  }

  private def processDirectory(directory: File): F[Unit] = {
    directoryReader
      .getFiles(directory)
      .filter(p => p.getName.toLowerCase.endsWith(".csv") && p.toScala.nonEmpty)
      .flatMap { csvFile =>
        metrics.counter(config.allFilesMetricName).inc()
        fileLinesReader.getLines(csvFile)
      }
      .mapEvalF(rowMapper.toRow)
      .mapEvalF(updateMetrics)
      .completedF[F]
  }

  private def getMetricsMap: F[Map[String, Metric]] = {
    monadError.pure {
      metrics.registry.getMetrics.asScala.toMap.map {
        case (metricName, m) =>
          (metricName.replaceFirst(s"${getClass.getName}.", ""), m)
      }
    }
  }

  private def updateMetrics: Row => F[Unit] =
    row =>
      monadError.pure {
        row match {
          case SuccessfulMeasurement(sensor, humidity) =>
            metrics.counter(config.allMeasurementsMetricName).inc()
            metrics.histogram(sensor.name) += humidity.value
          case FailedMeasurement(sensor) =>
            metrics.counter(config.allMeasurementsMetricName).inc()
            metrics.counter(config.failedMeasurementsMetricName).inc()
            val _ = metrics.histogram(s"${config.failedSensorMetricNamePrefix}${sensor.name}")
          case _ =>
            ()
        }
    }
}
