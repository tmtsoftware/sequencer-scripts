import sbt.Keys.unmanagedSourceDirectories

lazy val `sequencer-scripts` = project
  .in(file("."))
  .enablePlugins(KotlinPlugin)
  .settings(
    kotlinVersion := "1.4.21",
    kotlincOptions ++= Seq("-Xuse-experimental=kotlin.time.ExperimentalTime", "-jvm-target", "1.8"),
    inThisBuild(List(
      organization := "com.github.tmtsoftware.sequencer-scripts",
      scalaVersion := "2.13.5",
      version      := "0.2.0"
    )),

    Compile / unmanagedSourceDirectories += (Compile / baseDirectory) (_ / "scripts").value,
    Compile / unmanagedSources / excludeFilter := "*.conf",
    Test / unmanagedSourceDirectories += (Test / baseDirectory ) (_ / "tests").value,
    Compile / unmanagedResourceDirectories += (Compile / baseDirectory ) (_ / "scripts").value,
    Test / unmanagedResourceDirectories += (Test / baseDirectory ) (_ / "tests/resources").value,
    Compile / unmanagedResources / includeFilter := "*.conf",
    reStart / mainClass := Some("esw.ocs.app.SequencerApp"),
    name := "sequencer-scripts",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      Libs.`esw-ocs-dsl-kt`,
      Libs.`esw-ocs-app`,
      Libs.`esw-testkit`
    ),
    Test / fork := true
  )
