package com.datart.sensors.metrics.input

import java.io.File

import better.files._
import monix.eval.Task
import monix.reactive.Observable

trait DirectoryReader {
  def getFiles(directory: File): Observable[File]
}

class DirectoryReaderImpl extends DirectoryReader {

  override def getFiles(directory: File): Observable[File] = {
    Observable.fromIterator {
      Task(directory.toScala.children.map(_.toJava))
    }
  }
}