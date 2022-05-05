import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
//  private val Version = "1ea12a9"
  private val Version = "0.1.0-SNAPSHOT"

  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-script-host-kt` = Org %% "esw-ocs-script-host-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version
}
