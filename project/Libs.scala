import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
  private val Version = "93212e16e6"

  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version
}
