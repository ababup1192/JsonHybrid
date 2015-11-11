name := """JsonHybrid"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

import sbt.Project.projectToRef

lazy val clients = Seq(client)
lazy val scalaV = "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


lazy val server = (project in file("server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := clients,
  pipelineStages := Seq(scalaJSProd, gzip),
  resolvers ++= Seq(
    "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    "jitpack" at "https://jitpack.io"
  ),
  resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases"),
  libraryDependencies ++= Seq(
    ws,
    "com.lihaoyi" %%% "autowire" % "0.2.5",
    "com.lihaoyi" %%% "upickle" % "0.3.6",
    "com.vmunier" %% "play-scalajs-scripts" % "0.3.0",
    "org.ababup1192" % "hybridparser_2.11" % "0.2.7",
    specs2 % Test
  ),
  // Heroku specific
  herokuAppName in Compile := "json-hybrid-editor",
  herokuSkipSubProjects in Compile := false
).enablePlugins(PlayScala).
  aggregate(clients.map(projectToRef): _*).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  resolvers ++= Seq(
    "amateras-repo" at "http://amateras.sourceforge.jp/mvn-snapshot/",
    "jitpack" at "https://jitpack.io"
  ),
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "autowire" % "0.2.5",
    "com.lihaoyi" %%% "upickle" % "0.3.6",
    "org.scala-js" %%% "scalajs-dom" % "0.8.0",
    "be.doeraene" %%% "scalajs-jquery" % "0.8.1",
    "com.lihaoyi" %%% "scalarx" % "0.2.8",
    "fr.iscpif" %%% "scaladget" % "0.8.5-SNAPSHOT",
    "com.scalawarrior" %%% "scalajs-ace" % "0.0.1-SNAPSHOT",
    "org.ababup1192" % "hybridparser_2.11" % "0.2.7"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(
    scalaVersion := scalaV,
    libraryDependencies += "org.ababup1192" % "hybridparser_2.11" % "0.2.5").
  jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
