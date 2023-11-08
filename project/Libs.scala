import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
  private val Version = "e84e4d4"

  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version
}
