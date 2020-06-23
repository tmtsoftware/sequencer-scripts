import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
  private val Version = "e5f7eb301e6c7315b2f79a72752dffa8a5954cce"

  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version
}
