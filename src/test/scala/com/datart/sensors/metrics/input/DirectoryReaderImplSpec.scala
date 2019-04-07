package com.datart.sensors.metrics.input

import monix.execution.Scheduler
import org.scalatest._
import better.files._

class DirectoryReaderImplSpec extends AsyncWordSpec with Matchers {

  private implicit val scheduler: Scheduler = Scheduler.global

  private val testedImplementation = new DirectoryReaderImpl()

  "A DirectoryReaderImpl" can {

    "read directory" should {

      "return an empty stream of files in an empty directory is provided" in {
        val emptyDirectory = File.newTemporaryDirectory().toJava

        testedImplementation
          .getFiles(emptyDirectory)
          .toListL
          .runToFuture
          .map(_ shouldBe List())
      }

      "return a nonempty stream of files in a nonempty directory is provided" in {
        val nonEmptyDirectory = File.newTemporaryDirectory()
        val temporaryFile     = File.newTemporaryFile(parent = Option(nonEmptyDirectory)).toJava

        testedImplementation
          .getFiles(nonEmptyDirectory.toJava)
          .toListL
          .runToFuture
          .map(_ shouldBe List(temporaryFile))
      }
    }
  }
}
