import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
  private val Version = "14bc2645"
//  private val Version = "0.1.0-SNAPSHOT"

  val `esw-ocs-dsl-kt` = Org             %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-app`    = Org             %% "esw-ocs-app"    % Version
}
