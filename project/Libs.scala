import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
//  private val Version = "1ea12a9"
  private val Version = "bba6282a50c34d26d91e70b74f776ca87f17effd"

  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-script-kt` = Org %% "esw-ocs-script-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version

  val `junit` = "junit"          % "junit" % "4.12"
}
