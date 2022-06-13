import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
  private val Version = "1ea12a9"

  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version

  val `graal-sdk` = "org.graalvm.sdk" % "graal-sdk" % "22.1.0.1"
}
