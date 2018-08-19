import sbt._
import Dependencies._


name := "KafkaReader"
version := "0.0.1"

// Version of scala to use
scalaVersion := "2.12.4"

// Append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature", "-Xcheckinit", "-encoding", "utf8")

// Point to location of a snapshot repository for ScalaFX
resolvers += Resolver.mavenCentral
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.jcenterRepo

//resolvers += Opts.resolver.sonatypeStaging

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

// ScalaFX dependency
libraryDependencies ++= rootDependencies

// Set the prompt (for this build) to include the project id.
shellPrompt := { state => System.getProperty("user.name") + ":" + Project.extract(state).currentRef.project + "> " }

// Fork a new JVM for 'run' and 'test:run'
fork := true
