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
      .get(config.allFilesMetricName) match {
      case Some(c: Counter) => c.getCount
      case _                => 0L
    }

    val allMeasurementsCount = metrics
      .get(config.allMeasurementsMetricName) match {
      case Some(c: Counter) => c.getCount
      case _                => 0L
    }

    val failedMeasurementsCount = metrics
      .get(config.failedMeasurementsMetricName) match {
      case Some(c: Counter) => c.getCount
      case _                => 0L
    }

    (allFilesCount, allMeasurementsCount, failedMeasurementsCount)
  }

  private def extractSensorsReports(metrics: Map[String, Metric]): (Set[AliveSensorReport], Set[DeadSensorReport]) = {
    val flowMetricsNames =
      Set[String](config.allFilesMetricName, config.allMeasurementsMetricName, config.failedMeasurementsMetricName)

    val metricsForDeadGroupKey   = "metricsForDead"
    val metricsForActiveGroupKey = "metricsForActive"

    val groupedMetrics = metrics.groupBy {
      case (metricName, _) if metricName.startsWith(config.failedSensorMetricNamePrefix) =>
        metricsForDeadGroupKey
      case (metricName, _) if flowMetricsNames.contains(metricName) =>
        ""
      case _ =>
        metricsForActiveGroupKey
    }

    val metricsForDead   = groupedMetrics.get(metricsForDeadGroupKey).fold(Map.empty[String, Metric])(identity)
    val metricsForActive = groupedMetrics.get(metricsForActiveGroupKey).fold(Map.empty[String, Metric])(identity)

    val onlyDeadHistograms = metricsForDead
      .collect {
        case (metricName, m: Histogram) =>
          (metricName.replaceAll(config.failedSensorMetricNamePrefix, ""), m)
      }
    val onlyActiveHistograms = metricsForActive
      .collect {
        case (metricName, m: Histogram) =>
          (metricName, m)
      }

    val aliveSensorsReports = extractReportsForActive(onlyActiveHistograms)
    val deadSensorsReports = extractReportsForDead(onlyDeadHistograms)

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
