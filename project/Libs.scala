import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
  private val Version = "e3c96ec"
//  private val Version = "0.1.0-SNAPSHOT"

  val `esw-ocs-dsl-kt` = Org             %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-app`    = Org             %% "esw-ocs-app"    % Version
  val `scalaTest`      = "org.scalatest" %% "scalatest"      % "3.0.8" % Test
}
