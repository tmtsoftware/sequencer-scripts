import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
//  private val Version = "1ea12a9"
  private val Version = "6934692f76eb406b73402153eefb3ca0e9f8133f"

  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-script-kt` = Org %% "esw-ocs-script-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version

  val `junit` = "junit"          % "junit" % "4.12"
}
