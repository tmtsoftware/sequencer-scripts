
lazy val `sequencer-scripts` = project
  .in(file("."))
  .enablePlugins(KotlinPlugin)
  .aggregate(`ignore`)
  .settings(
    kotlinVersion := "1.4.21",
    kotlincOptions ++= Seq("-Xuse-experimental=kotlin.time.ExperimentalTime", "-jvm-target", "1.8"),
    inThisBuild(List(
      organization := "com.github.tmtsoftware.sequencer-scripts",
      scalaVersion := "2.13.5",
      version      := "0.1.0-SNAPSHOT"
    )),

    unmanagedSourceDirectories in Compile += (baseDirectory in Compile) (_ / "scripts").value,
    excludeFilter in (Compile, unmanagedSources) := "*.conf",
    unmanagedSourceDirectories in Test += (baseDirectory in Test) (_ / "tests").value,
    unmanagedResourceDirectories in Compile += (baseDirectory in Compile) (_ / "scripts").value,
    includeFilter in (Compile, unmanagedResources) := "*.conf",
    mainClass in Compile := Some("esw.ocs.app.SequencerApp"),
    name := "sequencer-scripts",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      Libs.`esw-ocs-dsl-kt`,
      Libs.`esw-ocs-app`
    ),
    fork in Test := true
  )

lazy val `ignore` = project.in(file(".ignore"))
