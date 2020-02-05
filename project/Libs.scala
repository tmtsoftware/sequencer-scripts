import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
  private val Version = "d0e5a3eb53a7a8f75630f1757747418b9e9b1ccf"

  val `esw-ocs-dsl-kt` = Org             %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-app`    = Org             %% "esw-ocs-app"    % Version
}
