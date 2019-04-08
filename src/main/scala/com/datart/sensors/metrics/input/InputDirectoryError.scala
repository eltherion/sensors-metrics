package com.datart.sensors.metrics.input

sealed abstract class InputDirectoryError(message: String) extends Exception(message)

object InputDirectoryError {
  final case object NoPathProvided  extends InputDirectoryError("No path to input directory provided")
  final case object NoSuchDirectory extends InputDirectoryError("No such directory exists")
  final case object NotADirectory   extends InputDirectoryError("Provided path indicates regular file")
  final case object EmptyDirectory  extends InputDirectoryError("Provided directory is empty")
  final case object NoInputFilesInDirectory
      extends InputDirectoryError("Missing nonempty CSV files inside provided directory")
}
