lazy val `sequencer-scripts` = project
  .in(file("."))
  .enablePlugins(KotlinPlugin)
  .aggregate(`ignore`)
  .settings(
    kotlinVersion := "1.6.10",
    kotlincOptions ++= Seq("-Xopt-in=kotlin.time.ExperimentalTime", "-jvm-target", "1.8"),
    inThisBuild(
      List(
        organization := "com.github.tmtsoftware.sequencer-scripts",
        scalaVersion := "2.13.8",
        version := "0.4.0"
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
