package com.datart.sensors.metrics.input

import better.files._
import cats.Applicative
import cats.data._
import cats.syntax.validated._
import com.datart.sensors.metrics.input.InputDirectoryError._

import scala.util.Try

trait InputValidator {
  type VErr[T] = ValidatedNel[Throwable, T]
  def validateInput(args: Array[String]): Validated[Throwable, File]
}

class InputValidatorImpl extends InputValidator {
  def validateInput(args: Array[String]): Validated[Throwable, File] = {
    args
      .take(1)
      .toList match {
      case Nil =>
        NoPathProvided.invalid[File]
      case h :: _ =>
        Validated
          .catchNonFatal(File(h))
          .andThen { directory =>
            Applicative[VErr]
              .map4(
                check(directory, _.exists, NoSuchDirectory),
                check(directory, _.isDirectory, NotADirectory),
                check(directory, _.nonEmpty, EmptyDirectory),
                check(directory, containsNonEmptyReadableCsvFiles, NoInputFilesInDirectory)
              ) {
                case (_, _, _, _) => directory
              }
              .leftMap(_.head)
          }
    }
  }

  private def check(directory: File,
                    directoryCheck: File => Boolean,
                    checkedError: InputDirectoryError): Validated[NonEmptyList[Throwable], Unit] = {
    if (directoryCheck(directory)) {
      ().validNel[InputDirectoryError]
    } else {
      checkedError.invalidNel[Unit]
    }
  }

  private def containsNonEmptyReadableCsvFiles(directory: File): Boolean = {
    Try(directory.children)
      .fold(_ => false, _.exists { file =>
        file.name.toLowerCase.endsWith(".csv") && file.nonEmpty
      })
  }
}
