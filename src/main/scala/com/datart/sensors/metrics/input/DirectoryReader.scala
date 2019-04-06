package com.datart.sensors.metrics.input

import java.io.File

import monix.reactive.Observable

trait DirectoryReader {
  def getFiles(directory: File): Observable[File]
}

class DirectoryReaderImpl extends DirectoryReader {

  override def getFiles(directory: File): Observable[File] = ???
}