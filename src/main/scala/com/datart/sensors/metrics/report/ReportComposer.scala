package com.datart.sensors.metrics.report

import com.codahale.metrics._
import com.datart.sensors.metrics.config.Config
import com.datart.sensors.metrics.model.report._
import monix.eval.Task

trait ReportComposer {
  def composeReport(metrics: Map[String, Metric]): Task[TotalReport]
}

class ReportComposerImpl(config: Config) extends ReportComposer {
  override def composeReport(metrics: Map[String, Metric]): Task[TotalReport] = ???
}
