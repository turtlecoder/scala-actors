import sbt._

name := "scala-actors"

version := "0.1"

javaOptions := Seq("-XstartOnFirstThread", "-d32")

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.10.4",
  "org.scala-lang" % "scala-library" % "2.10.4",
  "org.scala-lang" % "scala-actors" % "2.10.0")

// The scala-tools.org substitute
resolvers ++= Seq("SonaScalaTools" at "http://oss.sonatype.org/content/groups/scala-tools/",
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "scala_actors" at "https://oss.sonatype.org/content/groups/public/org/scala-lang/scala-actors/2.10.0-M6/",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases")

parallelExecution := false

// https://github.com/szeiger/junit-interface
testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-s", "-a", "-v")

logBuffered in Test := false

parallelExecution in Test := false

excludeFilter in unmanagedSources := "console.scala"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

scalacOptions += "-feature"
