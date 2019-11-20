
lazy val `sequencer-scripts` = project
  .in(file("."))
  .enablePlugins(KotlinPlugin)
  .settings(
    kotlinVersion := "1.3.50",
    kotlincOptions ++= Seq("-Xuse-experimental=kotlin.time.ExperimentalTime", "-jvm-target", "1.8"),
    inThisBuild(List(
      organization := "com.github.tmtsoftware",
      scalaVersion := "2.13.0",
      version      := "0.1.0-SNAPSHOT"
    )),

    unmanagedSourceDirectories in Compile += (baseDirectory in Compile) (_ / "scripts").value,
    unmanagedSourceDirectories in Test += (baseDirectory in Test) (_ / "tests").value,
    unmanagedResourceDirectories in Compile += (baseDirectory in Compile) (_ / "configs").value,
    mainClass in Compile := Some("esw.ocs.app.SequencerApp"),
    name := "sequencer-scripts",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      Libs.`esw-ocs-dsl-kt`,
      Libs.`esw-ocs-app`,
      Libs.`scalaTest`
    )
  )
