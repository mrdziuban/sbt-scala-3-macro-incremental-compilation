ThisBuild / scalaVersion := "3.3.0"

lazy val types = project.in(file("types"))
lazy val macros = project.in(file("macros")).dependsOn(types).aggregate(types)
