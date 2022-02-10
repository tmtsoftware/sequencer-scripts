import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
  private val Version = "f3d073f"

  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version
}
