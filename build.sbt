val KotlincOptions = Seq("-Xopt-in=kotlin.time.ExperimentalTime", "-jvm-target", "1.8")
val KotlinVersion  = "1.5.20"

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
        scalaVersion := "2.13.6",
        version := "0.1.0-SNAPSHOT"
      )
    ),
    Compile / unmanagedSourceDirectories += (Compile / baseDirectory)(_ / "scripts").value,
    Compile / unmanagedSources / excludeFilter := "*.conf",
    Test / unmanagedSourceDirectories += (Test / baseDirectory)(_ / "tests").value,
    Compile / unmanagedResourceDirectories += (Compile / baseDirectory)(_ / "scripts").value,
    Compile / unmanagedResources / includeFilter := "*.conf",
    Compile / mainClass := Some("esw.ocs.app.SequencerApp"),
    name := "sequencer-scripts",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      Libs.`esw-ocs-dsl-kt`,
      Libs.`esw-ocs-app`
    ),
    Test / fork := true
  )

lazy val `ignore` = project.in(file(".ignore"))
