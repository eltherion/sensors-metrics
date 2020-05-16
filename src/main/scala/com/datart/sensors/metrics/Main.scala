package com.datart.sensors.metrics

import cats.MonadError
import cats.syntax.functor._
import cats.syntax.flatMap._
import com.datart.sensors.metrics.config.Config
import com.datart.sensors.metrics.flow.FlowProviderImpl
import com.datart.sensors.metrics.input._
import com.datart.sensors.metrics.mapping.RowMapperImpl
import com.datart.sensors.metrics.report.ReportComposerImpl
import com.typesafe.scalalogging.StrictLogging
import monix.eval.{TaskLift, TaskLike}
import monix.execution.Scheduler.global
import monix.execution.schedulers.CanBlock.permit
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class Main[F[_]](args: Array[String])(
    implicit taskLike: TaskLike[F],
    taskLift: TaskLift[F],
    monadError: MonadError[F, Throwable]
) extends StrictLogging {

  private val config         = ConfigSource.default.loadOrThrow[Config]
  private val inputValidator = new InputValidatorImpl()

  private val validatedInput = inputValidator.validateInput(args)
  private val flowProvider = new FlowProviderImpl[F](
    config,
    new DirectoryReaderImpl(),
    new FileLinesReaderImpl(),
    new RowMapperImpl(),
    new ReportComposerImpl(config)
  )

  def run: F[Unit] = {
    validatedInput.fold[F[Unit]](
      { ex =>
        monadError.pure {
          val errorMessage =
            s"Invalid input directory, reason: ${ex.getMessage}. Recover impossible, exiting application..."
          logger.error(errorMessage)
          scala.sys.error(errorMessage)
        }
      }, { inputDirectory =>
        for {
          totalReport <- flowProvider.runFlow(inputDirectory.toJava)
          _ <- monadError.pure {
                logger.info(
                  s"""
                 |${totalReport.toString}""".stripMargin
                )
              }
        } yield ()
      }
    )
  }
}

object MainMonixTaskImpl extends App {
  import monix.eval.Task

  new Main[Task](args).run.runSyncUnsafe(Duration.Inf)(global, permit)
}

object MainCatsEffectIOImpl extends App {
  import cats.effect._
  import monix.eval.Task

  private implicit val taskLike: TaskLift[IO] = TaskLift.toIO(Task.catsEffect(global))

  new Main[IO](args).run.unsafeRunSync()
}

object MainFutureImpl extends App {
  import cats.implicits.catsStdInstancesForFuture
  import monix.eval.Task

  private implicit val taskLike: TaskLike[Future] = new TaskLike[Future] {
    override def apply[A](fa: Future[A]): Task[A] = Task.deferFuture(fa)
  }

  private implicit val taskLift: TaskLift[Future] = new TaskLift[Future] {
    override def apply[A](task: Task[A]): Future[A] = task.runToFuture(global)
  }

  private implicit val monadError: MonadError[Future, Throwable] = catsStdInstancesForFuture(global)

  Await.result(new Main[Future](args).run, Duration.Inf)
}

@SuppressWarnings(Array("org.wartremover.warts.Nothing", "org.wartremover.warts.Any"))
object MainZIOTaskImpl extends App {
  import monix.eval.{Task => MonixTask}
  import zio.{Task => ZIOTask}
  import zio._
  import zio.interop.catz._

  private val runtime = Runtime.default

  private implicit val taskLike: TaskLike[ZIOTask] = new TaskLike[ZIOTask] {
    override def apply[A](zioTask: ZIOTask[A]): MonixTask[A] = MonixTask.deferFuture {
      runtime.unsafeRun(zioTask.toFuture)
    }
  }

  private implicit val taskLift: TaskLift[ZIOTask] = new TaskLift[ZIOTask] {
    override def apply[A](monixTask: MonixTask[A]): ZIOTask[A] = ZIO.fromFuture { _ =>
      monixTask.runToFuture(global)
    }
  }

  runtime.unsafeRun(new Main[ZIOTask](args).run)
}
