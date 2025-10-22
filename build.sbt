ThisBuild / scalaVersion := "3.7.3"

lazy val types = project.in(file("types"))
lazy val macros = project.in(file("macros")).dependsOn(types).aggregate(types)
