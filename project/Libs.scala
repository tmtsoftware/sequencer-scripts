import sbt._

object Libs {
  private val Org     = "com.github.tmtsoftware.esw"
  private val Version = {
    sys.props.get("dev") match {
      case Some("true") => "0.1.0-SNAPSHOT"
      case _            => "d8c798151"
    }
  }
  val `esw-ocs-dsl-kt` = Org %% "esw-ocs-dsl-kt" % Version
  val `esw-ocs-app`    = Org %% "esw-ocs-app"    % Version
}
