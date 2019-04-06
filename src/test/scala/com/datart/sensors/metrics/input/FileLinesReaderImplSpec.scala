package com.datart.sensors.metrics.input

import better.files._
import monix.execution.Scheduler
import org.scalatest._

class FileLinesReaderImplSpec extends AsyncWordSpec with Matchers {

  private implicit val scheduler: Scheduler = Scheduler.global

  private val testedImplementation = new FileLinesReaderImpl()

  "A FileLinesReaderImplSpec" can {

    "read file lines" should {

      "return an empty stream of file lines in an empty file is provided" in {
        val emptyFile = File.newTemporaryFile().toJava

        testedImplementation
          .getLines(emptyFile)
          .toListL
          .runToFuture
          .map(_ shouldBe List())
      }

      "return a nonempty stream of file lines in a nonempty file is provided" in {
        val nonEmptyFile = File.newTemporaryFile()

        val line1 = "line1"
        val line2 = "line2"
        val expectedLines = List(line1, line2)

        nonEmptyFile.write(expectedLines.mkString("\n"))

        testedImplementation
          .getLines(nonEmptyFile.toJava)
          .toListL
          .runToFuture
          .map(_ shouldBe expectedLines)
      }
    }
  }
}