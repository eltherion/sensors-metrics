package com.datart.sensors.metrics.input

import java.util.UUID

import better.files._
import com.datart.sensors.metrics.input.InputDirectoryError._
import org.scalatest._
import cats.syntax.validated._

class InputValidatorImplSpec extends WordSpec with Matchers {
  private val testedImplementation = new InputValidatorImpl()

  "An InputValidatorImpl" can {

    "check given input directory" should {

      "consider no provided directory as invalid" in {

        testedImplementation.validateInput(Array.empty) shouldBe NoPathProvided.invalid[File]
      }

      "consider not existing directory as invalid" in {

        val invalidInputdirectory = Array(UUID.randomUUID().toString)

        testedImplementation.validateInput(invalidInputdirectory) shouldBe NoSuchDirectory.invalid[File]
      }

      "consider non-directory file as invalid" in {

        val invalidInputDirectory = Array(Resource.getUrl("expected_output.txt").getPath)

        testedImplementation.validateInput(invalidInputDirectory) shouldBe NotADirectory.invalid[File]
      }

      "consider empty directory as invalid" in {

        val invalidInputDirectory = Array(File.newTemporaryDirectory().pathAsString)

        testedImplementation.validateInput(invalidInputDirectory) shouldBe EmptyDirectory.invalid[File]
      }

      "consider directory without nonempty csv files as invalid" in {

        val invalidInputDirectory = Array(Resource.getUrl("invalid_inputs/no_csv_files").getPath)

        testedImplementation.validateInput(invalidInputDirectory) shouldBe NoInputFilesInDirectory.invalid[File]
      }

      "consider proper directory as valid" in {

        val validInputDirectory = Array(Resource.getUrl("valid_inputs/csv").getPath)

        testedImplementation.validateInput(validInputDirectory).isValid shouldBe true
      }
    }
  }
}
