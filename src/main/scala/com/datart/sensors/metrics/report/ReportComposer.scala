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
    val allFilesCount = metrics
      .collect {
        case (metricName, c: Counter) => (metricName, c)
      }
      .get(config.allFilesMetricName)
      .fold(0L)(_.getCount)

    val allMeasurementsCount = metrics
      .collect {
        case (metricName, c: Counter) => (metricName, c)
      }
      .get(config.allMeasurementsMetricName)
      .fold(0L)(_.getCount)

    val failedMeasurementsCount = metrics
      .collect {
        case (metricName, c: Counter) => (metricName, c)
      }
      .get(config.failedMeasurementsMetricName)
      .fold(0L)(_.getCount)

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
      .collect {
        case (metricName, m: Histogram) =>
          (metricName.replaceAll(config.failedSensorMetricNamePrefix, ""), m)
      }

    val onlyActive = metricsForActive.collect {
      case (metricName, m: Histogram) if !onlyDead.keys.toSet.contains(metricName) =>
        (metricName, m)
    }

    val aliveSensorsReports = extractReportsForActive(onlyActive)

    val deadSensorsReports = extractReportsForDead(onlyDead)

    (aliveSensorsReports, deadSensorsReports)
  }

  private def extractReportsForActive(onlyActive: Map[String, Histogram]): Set[AliveSensorReport] = {
    onlyActive.map {
      case (metricName, m) =>
        val snapshot = m.getSnapshot
        AliveSensorReport(metricName, snapshot.getMin, snapshot.getMean.round, snapshot.getMax)
    }.toSet
  }

  private def extractReportsForDead(onlyDead: Map[String, Histogram]): Set[DeadSensorReport] = {
    onlyDead.map {
      case (metricName, _) =>
        DeadSensorReport(metricName)
    }.toSet
  }
}
