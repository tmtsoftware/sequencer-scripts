import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
//  private val Version = "7baae4ab18f3d39a57557cc4dc1a934d00e4b81b"
  private val Version = "0.1.0-SNAPSHOT"

  val `esw-ocs-dsl-kt`    = Org             %% "esw-ocs-dsl-kt"    % Version
  val `esw-ocs-script-kt` = Org             %% "esw-ocs-script-kt" % Version
  val `esw-ocs-app`       = Org             %% "esw-ocs-app"       % Version
  val `esw-ocs-impl`      = Org             %% "esw-ocs-impl"      % Version
  val `ivy`               = "org.apache.ivy" % "ivy"               % "2.5.0"

  val `junit` = "junit" % "junit" % "4.12"
}
