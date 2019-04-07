package com.datart.sensors.metrics.config

final case class Config(allFilesMetricName: String,
                        allMeasurementsMetricName: String,
                        failedMeasurementsMetricName: String,
                        failedSensorMetricNamePrefix: String)
