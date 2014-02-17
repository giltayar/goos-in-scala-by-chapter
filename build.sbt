name := "goos"

organization := "com.example"

version := "1.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "org.hamcrest" % "hamcrest-all" % "1.3",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "com.googlecode.windowlicker" % "windowlicker-swing" % "r268",
  "org.igniterealtime.smack" % "smack" % "3.2.1",
  "org.specs2" %% "specs2" % "2.3.7" % "test"
)