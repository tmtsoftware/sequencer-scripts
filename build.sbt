val KotlincOptions = Seq("-Xopt-in=kotlin.time.ExperimentalTime", "-jvm-target", "17")
val KotlinVersion  = "1.9.20"
kotlinLib("stdlib")

lazy val `sequencer-scripts` = project
  .in(file("."))
  .enablePlugins(KotlinPlugin)
  .aggregate(`ignore`)
  .settings(
    kotlinVersion := KotlinVersion,
    kotlincOptions ++= KotlincOptions,
    inThisBuild(
      List(
        organization := "com.github.tmtsoftware.sequencer-scripts",
        scalaVersion := "3.3.0",
        version      := "0.1.0-SNAPSHOT"
//        version := "0.5.1"
      )
    ),
    Compile / unmanagedSources / excludeFilter := "*.conf",
    Test / unmanagedSourceDirectories += (Test / baseDirectory)(_ / "tests").value,
    Compile / mainClass := Some("esw.ocs.app.SequencerApp"),
    name := "sequencer-scripts",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      Libs.`esw-ocs-dsl-kt`,
      Libs.`esw-ocs-script-kt`,
      Libs.`esw-ocs-app`,
      Libs.`esw-ocs-impl`,
      Libs.`junit` % Test
    ),
    Test / fork := true
  )

lazy val `ignore` = project.in(file(".ignore"))
