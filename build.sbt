
lazy val `sequencer-scripts` = project
  .in(file("."))
  .enablePlugins(KotlinPlugin)
  .settings(
    kotlinVersion := "1.4.10",
    kotlincOptions ++= Seq("-Xuse-experimental=kotlin.time.ExperimentalTime", "-jvm-target", "1.8"),
    inThisBuild(List(
      organization := "com.github.tmtsoftware.sequencer-scripts",
      scalaVersion := "2.13.3",
      version      := "0.2.0"
    )),

    unmanagedSourceDirectories in Compile += (baseDirectory in Compile) (_ / "scripts").value,
    excludeFilter in (Compile, unmanagedSources) := "*.conf",
    unmanagedSourceDirectories in Test += (baseDirectory in Test) (_ / "tests").value,
    unmanagedResourceDirectories in Compile += (baseDirectory in Compile) (_ / "scripts").value,
    unmanagedResourceDirectories in Test += (baseDirectory in Test) (_ / "tests/resources").value,
    includeFilter in (Compile, unmanagedResources) := "*.conf",
    mainClass in Compile := Some("esw.ocs.app.SequencerApp"),
    name := "sequencer-scripts",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      Libs.`esw-ocs-dsl-kt`,
      Libs.`esw-ocs-app`,
      Libs.`esw-testkit`
    ),
    fork in Test := true
  )
