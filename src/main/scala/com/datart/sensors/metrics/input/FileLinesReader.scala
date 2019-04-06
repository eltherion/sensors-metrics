package com.datart.sensors.metrics.input

import java.io.File

import monix.reactive.Observable

trait FileLinesReader {
  def getLines(file: File): Observable[String]
}

class FileLinesReaderImpl extends FileLinesReader {

  override def getLines(file: File): Observable[String] = ???
}