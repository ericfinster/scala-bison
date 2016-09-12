
val commonSettings = Seq(
  name := "scala-bison",
  organization := "edu.uwm.cs",
  homepage := Some(url("http://www.cs.uwm.edu/~boyland/scala-bison.html")),
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.10.6"
)

lazy val scalaBison = (project in file(".")).
  settings(commonSettings: _*)

