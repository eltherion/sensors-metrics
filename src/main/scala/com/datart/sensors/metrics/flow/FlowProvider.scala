package com.datart.sensors.metrics.flow

import java.io.File

import com.datart.sensors.metrics.config.Config
import com.datart.sensors.metrics.input._
import com.datart.sensors.metrics.mapping.RowMapper
import com.datart.sensors.metrics.model.report.TotalReport
import com.datart.sensors.metrics.report.ReportComposer
import monix.eval.Task
import nl.grons.metrics4.scala.DefaultInstrumented

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

  override def runFlow(directory: File): Task[TotalReport] = ???
}
