import sbt._

object Dependencies {
  private val betterFiles     = "com.github.pathikrit"    %% "better-files"     % VersionsOf.betterFiles
  private val logbackClassic  = "ch.qos.logback"          %  "logback-classic"  % VersionsOf.logbackClassic
  private val metricsScala    = "nl.grons"                %% "metrics4-scala"   % VersionsOf.metricsScala
  private val monix           = "io.monix"                %% "monix"            % VersionsOf.monix
  private val pureConfig      = "com.github.pureconfig"   %% "pureconfig"       % VersionsOf.pureConfig
  private val scalatest       = "org.scalatest"           %% "scalatest"        % VersionsOf.scalatest      % Test

  val all: Seq[ModuleID] = Seq(
    betterFiles,
    logbackClassic,
    metricsScala,
    monix,
    pureConfig,
    scalatest
  )
}
