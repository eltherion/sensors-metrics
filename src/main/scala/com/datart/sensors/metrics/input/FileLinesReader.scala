package com.datart.sensors.metrics.input

import java.io.File
import better.files._

import monix.reactive.Observable

trait FileLinesReader {
  def getLines(file: File): Observable[String]
}

class FileLinesReaderImpl extends FileLinesReader {

  override def getLines(file: File): Observable[String] = {
    Observable.fromIterable(file.toScala.lines.toIterable)
  }
}