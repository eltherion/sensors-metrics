package com.datart.sensors.metrics

import com.datart.sensors.metrics.config.Config
import com.datart.sensors.metrics.flow.FlowProviderImpl
import com.datart.sensors.metrics.input._
import com.datart.sensors.metrics.mapping.RowMapperImpl
import com.datart.sensors.metrics.report.ReportComposerImpl
import com.typesafe.scalalogging.StrictLogging
import monix.execution.Scheduler
import pureconfig.generic.auto._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util._

object Main extends App with StrictLogging {

  private implicit val scheduler: Scheduler = Scheduler.global

  private val config         = pureconfig.loadConfigOrThrow[Config]
  private val inputValidator = new InputValidatorImpl()

  private val validatedInput = inputValidator.validateInput(args)

  validatedInput.fold[Unit](
    { ex =>
      val errorMessage =
        s"Invalid input directory, reason: ${ex.getMessage}. Recover impossible, exiting application..."
      logger.error(errorMessage)
      scala.sys.error(errorMessage)
    }, { inputDirectory =>
      val flowProvider = new FlowProviderImpl(
        config,
        new DirectoryReaderImpl(),
        new FileLinesReaderImpl(),
        new RowMapperImpl(),
        new ReportComposerImpl(config)
      )

      val fResult = flowProvider
        .runFlow(inputDirectory.toJava)
        .runToFuture

      fResult.onComplete {
        case Failure(error) =>
          logger.error(s"Error occured while collecting statistics: ${error.getMessage}.")
        case _ =>
          ()
      }

      val totalReport = Await.result(fResult, Duration.Inf)

      val _ = logger.info(
        s"""
           |$totalReport""".stripMargin
      )
    }
  )
}
