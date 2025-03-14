import kotlin.Keys._

val KotlincOptions = Seq(
  "-opt-in=kotlin.time.ExperimentalTime",
  "-Xallow-any-scripts-in-source-roots",
  "-Xuse-fir-lt=false",
  "-jvm-target",
  "21"
)
val KotlinVersion  = "2.1.10"
//kotlinLib("stdlib")

lazy val `sequencer-scripts` = project
  .in(file("."))
  .enablePlugins(KotlinPlugin)
  .aggregate(`ignore`)
  .settings(
    kotlinVersion := KotlinVersion,
    kotlincJvmTarget                             := "21",
    kotlincOptions ++= KotlincOptions,
    kotlinLib("stdlib"),
    inThisBuild(
      List(
        organization := "com.github.tmtsoftware.sequencer-scripts",
        scalaVersion := "3.6.4",
        version      := "0.1.0-SNAPSHOT"
      )
    ),
    Compile / unmanagedSourceDirectories += (Compile / baseDirectory)(_ / "scripts").value,
    Compile / unmanagedSources / excludeFilter := "*.conf",
    Test / unmanagedSourceDirectories += (Test / baseDirectory)(_ / "tests").value,
    Compile / unmanagedResourceDirectories += (Compile / baseDirectory)(_ / "scripts").value,
    Compile / unmanagedResources / includeFilter := "*.conf",
    reStart / mainClass                          := Some("esw.ocs.app.SequencerApp"),
    name := "sequencer-scripts",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      Libs.`esw-ocs-dsl-kt`,
      Libs.`esw-ocs-app`
    ),
    Test / fork := true
  )

lazy val `ignore` = project.in(file(".ignore"))
