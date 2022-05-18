import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
//  private val Version = "1ea12a9"
  private val Version = "b964c761e4f998d64306dd70298da577558a42f2"

  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-script-kt` = Org %% "esw-ocs-script-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version

  val `junit` = "junit"          % "junit" % "4.12"
}
