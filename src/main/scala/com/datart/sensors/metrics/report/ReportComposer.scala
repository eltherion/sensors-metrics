package com.datart.sensors.metrics.report

import com.codahale.metrics._
import com.datart.sensors.metrics.config.Config
import com.datart.sensors.metrics.model.report.SensorReport._
import com.datart.sensors.metrics.model.report._
import monix.eval.Task

trait ReportComposer {
  def composeReport(metrics: Map[String, Metric]): Task[TotalReport]
}

class ReportComposerImpl(config: Config) extends ReportComposer {
  override def composeReport(metrics: Map[String, Metric]): Task[TotalReport] = {
    Task {

      val (allFilesCount, allMeasurementsCount, failedMeasurementsCount) = extractFlowMetrics(metrics)

      val (aliveSensorsReports, deadSensorsReports) = extractSensorsReports(metrics)

      TotalReport(
        allFilesCount,
        allMeasurementsCount,
        failedMeasurementsCount,
        aliveSensorsReports,
        deadSensorsReports
      )
    }
  }

  private def extractFlowMetrics(metrics: Map[String, Metric]): (Long, Long, Long) = {
    val allFilesCount = metrics.get(config.allFilesMetricName).fold(0L) {
      case c: Counter =>
        c.getCount
      case _ =>
        0
    }

    val allMeasurementsCount = metrics.get(config.allMeasurementsMetricName).fold(0L) {
      case c: Counter =>
        c.getCount
      case _ =>
        0
    }

    val failedMeasurementsCount = metrics.get(config.failedMeasurementsMetricName).fold(0L) {
      case c: Counter =>
        c.getCount
      case _ =>
        0
    }

    (allFilesCount, allMeasurementsCount, failedMeasurementsCount)
  }

  private def extractSensorsReports(metrics: Map[String, Metric]): (Set[AliveSensorReport], Set[DeadSensorReport]) = {
    val flowMetricsNames =
      Set[String](config.allFilesMetricName, config.allMeasurementsMetricName, config.failedMeasurementsMetricName)

    val sensorsMetrics = metrics
      .filterNot {
        case (metricName, _) =>
          flowMetricsNames.contains(metricName)
      }

    val metricsForDead = sensorsMetrics.filter {
      case (metricName, _) =>
        metricName.startsWith(config.failedSensorMetricNamePrefix)
    }

    val metricsForActive = sensorsMetrics.toSet.diff(metricsForDead.toSet).toMap

    val onlyDead = metricsForDead
      .filterNot {
        case (metricName, _) =>
          val sensorName = metricName.replaceAll(config.failedSensorMetricNamePrefix, "")
          metricsForActive.keys.toSet.contains(sensorName)
      }
      .map {
        case (metricName, m) =>
          (metricName.replaceAll(config.failedSensorMetricNamePrefix, ""), m)
      }

    val onlyActive = metricsForActive.filterNot {
      case (metricName, _) =>
        onlyDead.keys.toSet.contains(metricName)
    }

    val aliveSensorsReports = onlyActive.flatMap {
      case (metricName, m: Histogram) =>
        val snapshot = m.getSnapshot
        Set(AliveSensorReport(metricName, snapshot.getMin, snapshot.getMean.round, snapshot.getMax))
      case _ =>
        Set.empty[AliveSensorReport]
    }.toSet

    val deadSensorsReports = onlyDead.flatMap {
      case (metricName, _: Histogram) =>
        Set(DeadSensorReport(metricName))
      case _ =>
        Set.empty[DeadSensorReport]
    }.toSet

    (aliveSensorsReports, deadSensorsReports)
  }
}
