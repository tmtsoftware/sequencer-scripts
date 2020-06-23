import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
  private val Version = "58f4fc4206"

  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version
}
