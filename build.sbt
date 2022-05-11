val KotlincOptions = Seq("-Xopt-in=kotlin.time.ExperimentalTime", "-jvm-target", "1.8")
val KotlinVersion  = "1.6.10"

lazy val `sequencer-scripts` = project
  .in(file("."))
  .enablePlugins(KotlinPlugin)
  .aggregate(`ignore`)
  .settings(
    kotlinVersion                                := KotlinVersion,
    kotlincOptions ++= KotlincOptions,
    inThisBuild(
      List(
        organization := "com.github.tmtsoftware.sequencer-scripts",
        scalaVersion := "2.13.8",
        version      := "0.1.0-SNAPSHOT"
      )
    ),
    Compile / unmanagedSources / excludeFilter   := "*.conf",
    Test / unmanagedSourceDirectories += (Test / baseDirectory)(_ / "tests").value,
    Compile / mainClass                          := Some("esw.ocs.app.SequencerApp"),
    name                                         := "sequencer-scripts",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      Libs.`esw-ocs-dsl-kt`,
      Libs.`esw-ocs-script-kt`,
      Libs.`esw-ocs-app`,
      Libs.`junit` % Test
    ),
    Test / fork                                  := true
  )

// This project is only defined so that Intellij Idea highlights errors in sequence scripts
lazy val `sequencer-scripts-ignored` = project
  .in(file("scripts"))
  .enablePlugins(KotlinPlugin)
  .settings(
    kotlinVersion                                := KotlinVersion,
    kotlincOptions ++= KotlincOptions,
    inThisBuild(
      List(
        organization := "com.github.tmtsoftware.sequencer-scripts",
        scalaVersion := "2.13.8",
        version      := "0.1.0-SNAPSHOT"
      )
    ),
    Compile / unmanagedSourceDirectories += (Compile / baseDirectory)(_ / ".").value,
    name                                         := "sequencer-scripts-ignore",
    resolvers += "jitpack" at "https://jitpack.io"
  ).dependsOn(`sequencer-scripts`)

lazy val `ignore` = project.in(file(".ignore"))
