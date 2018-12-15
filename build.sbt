name := "OrientPairs"

version := "1.0"

scalaVersion := "2.12.7"

libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1212"

libraryDependencies += "org.squeryl" %% "squeryl" % "0.9.7"

val jacksonVersion = "2.9.7"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
