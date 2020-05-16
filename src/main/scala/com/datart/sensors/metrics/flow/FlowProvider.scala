package com.datart.sensors.metrics.flow

import java.io.File

import better.files._
import com.datart.sensors.metrics.config.Config
import com.datart.sensors.metrics.input._
import com.datart.sensors.metrics.mapping.RowMapper
import com.datart.sensors.metrics.model.input.Row
import com.datart.sensors.metrics.model.input.Row._
import com.datart.sensors.metrics.model.report.TotalReport
import com.datart.sensors.metrics.report.ReportComposer
import monix.eval.Task
import nl.grons.metrics4.scala.DefaultInstrumented

import scala.jdk.CollectionConverters._

trait FlowProvider {
  def runFlow(directory: File): Task[TotalReport]
}

class FlowProviderImpl(
    config: Config,
    directoryReader: DirectoryReader,
    fileLinesReader: FileLinesReader,
    rowMapper: RowMapper,
    reportComposer: ReportComposer
) extends FlowProvider
    with DefaultInstrumented {

  override def runFlow(directory: File): Task[TotalReport] = {
    directoryReader
      .getFiles(directory)
      .filter(p => p.getName.toLowerCase.endsWith(".csv") && p.toScala.nonEmpty)
      .flatMap { csvFile =>
        metrics.counter(config.allFilesMetricName).inc()
        fileLinesReader.getLines(csvFile)
      }
      .map(rowMapper.toRow)
      .map[Unit](updateMetrics)
      .completedL
      .map { _ =>
        metrics.registry.getMetrics.asScala.toMap.map {
          case (metricName, m) =>
            (metricName.replaceFirst(s"${getClass.getName}.", ""), m)
        }
      }
      .flatMap(reportComposer.composeReport)
  }

  private def updateMetrics: Row => Unit = {
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
