package com.datart.sensors.metrics.model.report

import com.datart.sensors.metrics.model.report.SensorReport._

final case class TotalReport(
    allFilesProcessed: Long,
    allMeasurementsProcessed: Long,
    failedMeasurements: Long,
    aliveSensorsReports: Set[AliveSensorReport],
    deadSensorsReports: Set[DeadSensorReport]
) {
  override def toString: String = {
    def stringReportForAliveSensors: String = {
      val sortedByAvgDesc = aliveSensorsReports.toList.sortBy(_.avg)(Ordering.Long.reverse)
      s"""${sortedByAvgDesc.map(_.toString).mkString("\n")}"""
    }

    def stringReportForDeadSensors: String = {
      val sortedByNameAsc = deadSensorsReports.toList.sortBy(_.sensorName)
      s"""${sortedByNameAsc.map(_.toString).mkString("\n")}"""
    }

    s"""Num of processed files: $allFilesProcessed
       |Num of processed measurements: $allMeasurementsProcessed
       |Num of failed measurements: $failedMeasurements
       |
       |Sensors with highest avg humidity:
       |
       |sensor-id,min,avg,max
       |$stringReportForAliveSensors
       |$stringReportForDeadSensors""".stripMargin
  }
}
