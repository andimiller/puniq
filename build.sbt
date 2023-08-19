import scala.scalanative.build.{BuildTarget, LTO, Mode}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val theta = (crossProject(NativePlatform, JVMPlatform, JSPlatform))
  .crossType(CrossType.Full)
  .in(file("modules/theta"))
  .settings(
    libraryDependencies ++= List(
      "org.typelevel" %%% "cats-core"        % "2.9.0",
      "org.typelevel" %%% "cats-laws"        % "2.9.0" % Test,
      "org.typelevel" %%% "discipline-munit" % "1.0.9" % Test,
      "org.typelevel" %%% "cats-effect-std"  % "3.5.1",
      "org.typelevel" %%% "cats-effect"      % "3.5.1"
    )
  )

lazy val puniq = (crossProject(NativePlatform)
  .crossType(CrossType.Full)
  .in(file("modules/puniq")))
  .dependsOn(theta)
  .settings(
    libraryDependencies ++= List(
      "org.typelevel"  %%% "toolkit"            % "0.1.6",
      "co.fs2"         %%% "fs2-io"             % "3.8-1af22dd",
      "co.fs2"         %%% "fs2-core"           % "3.8-1af22dd",
      "com.lihaoyi"    %%% "fansi"              % "0.4.0",
      "net.andimiller" %%% "decline-completion" % "0.0.3",
      "org.typelevel"  %%% "toolkit-test"       % "0.1.6" % Test
    ),
    nativeConfig ~= { c =>
      c.withLTO(LTO.full)
        .withMode(Mode.releaseFull)
    },
    version := "0.2",
    envVars ++= {
      Map("S2N_DONT_MLOCK" -> "1")
    },
    mainClass := Some("net.andimiller.puniq.Puniq")
  )
